package io.github.methrat0n.restruct.schema

import cats.{ Invariant, Semigroupal }
import io.github.methrat0n.restruct.core.Program
import io.github.methrat0n.restruct.core.data.constraints.Constraint
import io.github.methrat0n.restruct.core.data.schema.FieldAlgebra
import io.github.methrat0n.restruct.schema.Syntax.{ FieldBuilder1, FieldBuilder2, FieldBuilder3, FieldBuilder4, FieldBuilder5 }
import shapeless.{ ::, Coproduct, Generic, HNil }

import scala.language.higherKinds

trait Schema[A] {

  protected[schema] def program: Program[FieldAlgebra, A]
  protected def default: Option[A]

  def constrainted(constraint: Constraint[A]): Schema[A] = Schema(new Program[FieldAlgebra, A] {
    override def run[F[_]](implicit algebra: FieldAlgebra[F]): F[A] =
      algebra.verifying(program.run(algebra), constraint)
  })

  private[schema] def bindName(name: String): FieldSchema[A] = FieldSchema[A](name, new Program[FieldAlgebra, A] {
    override def run[F[_]](implicit algebra: FieldAlgebra[F]): F[A] =
      algebra.required(name, program.run(algebra), default)
  })

  def bind[FORMAT[_]](algebra: FieldAlgebra[FORMAT]): FORMAT[A] =
    program.run(algebra)
}

trait FieldSchema[A] {

  protected[schema] def program: Program[FieldAlgebra, A]
  protected def name: String

  def defaultTo(defaultA: A): FieldSchema[A] = FieldSchema[A](name, new Program[FieldAlgebra, A] {
    override def run[F[_]](implicit algebra: FieldAlgebra[F]): F[A] =
      algebra.required(name, program.run(algebra), Some(defaultA))
  })
}

trait SchemaConstructor[TYPE_CONSTRUCTOR[_]] {
  def of[A](reader: Schema[A]): Schema[TYPE_CONSTRUCTOR[A]]
}

object Schema {
  def apply[A](prgram: Program[FieldAlgebra, A]): Schema[A] = new Schema[A] {
    override val program: Program[FieldAlgebra, A] = prgram
    override val default: Option[A] = None
  }
  def apply[A](prgram: Program[FieldAlgebra, A], defaultA: A): Schema[A] = new Schema[A] {
    override val program: Program[FieldAlgebra, A] = prgram
    override val default: Option[A] = Some(defaultA)
  }

  def is[TYPE <: Product, FIELD_1](builder: FieldBuilder1[FIELD_1])(implicit
    invariant: Invariant[Program[FieldAlgebra, ?]],
    generic: Generic.Aux[TYPE, FIELD_1 :: HNil]
  ): Schema[TYPE] =
    Schema(builder.build)

  def is[TYPE <: Product, FIELD_1, FIELD_2](builder: FieldBuilder2[FIELD_1, FIELD_2])(implicit
    semigroupal: Semigroupal[Program[FieldAlgebra, ?]],
    invariant: Invariant[Program[FieldAlgebra, ?]],
    generic: Generic.Aux[TYPE, FIELD_1 :: FIELD_2 :: HNil]
  ): Schema[TYPE] =
    Schema(builder.build)

  def is[TYPE <: Product, FIELD_1, FIELD_2, FIELD_3](builder: FieldBuilder3[FIELD_1, FIELD_2, FIELD_3])(implicit
    semigroupal: Semigroupal[Program[FieldAlgebra, ?]],
    invariant: Invariant[Program[FieldAlgebra, ?]],
    generic: Generic.Aux[TYPE, FIELD_1 :: FIELD_2 :: FIELD_3 :: HNil]
  ): Schema[TYPE] =
    Schema(builder.build)

  def is[TYPE <: Product, FIELD_1, FIELD_2, FIELD_3, FIELD_4](builder: FieldBuilder4[FIELD_1, FIELD_2, FIELD_3, FIELD_4])(implicit
    semigroupal: Semigroupal[Program[FieldAlgebra, ?]],
    invariant: Invariant[Program[FieldAlgebra, ?]],
    generic: Generic.Aux[TYPE, FIELD_1 :: FIELD_2 :: FIELD_3 :: FIELD_4 :: HNil]
  ): Schema[TYPE] =
    Schema(builder.build)

  def is[TYPE <: Product, FIELD_1, FIELD_2, FIELD_3, FIELD_4, FIELD_5](builder: FieldBuilder5[FIELD_1, FIELD_2, FIELD_3, FIELD_4, FIELD_5])(implicit
    semigroupal: Semigroupal[Program[FieldAlgebra, ?]],
    invariant: Invariant[Program[FieldAlgebra, ?]],
    generic: Generic.Aux[TYPE, FIELD_1 :: FIELD_2 :: FIELD_3 :: FIELD_4 :: FIELD_5 :: HNil]
  ): Schema[TYPE] =
    Schema(builder.build)

  //Coproduct schema construction

  def is[COPRODUCT <: Coproduct, PRODUCT_1 <: COPRODUCT](schema1: Schema[PRODUCT_1]): Schema[COPRODUCT] =
    schema1.asInstanceOf[Schema[COPRODUCT]]

  def is[COPRODUCT <: Coproduct, PRODUCT_1 <: COPRODUCT, PRODUCT_2 <: COPRODUCT](schema1: Schema[PRODUCT_1], schema2: Schema[PRODUCT_2])(implicit manifest1: Manifest[PRODUCT_1], manifest2: Manifest[PRODUCT_2]): Schema[COPRODUCT] =
    Schema(new Program[FieldAlgebra, COPRODUCT] {
      override def run[F[_]](implicit algebra: FieldAlgebra[F]): F[COPRODUCT] = {
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
    Schema(new Program[FieldAlgebra, COPRODUCT] {
      override def run[F[_]](implicit algebra: FieldAlgebra[F]): F[COPRODUCT] = {
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
  def apply[A](nme: String, prog: Program[FieldAlgebra, A]): FieldSchema[A] = new FieldSchema[A] {
    override protected val name: String = nme
    override def program: Program[FieldAlgebra, A] = prog
  }
}
