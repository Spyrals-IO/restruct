package restruct.reader

import cats.{Invariant, Semigroupal}
import restruct.core.Program
import restruct.core.data.constraints.Constraint
import restruct.core.data.schema.ComplexSchemaAlgebra
import restruct.reader.Syntax.{FieldBuilder1, FieldBuilder2, FieldBuilder3, FieldBuilder4, FieldBuilder5, Symboled}
import shapeless.labelled.FieldType
import shapeless.{::, HNil, LabelledGeneric}

import scala.language.higherKinds

trait Reader[A] {

  protected def program: Program[ComplexSchemaAlgebra, A]
  protected def default: Option[A]

  def constrainted(constraint: Constraint[A]): Reader[A] = Reader(new Program[ComplexSchemaAlgebra, A] {
    override def run[F[_]](implicit algebra: ComplexSchemaAlgebra[F]): F[A] =
      algebra.verifying(program.run(algebra), constraint)
  })

  private[reader] def bindName(name: String): NamedReader[A] = NamedReader[A](name, new Program[ComplexSchemaAlgebra, A] {
    override def run[F[_]](implicit algebra: ComplexSchemaAlgebra[F]): F[A] =
      algebra.required(name, program.run(algebra), default)
  })

  def read[FORMAT[_]](algebra: ComplexSchemaAlgebra[FORMAT]): FORMAT[A] =
    program.run(algebra)
}

trait NamedReader[A] {

  protected[reader] def program: Program[ComplexSchemaAlgebra, A]
  protected def name: String

  def read[FORMAT[_]](algebra: ComplexSchemaAlgebra[FORMAT]): FORMAT[A] =
    program.run(algebra)

  def defaultTo(defaultA: A): NamedReader[A] = NamedReader[A](name, new Program[ComplexSchemaAlgebra, A] {
    override def run[F[_]](implicit algebra: ComplexSchemaAlgebra[F]): F[A] =
      algebra.required(name, program.run(algebra), Some(defaultA))
  })
}

trait ReaderConstructor[TYPE_CONSTRUCTOR[_]] {

  protected def bindReader[A](reader: Reader[A]): NameConstructor[TYPE_CONSTRUCTOR[A]]

  def of[A](reader: Reader[A]): NameConstructor[TYPE_CONSTRUCTOR[A]] = bindReader(reader)
}

trait NameConstructor[A] {
  protected[reader] def bindName(name: String): NamedReader[A]
}

object Reader {
  def apply[A](prgram: Program[ComplexSchemaAlgebra, A]): Reader[A] = new Reader[A] {
    override val program: Program[ComplexSchemaAlgebra, A] = prgram
    override val default: Option[A] = None
  }
  def apply[A](prgram: Program[ComplexSchemaAlgebra, A], defaultA: A): Reader[A] = new Reader[A] {
    override val program: Program[ComplexSchemaAlgebra, A] = prgram
    override val default: Option[A] = Some(defaultA)
  }

  def is[TYPE <: Product, KEY_1, FIELD_1](builder: FieldBuilder1[KEY_1, FIELD_1])(implicit
    invariant: Invariant[Program[ComplexSchemaAlgebra, ?]],
    generic: LabelledGeneric.Aux[TYPE, FieldType[Symboled[KEY_1], FIELD_1] :: HNil]
  ): Reader[TYPE] =
    Reader(builder.build)

  def is[TYPE <: Product, KEY_1, FIELD_1, KEY_2, FIELD_2](builder: FieldBuilder2[KEY_1, FIELD_1, KEY_2, FIELD_2])(implicit
    semigroupal: Semigroupal[Program[ComplexSchemaAlgebra, ?]],
    invariant: Invariant[Program[ComplexSchemaAlgebra, ?]],
    generic: LabelledGeneric.Aux[TYPE, FieldType[Symboled[KEY_1], FIELD_1] :: FieldType[Symboled[KEY_2], FIELD_2] :: HNil]
  ): Reader[TYPE] =
    Reader(builder.build)

  def is[TYPE <: Product, KEY_1, FIELD_1, KEY_2, FIELD_2, KEY_3, FIELD_3](builder: FieldBuilder3[KEY_1, FIELD_1, KEY_2, FIELD_2, KEY_3, FIELD_3])(implicit
    semigroupal: Semigroupal[Program[ComplexSchemaAlgebra, ?]],
    invariant: Invariant[Program[ComplexSchemaAlgebra, ?]],
    generic: LabelledGeneric.Aux[TYPE, FieldType[Symboled[KEY_1], FIELD_1] :: FieldType[Symboled[KEY_2], FIELD_2] :: FieldType[Symboled[KEY_3], FIELD_3] :: HNil]
  ): Reader[TYPE] =
    Reader(builder.build)

  def is[TYPE <: Product, KEY_1, FIELD_1, KEY_2, FIELD_2, KEY_3, FIELD_3, KEY_4, FIELD_4](builder: FieldBuilder4[KEY_1, FIELD_1, KEY_2, FIELD_2, KEY_3, FIELD_3, KEY_4, FIELD_4])(implicit
    semigroupal: Semigroupal[Program[ComplexSchemaAlgebra, ?]],
    invariant: Invariant[Program[ComplexSchemaAlgebra, ?]],
    generic: LabelledGeneric.Aux[TYPE, FieldType[Symboled[KEY_1], FIELD_1] :: FieldType[Symboled[KEY_2], FIELD_2] :: FieldType[Symboled[KEY_3], FIELD_3] :: FieldType[Symboled[KEY_4], FIELD_4] :: HNil]
  ): Reader[TYPE] =
    Reader(builder.build)

  def is[TYPE <: Product, KEY_1, FIELD_1, KEY_2, FIELD_2, KEY_3, FIELD_3, KEY_4, FIELD_4, KEY_5, FIELD_5](builder: FieldBuilder5[KEY_1, FIELD_1, KEY_2, FIELD_2, KEY_3, FIELD_3, KEY_4, FIELD_4, KEY_5, FIELD_5])(implicit
    semigroupal: Semigroupal[Program[ComplexSchemaAlgebra, ?]],
    invariant: Invariant[Program[ComplexSchemaAlgebra, ?]],
    generic: LabelledGeneric.Aux[TYPE, FieldType[Symboled[KEY_1], FIELD_1] :: FieldType[Symboled[KEY_2], FIELD_2] :: FieldType[Symboled[KEY_3], FIELD_3] :: FieldType[Symboled[KEY_4], FIELD_4] :: FieldType[Symboled[KEY_5], FIELD_5] :: HNil]
  ): Reader[TYPE] =
    Reader(builder.build)
}

object NamedReader {
  def apply[A](nme: String, prog: Program[ComplexSchemaAlgebra, A]): NamedReader[A] = new NamedReader[A] {
    override protected val name: String = nme
    override def program: Program[ComplexSchemaAlgebra, A] = prog
  }
}
