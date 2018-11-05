package io.github.methrat0n.restruct.writers.json

import play.api.libs.json.{ JsNumber, JsString, Writes }
import io.github.methrat0n.restruct.core.data.schema.SimpleSchemaAlgebra

trait SimpleJsonWriterInterpreter extends SimpleSchemaAlgebra[Writes] {
  override def stringSchema: Writes[String] =
    Writes.StringWrites

  override def decimalSchema: Writes[Double] =
    Writes.DoubleWrites

  override def integerSchema: Writes[Int] =
    Writes.IntWrites

  override def booleanSchema: Writes[Boolean] =
    Writes.BooleanWrites

  override def charSchema: Writes[Char] =
    Writes[Char](char => JsString(char.toString))

  override def byteSchema: Writes[Byte] =
    Writes.ByteWrites

  override def shortSchema: Writes[Short] =
    Writes.ShortWrites

  override def floatSchema: Writes[Float] =
    Writes.FloatWrites

  override def bigDecimalSchema: Writes[BigDecimal] =
    Writes.BigDecimalWrites

  override def longSchema: Writes[Long] =
    Writes.LongWrites

  override def bigIntSchema: Writes[BigInt] =
    Writes[BigInt](bigInt => JsNumber(BigDecimal(bigInt)))
}
