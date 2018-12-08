package io.github.methrat0n.restruct.core.data.schema

import java.time.{ LocalDate, LocalTime, ZonedDateTime }

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
