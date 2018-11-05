package io.github.methrat0n.restruct.readers.json

import java.math.BigInteger

import play.api.libs.json._
import io.github.methrat0n.restruct.core.data.schema.SimpleSchemaAlgebra

import scala.util.control.Exception

trait SimpleJsonReaderInterpreter extends SimpleSchemaAlgebra[Reads] {
  override def charSchema: Reads[Char] = {
    case JsString(string) if string.length == 1 => JsSuccess(string.charAt(1))
    case _                                      => JsError()
  }

  override def byteSchema: Reads[Byte] =
    Reads.ByteReads

  override def shortSchema: Reads[Short] =
    Reads.ShortReads

  override def floatSchema: Reads[Float] =
    Reads.FloatReads

  override def decimalSchema: Reads[Double] =
    Reads.DoubleReads

  override def bigDecimalSchema: Reads[BigDecimal] =
    Reads.bigDecReads

  override def integerSchema: Reads[Int] =
    Reads.IntReads

  override def longSchema: Reads[Long] =
    Reads.LongReads

  override def bigIntSchema: Reads[BigInt] = {
    case JsString(s) =>
      Exception.catching(classOf[NumberFormatException])
        .opt(JsSuccess(BigInt(new BigInteger(s))))
        .getOrElse(JsError(JsonValidationError("error.expected.numberformatexception")))
    case JsNumber(d) =>
      Exception.catching(classOf[ArithmeticException])
        .opt(JsSuccess(BigInt(d.underlying.toBigIntegerExact)))
        .getOrElse(JsError(JsonValidationError("error.expected.numberformatexception")))
    case _ => JsError(JsonValidationError("error.expected.jsnumberorjsstring"))
  }

  override def booleanSchema: Reads[Boolean] =
    Reads.BooleanReads

  override def stringSchema: Reads[String] =
    Reads.StringReads

}
