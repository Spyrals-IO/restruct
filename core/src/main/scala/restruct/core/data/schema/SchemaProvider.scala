package restruct.core.data.schema

import cats.implicits._
import restruct.core.Program
import restruct.core.data.constraints.Constraints
import shapeless.labelled.FieldType
import shapeless.{:+:, ::, CNil, Coproduct, Default, HList, HNil, Inl, Inr, LabelledGeneric, Lazy, Witness, labelled}

import scala.language.higherKinds

trait SchemaProvider[T] {
  def apply(): Program[ComplexSchemaAlgebra, T]
}

//TODO split to allow the use of SimpleSchemaAlgebra,
// to be able to Schema.from[String] with a format which dont support object
object SchemaProvider {

  implicit def genericCoproductSchemaProvider[T, PRODUCTS <: Coproduct](implicit
    labelledGeneric: LabelledGeneric.Aux[T, PRODUCTS],
    productsSchema: SchemaProvider[PRODUCTS]
  ): SchemaProvider[T] = () => new Program[ComplexSchemaAlgebra, T] {
    override def run[F[_]](implicit algebra: ComplexSchemaAlgebra[F]): F[T] =
      productsSchema().run[F](algebra).imap(labelledGeneric.from)(labelledGeneric.to)
  }

  implicit def cconsSchemaProvider[PRODUCT, PRODUCTS <: Coproduct](implicit
    productSchemaProvider: SchemaProvider[PRODUCT],
    productsSchemaProvider: Lazy[SchemaProvider[PRODUCTS]]
  ): SchemaProvider[PRODUCT :+: PRODUCTS] = () => new Program[ComplexSchemaAlgebra, PRODUCT :+: PRODUCTS] {
    override def run[F[_]](implicit algebra: ComplexSchemaAlgebra[F]): F[PRODUCT :+: PRODUCTS] =
      algebra.either(
        productsSchemaProvider.value().run(algebra),
        productSchemaProvider().run(algebra)
      ).imap(
        _.fold(Inr.apply, Inl.apply)
      )({
        case Inr(products) => Left(products)
        case Inl(product) => Right(product)
      })
  }

  implicit def cnilSchemaProvider[PRODUCT](implicit
    productSchemaProvider: SchemaProvider[PRODUCT]
  ): SchemaProvider[PRODUCT :+: CNil] = () => new Program[ComplexSchemaAlgebra, PRODUCT :+: CNil] {
    override def run[F[_]](implicit algebra: ComplexSchemaAlgebra[F]): F[PRODUCT :+: CNil] =
      productSchemaProvider().run(algebra).imap[PRODUCT :+: CNil](Inl.apply)(_.eliminate(identity, throw new IllegalStateException("An impossible case occured")))
  }

  //Product deconstruction
  implicit def genericProductSchemaProvider[T <: Product, FIELDS <: HList, DEFAULTS <: HList](implicit
    labbeledGeneric: LabelledGeneric.Aux[T, FIELDS],
    defaultGeneric: Default.AsOptions.Aux[T, DEFAULTS],
    fieldsSchema: SchemaProvider.WithDefault[FIELDS, DEFAULTS]
  ): SchemaProvider[T] = () => new Program[ComplexSchemaAlgebra, T] {
    override def run[F[_]](implicit algebra: ComplexSchemaAlgebra[F]): F[T] =
      fieldsSchema(defaultGeneric()).run(algebra).imap(labbeledGeneric.from)(labbeledGeneric.to)
  }

  implicit def labbeldProductSchemaProvider[KEY <: Symbol, PRODUCT](implicit
    witness: Witness.Aux[KEY],
    productSchemaProvider: SchemaProvider[PRODUCT],
  ): SchemaProvider[FieldType[KEY, PRODUCT]] = () => new Program[ComplexSchemaAlgebra, FieldType[KEY, PRODUCT]] {
    override def run[F[_]](implicit algebra: ComplexSchemaAlgebra[F]): F[FieldType[KEY, PRODUCT]] =
      (
        algebra.required("__type", algebra.verifying(algebra.stringSchema, Constraints.EqualConstraint(witness.value.name)), None),
        productSchemaProvider().run(algebra)
      ).imapN((_, value) => labelled.field[KEY](value))(value => (witness.value.name, value))
  }

