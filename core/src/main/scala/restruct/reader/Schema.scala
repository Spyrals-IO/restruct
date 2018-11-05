package restruct.reader

import cats.{ Invariant, Semigroupal }
import restruct.core.Program
import restruct.core.data.constraints.Constraint
import restruct.core.data.schema.ComplexSchemaAlgebra
import restruct.reader.Syntax.{ FieldBuilder1, FieldBuilder2, FieldBuilder3, FieldBuilder4, FieldBuilder5 }
import shapeless.{ ::, Coproduct, Generic, HNil }

import scala.language.higherKinds

trait Schema[A] {

  protected[reader] def program: Program[ComplexSchemaAlgebra, A]
  protected def default: Option[A]

  def constrainted(constraint: Constraint[A]): Schema[A] = Schema(new Program[ComplexSchemaAlgebra, A] {
    override def run[F[_]](implicit algebra: ComplexSchemaAlgebra[F]): F[A] =
      algebra.verifying(program.run(algebra), constraint)
  })

  private[reader] def bindName(name: String): FieldSchema[A] = FieldSchema[A](name, new Program[ComplexSchemaAlgebra, A] {
    override def run[F[_]](implicit algebra: ComplexSchemaAlgebra[F]): F[A] =
      algebra.required(name, program.run(algebra), default)
  })

  def read[FORMAT[_]](algebra: ComplexSchemaAlgebra[FORMAT]): FORMAT[A] =
    program.run(algebra)
}

trait FieldSchema[A] {

  protected[reader] def program: Program[ComplexSchemaAlgebra, A]
  protected def name: String

  def read[FORMAT[_]](algebra: ComplexSchemaAlgebra[FORMAT]): FORMAT[A] =
    program.run(algebra)

  def defaultTo(defaultA: A): FieldSchema[A] = FieldSchema[A](name, new Program[ComplexSchemaAlgebra, A] {
    override def run[F[_]](implicit algebra: ComplexSchemaAlgebra[F]): F[A] =
      algebra.required(name, program.run(algebra), Some(defaultA))
  })
}

trait SchemaConstructor[TYPE_CONSTRUCTOR[_]] {

  protected def bindSchema[A](reader: Schema[A]): NameConstructor[TYPE_CONSTRUCTOR[A]]

  def of[A](reader: Schema[A]): NameConstructor[TYPE_CONSTRUCTOR[A]] = bindSchema(reader)
}

trait NameConstructor[A] {
  protected[reader] def bindName(name: String): FieldSchema[A]
}

object Schema {
  def apply[A](prgram: Program[ComplexSchemaAlgebra, A]): Schema[A] = new Schema[A] {
    override val program: Program[ComplexSchemaAlgebra, A] = prgram
    override val default: Option[A] = None
  }
  def apply[A](prgram: Program[ComplexSchemaAlgebra, A], defaultA: A): Schema[A] = new Schema[A] {
    override val program: Program[ComplexSchemaAlgebra, A] = prgram
    override val default: Option[A] = Some(defaultA)
  }

  def is[TYPE <: Product, FIELD_1](builder: FieldBuilder1[FIELD_1])(implicit
    invariant: Invariant[Program[ComplexSchemaAlgebra, ?]],
    generic: Generic.Aux[TYPE, FIELD_1 :: HNil]
  ): Schema[TYPE] =
    Schema(builder.build)

  def is[TYPE <: Product, FIELD_1, FIELD_2](builder: FieldBuilder2[FIELD_1, FIELD_2])(implicit
    semigroupal: Semigroupal[Program[ComplexSchemaAlgebra, ?]],
    invariant: Invariant[Program[ComplexSchemaAlgebra, ?]],
    generic: Generic.Aux[TYPE, FIELD_1 :: FIELD_2 :: HNil]
  ): Schema[TYPE] =
    Schema(builder.build)

  def is[TYPE <: Product, FIELD_1, FIELD_2, FIELD_3](builder: FieldBuilder3[FIELD_1, FIELD_2, FIELD_3])(implicit
    semigroupal: Semigroupal[Program[ComplexSchemaAlgebra, ?]],
    invariant: Invariant[Program[ComplexSchemaAlgebra, ?]],
    generic: Generic.Aux[TYPE, FIELD_1 :: FIELD_2 :: FIELD_3 :: HNil]
  ): Schema[TYPE] =
    Schema(builder.build)

