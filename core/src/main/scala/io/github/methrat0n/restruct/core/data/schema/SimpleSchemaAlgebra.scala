package io.github.methrat0n.restruct.core.data.schema

import java.time.{ LocalDate, LocalTime, ZonedDateTime }

import io.github.methrat0n.restruct.core.Program

import scala.language.higherKinds

trait SimpleSchemaAlgebra[F[_]] {

  def charSchema: F[Char]

  def byteSchema: F[Byte]

  def shortSchema: F[Short]

  def floatSchema: F[Float]

  def decimalSchema: F[Double]

  def bigDecimalSchema: F[BigDecimal]

  def integerSchema: F[Int]

  def longSchema: F[Long]

  def bigIntSchema: F[BigInt]

  def booleanSchema: F[Boolean]

  def stringSchema: F[String]

  def dateTimeSchema: F[ZonedDateTime]

  def dateSchema: F[LocalDate]

  def timeSchema: F[LocalTime]

}

object SimpleSchemaAlgebra {

  implicit val charSchema: Program[SimpleSchemaAlgebra, Char] = new Program[SimpleSchemaAlgebra, Char] {
    override def run[F[_]](implicit algebra: SimpleSchemaAlgebra[F]): F[Char] = algebra.charSchema
  }

  implicit val byteSchema: Program[SimpleSchemaAlgebra, Byte] = new Program[SimpleSchemaAlgebra, Byte] {
    override def run[F[_]](implicit algebra: SimpleSchemaAlgebra[F]): F[Byte] = algebra.byteSchema
  }

  implicit val shortSchema: Program[SimpleSchemaAlgebra, Short] = new Program[SimpleSchemaAlgebra, Short] {
    override def run[F[_]](implicit algebra: SimpleSchemaAlgebra[F]): F[Short] = algebra.shortSchema
  }

  implicit val floatSchema: Program[SimpleSchemaAlgebra, Float] = new Program[SimpleSchemaAlgebra, Float] {
    override def run[F[_]](implicit algebra: SimpleSchemaAlgebra[F]): F[Float] = algebra.floatSchema
  }

  implicit val decimalSchema: Program[SimpleSchemaAlgebra, Double] = new Program[SimpleSchemaAlgebra, Double] {
    override def run[F[_]](implicit algebra: SimpleSchemaAlgebra[F]): F[Double] = algebra.decimalSchema
  }

  implicit val bigDecimalSchema: Program[SimpleSchemaAlgebra, BigDecimal] = new Program[SimpleSchemaAlgebra, BigDecimal] {
    override def run[F[_]](implicit algebra: SimpleSchemaAlgebra[F]): F[BigDecimal] = algebra.bigDecimalSchema
  }

  implicit val integerSchema: Program[SimpleSchemaAlgebra, Int] = new Program[SimpleSchemaAlgebra, Int] {
    override def run[F[_]](implicit algebra: SimpleSchemaAlgebra[F]): F[Int] = algebra.integerSchema
  }

  implicit val longSchema: Program[SimpleSchemaAlgebra, Long] = new Program[SimpleSchemaAlgebra, Long] {
    override def run[F[_]](implicit algebra: SimpleSchemaAlgebra[F]): F[Long] = algebra.longSchema
  }

  implicit val bigIntSchema: Program[SimpleSchemaAlgebra, BigInt] = new Program[SimpleSchemaAlgebra, BigInt] {
    override def run[F[_]](implicit algebra: SimpleSchemaAlgebra[F]): F[BigInt] = algebra.bigIntSchema
  }

  implicit val booleanSchema: Program[SimpleSchemaAlgebra, Boolean] = new Program[SimpleSchemaAlgebra, Boolean] {
    override def run[F[_]](implicit algebra: SimpleSchemaAlgebra[F]): F[Boolean] = algebra.booleanSchema
  }

  implicit val stringSchema: Program[SimpleSchemaAlgebra, String] = new Program[SimpleSchemaAlgebra, String] {
    override def run[F[_]](implicit algebra: SimpleSchemaAlgebra[F]): F[String] = algebra.stringSchema
  }

  implicit val dateTimeSchema: Program[SimpleSchemaAlgebra, ZonedDateTime] = new Program[SimpleSchemaAlgebra, ZonedDateTime] {
    override def run[F[_]](implicit algebra: SimpleSchemaAlgebra[F]): F[ZonedDateTime] = algebra.dateTimeSchema
  }

  implicit val timeSchema: Program[SimpleSchemaAlgebra, LocalTime] = new Program[SimpleSchemaAlgebra, LocalTime] {
    override def run[F[_]](implicit algebra: SimpleSchemaAlgebra[F]): F[LocalTime] = algebra.timeSchema
  }
}