  trait WithDefault[T, D] {
    def apply(default: D): Program[ComplexSchemaAlgebra, T]
  }

  object WithDefault {
    implicit def hconsSchemaProgram[FIELD, FIELDS <: HList, DEFAULT, DEFAULTS <: HList](implicit
      fieldSchema: SchemaProvider.WithDefault[FIELD, DEFAULT],
      fieldsSchema: Lazy[SchemaProvider.WithDefault[FIELDS, DEFAULTS]]
    ): SchemaProvider.WithDefault[FIELD :: FIELDS, DEFAULT :: DEFAULTS] = (default: DEFAULT :: DEFAULTS) => new Program[ComplexSchemaAlgebra, FIELD :: FIELDS] {
      override def run[F[_]](implicit algebra: ComplexSchemaAlgebra[F]): F[FIELD :: FIELDS] = {
        (
          fieldSchema(default.head).run(algebra),
          fieldsSchema.value(default.tail).run(algebra)
        ).imapN(_ :: _)(list => (list.head, list.tail))
      }
    }

    implicit val hnilSchemaProgram: SchemaProvider.WithDefault[HNil, HNil] = _ => new Program[ComplexSchemaAlgebra, HNil] {
      override def run[F[_]](implicit algebra: ComplexSchemaAlgebra[F]): F[HNil] =
        algebra.pure(HNil)
    }

    implicit def requiredFieldSchemaProgram[KEY <: Symbol, VALUE](implicit
      witness: Witness.Aux[KEY],
      valueSchema: Program[ComplexSchemaAlgebra, VALUE]
    ): SchemaProvider.WithDefault[FieldType[KEY, VALUE], Option[VALUE]] =
      (default: Option[VALUE]) => new Program[ComplexSchemaAlgebra, FieldType[KEY, VALUE]] {
        override def run[F[_]](implicit algebra: ComplexSchemaAlgebra[F]): F[FieldType[KEY, VALUE]] =
          algebra.required(witness.value.name, valueSchema.run(algebra), default).imap(labelled.field[KEY].apply _)(identity)
      }

    implicit def manyFieldSchemaProgram[KEY <: Symbol, VALUE](implicit
      witness: Witness.Aux[KEY],
      valueSchema: Program[ComplexSchemaAlgebra, VALUE]
    ): SchemaProvider.WithDefault[FieldType[KEY, List[VALUE]], Option[List[VALUE]]] =
      (default: Option[List[VALUE]]) => new Program[ComplexSchemaAlgebra, FieldType[KEY, List[VALUE]]] {
        override def run[F[_]](implicit algebra: ComplexSchemaAlgebra[F]): F[FieldType[KEY, List[VALUE]]] =
          algebra.many(witness.value.name, valueSchema.run(algebra), default).imap(labelled.field[KEY].apply _)(identity)
      }

    implicit def optionalFieldSchemaProgram[KEY <: Symbol, VALUE](implicit
      witness: Witness.Aux[KEY],
      valueSchema: Program[ComplexSchemaAlgebra, VALUE]
    ): SchemaProvider.WithDefault[FieldType[KEY, Option[VALUE]], Option[Option[VALUE]]] =
      (default: Option[Option[VALUE]]) => new Program[ComplexSchemaAlgebra, FieldType[KEY, Option[VALUE]]] {
        override def run[F[_]](implicit algebra: ComplexSchemaAlgebra[F]): F[FieldType[KEY, Option[VALUE]]] =
          algebra.optional(witness.value.name, valueSchema.run(algebra), default).imap(labelled.field[KEY].apply _)(identity)
      }
  }
}