  def is[TYPE <: Product, FIELD_1, FIELD_2, FIELD_3, FIELD_4](builder: FieldBuilder4[FIELD_1, FIELD_2, FIELD_3, FIELD_4])(implicit
    semigroupal: Semigroupal[Program[ComplexSchemaAlgebra, ?]],
    invariant: Invariant[Program[ComplexSchemaAlgebra, ?]],
    generic: Generic.Aux[TYPE, FIELD_1 :: FIELD_2 :: FIELD_3 :: FIELD_4 :: HNil]
  ): Schema[TYPE] =
    Schema(builder.build)

  def is[TYPE <: Product, FIELD_1, FIELD_2, FIELD_3, FIELD_4, FIELD_5](builder: FieldBuilder5[FIELD_1, FIELD_2, FIELD_3, FIELD_4, FIELD_5])(implicit
    semigroupal: Semigroupal[Program[ComplexSchemaAlgebra, ?]],
    invariant: Invariant[Program[ComplexSchemaAlgebra, ?]],
    generic: Generic.Aux[TYPE, FIELD_1 :: FIELD_2 :: FIELD_3 :: FIELD_4 :: FIELD_5 :: HNil]
  ): Schema[TYPE] =
    Schema(builder.build)

  //Coproduct schema construction

  def is[COPRODUCT <: Coproduct, PRODUCT_1 <: COPRODUCT](schema1: Schema[PRODUCT_1]): Schema[COPRODUCT] =
    schema1.asInstanceOf[Schema[COPRODUCT]]

  def is[COPRODUCT <: Coproduct, PRODUCT_1 <: COPRODUCT, PRODUCT_2 <: COPRODUCT](schema1: Schema[PRODUCT_1], schema2: Schema[PRODUCT_2])(implicit manifest1: Manifest[PRODUCT_1], manifest2: Manifest[PRODUCT_2]): Schema[COPRODUCT] =
    Schema(new Program[ComplexSchemaAlgebra, COPRODUCT] {
      override def run[F[_]](implicit algebra: ComplexSchemaAlgebra[F]): F[COPRODUCT] = {
        val f1 = schema1.program.run(algebra)
        val f2 = schema2.program.run(algebra)

        algebra.imap(algebra.either(f1, f2))({
          case Right(product) => product
          case Left(product)  => product
        })({
          case product: PRODUCT_2 => Right(product)
          case product: PRODUCT_1 => Left(product)
        })
      }
    })

  def is[COPRODUCT, PRODUCT_1 <: COPRODUCT, PRODUCT_2 <: COPRODUCT, PRODUCT_3 <: COPRODUCT](schema1: Schema[PRODUCT_1], schema2: Schema[PRODUCT_2], schema3: Schema[PRODUCT_3])(implicit manifest1: Manifest[PRODUCT_1], manifest2: Manifest[PRODUCT_2], manifest3: Manifest[PRODUCT_3]): Schema[COPRODUCT] =
    Schema(new Program[ComplexSchemaAlgebra, COPRODUCT] {
      override def run[F[_]](implicit algebra: ComplexSchemaAlgebra[F]): F[COPRODUCT] = {
        val f1 = schema1.program.run(algebra)
        val f2 = schema2.program.run(algebra)
        val f3 = schema3.program.run(algebra)

        algebra.imap(algebra.either(f1, algebra.either(f2, f3)))({
          case Right(Right(product)) => product
          case Right(Left(product))  => product
          case Left(product)         => product
        })({
          case product: PRODUCT_3 => Right(Right(product))
          case product: PRODUCT_2 => Right(Left(product))
          case product: PRODUCT_1 => Left(product)
        })
      }
    })
}

object FieldSchema {
  def apply[A](nme: String, prog: Program[ComplexSchemaAlgebra, A]): FieldSchema[A] = new FieldSchema[A] {
    override protected val name: String = nme
    override def program: Program[ComplexSchemaAlgebra, A] = prog
  }
}
