package restruct.reader

import cats.{ Invariant, Semigroupal }
import restruct.core.Program
import restruct.core.data.constraints.Constraint
import restruct.core.data.schema.ComplexSchemaAlgebra
import restruct.reader.Syntax.{ FieldBuilder1, FieldBuilder2, FieldBuilder3, FieldBuilder4, FieldBuilder5 }
import shapeless.{ ::, Generic, HNil }

import scala.language.higherKinds

trait Schema[A] {

  protected def program: Program[ComplexSchemaAlgebra, A]
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
}

object FieldSchema {
  def apply[A](nme: String, prog: Program[ComplexSchemaAlgebra, A]): FieldSchema[A] = new FieldSchema[A] {
    override protected val name: String = nme
    override def program: Program[ComplexSchemaAlgebra, A] = prog
  }
}
