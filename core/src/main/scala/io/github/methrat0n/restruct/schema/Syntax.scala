package io.github.methrat0n.restruct.schema

import java.time.{ LocalDate, LocalTime, ZonedDateTime }

import cats.data.NonEmptyList
import io.github.methrat0n.restruct.core.Program
import io.github.methrat0n.restruct.core.data.schema.{ FieldAlgebra, IntStep, Path, StringStep }

import scala.language.higherKinds

object Syntax {

  val string: Schema[String] = Schema(new Program[FieldAlgebra, String] {
    override def run[F[_]](implicit algebra: FieldAlgebra[F]): F[String] =
      algebra.stringSchema
  })
  val decimal: Schema[Double] = Schema(new Program[FieldAlgebra, Double] {
    override def run[F[_]](implicit algebra: FieldAlgebra[F]): F[Double] =
      algebra.decimalSchema
  })
  val integer: Schema[Int] = Schema(new Program[FieldAlgebra, Int] {
    override def run[F[_]](implicit algebra: FieldAlgebra[F]): F[Int] =
      algebra.integerSchema
  })
  val boolean: Schema[Boolean] = Schema(new Program[FieldAlgebra, Boolean] {
    override def run[F[_]](implicit algebra: FieldAlgebra[F]): F[Boolean] =
      algebra.booleanSchema
  })
  val char: Schema[Char] = Schema(new Program[FieldAlgebra, Char] {
    override def run[F[_]](implicit algebra: FieldAlgebra[F]): F[Char] =
      algebra.charSchema
  })
  val byte: Schema[Byte] = Schema(new Program[FieldAlgebra, Byte] {
    override def run[F[_]](implicit algebra: FieldAlgebra[F]): F[Byte] =
      algebra.byteSchema
  })
  val short: Schema[Short] = Schema(new Program[FieldAlgebra, Short] {
    override def run[F[_]](implicit algebra: FieldAlgebra[F]): F[Short] =
      algebra.shortSchema
  })
  val float: Schema[Float] = Schema(new Program[FieldAlgebra, Float] {
    override def run[F[_]](implicit algebra: FieldAlgebra[F]): F[Float] =
      algebra.floatSchema
  })
  val bigDecimal: Schema[BigDecimal] = Schema(new Program[FieldAlgebra, BigDecimal] {
    override def run[F[_]](implicit algebra: FieldAlgebra[F]): F[BigDecimal] =
      algebra.bigDecimalSchema
  })
  val long: Schema[Long] = Schema(new Program[FieldAlgebra, Long] {
    override def run[F[_]](implicit algebra: FieldAlgebra[F]): F[Long] =
      algebra.longSchema
  })
  val bigInt: Schema[BigInt] = Schema(new Program[FieldAlgebra, BigInt] {
    override def run[F[_]](implicit algebra: FieldAlgebra[F]): F[BigInt] =
      algebra.bigIntSchema
  })
  val dateTime: Schema[ZonedDateTime] = Schema(new Program[FieldAlgebra, ZonedDateTime] {
    override def run[F[_]](implicit algebra: FieldAlgebra[F]): F[ZonedDateTime] =
      algebra.dateTimeSchema
  })
  val time: Schema[LocalTime] = Schema(new Program[FieldAlgebra, LocalTime] {
    override def run[F[_]](implicit algebra: FieldAlgebra[F]): F[LocalTime] =
      algebra.timeSchema
  })
  val date: Schema[LocalDate] = Schema(new Program[FieldAlgebra, LocalDate] {
    override def run[F[_]](implicit algebra: FieldAlgebra[F]): F[LocalDate] =
      algebra.dateSchema
  })

  val list: SchemaConstructor[List] = new SchemaConstructor[List] {
    override def of[A](reader: Schema[A]): Schema[List[A]] = Schema[List[A]](new Program[FieldAlgebra, List[A]] {
      override def run[F[_]](implicit algebra: FieldAlgebra[F]): F[List[A]] =
        algebra.many(reader.bind(algebra))
    })
  }

  import scala.language.implicitConversions

  implicit def string2Path(step: String): Path = Path(NonEmptyList(StringStep(step), List.empty))
  implicit def int2Path(step: Int): Path = Path(NonEmptyList(IntStep(step), List.empty))
}
