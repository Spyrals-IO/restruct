package io.github.methrat0n.restruct.schema

import java.time.{ LocalDate, LocalTime, ZonedDateTime }

import cats.data.NonEmptyList
import io.github.methrat0n.restruct.core.Program
import io.github.methrat0n.restruct.core.data.schema.{ FieldAlgebra, IntStep, Path, StringStep }

import scala.language.higherKinds

object Syntax {

  implicit val string: Schema[String] = TypedSchema(new Program[FieldAlgebra, String] {
    override def run[F[_]](implicit algebra: FieldAlgebra[F]): F[String] =
      algebra.stringSchema
  })
  implicit val decimal: Schema[Double] = TypedSchema(new Program[FieldAlgebra, Double] {
    override def run[F[_]](implicit algebra: FieldAlgebra[F]): F[Double] =
      algebra.decimalSchema
  })
  implicit val integer: Schema[Int] = TypedSchema(new Program[FieldAlgebra, Int] {
    override def run[F[_]](implicit algebra: FieldAlgebra[F]): F[Int] =
      algebra.integerSchema
  })
  implicit val boolean: Schema[Boolean] = TypedSchema(new Program[FieldAlgebra, Boolean] {
    override def run[F[_]](implicit algebra: FieldAlgebra[F]): F[Boolean] =
      algebra.booleanSchema
  })
  implicit val char: Schema[Char] = TypedSchema(new Program[FieldAlgebra, Char] {
    override def run[F[_]](implicit algebra: FieldAlgebra[F]): F[Char] =
      algebra.charSchema
  })
  implicit val byte: Schema[Byte] = TypedSchema(new Program[FieldAlgebra, Byte] {
    override def run[F[_]](implicit algebra: FieldAlgebra[F]): F[Byte] =
      algebra.byteSchema
  })
  implicit val short: Schema[Short] = TypedSchema(new Program[FieldAlgebra, Short] {
    override def run[F[_]](implicit algebra: FieldAlgebra[F]): F[Short] =
      algebra.shortSchema
  })
  implicit val float: Schema[Float] = TypedSchema(new Program[FieldAlgebra, Float] {
    override def run[F[_]](implicit algebra: FieldAlgebra[F]): F[Float] =
      algebra.floatSchema
  })
  implicit val bigDecimal: Schema[BigDecimal] = TypedSchema(new Program[FieldAlgebra, BigDecimal] {
    override def run[F[_]](implicit algebra: FieldAlgebra[F]): F[BigDecimal] =
      algebra.bigDecimalSchema
  })
  implicit val long: Schema[Long] = TypedSchema(new Program[FieldAlgebra, Long] {
    override def run[F[_]](implicit algebra: FieldAlgebra[F]): F[Long] =
      algebra.longSchema
  })
  implicit val bigInt: Schema[BigInt] = TypedSchema(new Program[FieldAlgebra, BigInt] {
    override def run[F[_]](implicit algebra: FieldAlgebra[F]): F[BigInt] =
      algebra.bigIntSchema
  })
  implicit val dateTime: Schema[ZonedDateTime] = TypedSchema(new Program[FieldAlgebra, ZonedDateTime] {
    override def run[F[_]](implicit algebra: FieldAlgebra[F]): F[ZonedDateTime] =
      algebra.dateTimeSchema
  })
  implicit val time: Schema[LocalTime] = TypedSchema(new Program[FieldAlgebra, LocalTime] {
    override def run[F[_]](implicit algebra: FieldAlgebra[F]): F[LocalTime] =
      algebra.timeSchema
  })
  implicit val date: Schema[LocalDate] = TypedSchema(new Program[FieldAlgebra, LocalDate] {
    override def run[F[_]](implicit algebra: FieldAlgebra[F]): F[LocalDate] =
      algebra.dateSchema
  })

  val list: SchemaConstructor[List] = new SchemaConstructor[List] {
    override def of[A](reader: Schema[A]): Schema[List[A]] = TypedSchema[List[A]](new Program[FieldAlgebra, List[A]] {
      override def run[F[_]](implicit algebra: FieldAlgebra[F]): F[List[A]] =
        algebra.many(reader.bind(algebra))
    })
  }

  import scala.language.implicitConversions

  implicit def string2Path(step: String): Path = Path(NonEmptyList(StringStep(step), List.empty))
  implicit def int2Path(step: Int): Path = Path(NonEmptyList(IntStep(step), List.empty))
}
