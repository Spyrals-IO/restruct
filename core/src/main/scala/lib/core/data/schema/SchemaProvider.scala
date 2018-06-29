package lib.core.data.schema

import cats.implicits._
import lib.core.Program
import lib.core.data.constraints.Constraints
import shapeless.labelled.FieldType
import shapeless.{:+:, ::, CNil, Coproduct, Default, HList, HNil, Inl, Inr, LabelledGeneric, Lazy, Witness, labelled}

import scala.language.higherKinds

trait SchemaProvider[T] {
  def apply(): Program[SchemaAlgebra, T]
}

object SchemaProvider {
  implicit def genericProductSchemaProvider[T <: Product, FIELDS <: HList, DEFAULTS <: HList](implicit
    labbeledGeneric: LabelledGeneric.Aux[T, FIELDS],
    defaultGeneric: Default.AsOptions.Aux[T, DEFAULTS],
    fieldsSchema: SchemaProvider.WithDefault[FIELDS, DEFAULTS]
  ): SchemaProvider[T] = () => new Program[SchemaAlgebra, T] {
    override def run[F[_]](implicit algebra: SchemaAlgebra[F]): F[T] =
      fieldsSchema(defaultGeneric()).run(algebra).imap(labbeledGeneric.from)(labbeledGeneric.to)
  }

  implicit def genericCoproductSchemaProvider[T, PRODUCTS <: Coproduct](implicit
    labelledGeneric: LabelledGeneric.Aux[T, PRODUCTS],
    productsSchema: SchemaProvider[PRODUCTS]
  ): SchemaProvider[T] = () => new Program[SchemaAlgebra, T] {
    override def run[F[_]](implicit algebra: SchemaAlgebra[F]): F[T] =
      productsSchema().run(algebra).imap(labelledGeneric.from)(labelledGeneric.to)
  }

  implicit def cconsSchemaProvider[PRODUCT, PRODUCTS <: Coproduct](implicit
    productSchemaProvider: SchemaProvider[PRODUCT],
    productsSchemaProvider: Lazy[SchemaProvider[PRODUCTS]]
  ): SchemaProvider[PRODUCT :+: PRODUCTS] = () => new Program[SchemaAlgebra, PRODUCT :+: PRODUCTS] {
    override def run[F[_]](implicit algebra: SchemaAlgebra[F]): F[PRODUCT :+: PRODUCTS] =
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
  ): SchemaProvider[PRODUCT :+: CNil] = () => new Program[SchemaAlgebra, PRODUCT :+: CNil] {
    override def run[F[_]](implicit algebra: SchemaAlgebra[F]): F[PRODUCT :+: CNil] =
      productSchemaProvider().run(algebra).imap[PRODUCT :+: CNil](Inl.apply)(_.eliminate(identity, throw new IllegalStateException("An impossible case occured")))
  }

  implicit def labbeldProductSchemaProvider[KEY <: Symbol, PRODUCT](implicit
    witness: Witness.Aux[KEY],
    productSchemaProvider: SchemaProvider[PRODUCT],
  ): SchemaProvider[FieldType[KEY, PRODUCT]] = () => new Program[SchemaAlgebra, FieldType[KEY, PRODUCT]] {
    override def run[F[_]](implicit algebra: SchemaAlgebra[F]): F[FieldType[KEY, PRODUCT]] =
      (
        algebra.required("__type", algebra.verifying(algebra.stringSchema, Constraints.EqualConstraint(witness.value.name)), None),
        productSchemaProvider().run(algebra)
      ).imapN((_, value) => labelled.field[KEY](value))(value => (witness.value.name, value))
  }

  trait WithDefault[T, D] {
    def apply(default: D): Program[SchemaAlgebra, T]
  }

  object WithDefault {
    implicit def hconsSchemaProgram[FIELD, FIELDS <: HList, DEFAULT, DEFAULTS <: HList](implicit
      fieldSchema: SchemaProvider.WithDefault[FIELD, DEFAULT],
      fieldsSchema: Lazy[SchemaProvider.WithDefault[FIELDS, DEFAULTS]]
    ): SchemaProvider.WithDefault[FIELD :: FIELDS, DEFAULT :: DEFAULTS] = (default: DEFAULT :: DEFAULTS) => new Program[SchemaAlgebra, FIELD :: FIELDS] {
      override def run[F[_]](implicit algebra: SchemaAlgebra[F]): F[FIELD :: FIELDS] = {
        (
          fieldSchema(default.head).run(algebra),
          fieldsSchema.value(default.tail).run(algebra)
        ).imapN(_ :: _)(list => (list.head, list.tail))
      }
    }

    implicit val hnilSchemaProgram: SchemaProvider.WithDefault[HNil, HNil] = _ => new Program[SchemaAlgebra, HNil] {
      override def run[F[_]](implicit algebra: SchemaAlgebra[F]): F[HNil] =
        algebra.pure(HNil)
    }

    implicit def requiredFieldSchemaProgram[KEY <: Symbol, VALUE](implicit
      witness: Witness.Aux[KEY],
      valueSchema: Program[SchemaAlgebra, VALUE]
    ): SchemaProvider.WithDefault[FieldType[KEY, VALUE], Option[VALUE]] =
      (default: Option[VALUE]) => new Program[SchemaAlgebra, FieldType[KEY, VALUE]] {
        override def run[F[_]](implicit algebra: SchemaAlgebra[F]): F[FieldType[KEY, VALUE]] =
          algebra.required(witness.value.name, valueSchema.run(algebra), default).imap(labelled.field[KEY].apply _)(identity)
      }

    implicit def manyFieldSchemaProgram[KEY <: Symbol, VALUE](implicit
      witness: Witness.Aux[KEY],
      valueSchema: Program[SchemaAlgebra, VALUE]
    ): SchemaProvider.WithDefault[FieldType[KEY, List[VALUE]], Option[List[VALUE]]] =
      (default: Option[List[VALUE]]) => new Program[SchemaAlgebra, FieldType[KEY, List[VALUE]]] {
        override def run[F[_]](implicit algebra: SchemaAlgebra[F]): F[FieldType[KEY, List[VALUE]]] =
          algebra.many(witness.value.name, valueSchema.run(algebra), default).imap(labelled.field[KEY].apply _)(identity)
      }

    implicit def optionalFieldSchemaProgram[KEY <: Symbol, VALUE](implicit
      witness: Witness.Aux[KEY],
      valueSchema: Program[SchemaAlgebra, VALUE]
    ): SchemaProvider.WithDefault[FieldType[KEY, Option[VALUE]], Option[Option[VALUE]]] =
      (default: Option[Option[VALUE]]) => new Program[SchemaAlgebra, FieldType[KEY, Option[VALUE]]] {
        override def run[F[_]](implicit algebra: SchemaAlgebra[F]): F[FieldType[KEY, Option[VALUE]]] =
          algebra.optional(witness.value.name, valueSchema.run(algebra), default).imap(labelled.field[KEY].apply _)(identity)
      }
  }
}
