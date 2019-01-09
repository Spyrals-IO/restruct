package io.github.methrat0n.restruct.readers.json

import java.time.{ LocalDate, LocalTime, ZonedDateTime }

import io.github.methrat0n.restruct.core.data.schema.SimpleSchemaAlgebra
import play.api.libs.json._

trait SimpleJsonReaderInterpreter extends SimpleSchemaAlgebra[Reads] {
  override def charSchema: Reads[Char] = {
    case JsString(string) if string.length == 1 => JsSuccess(string.charAt(0))
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
      try {
        JsSuccess(BigInt(s))
      }
      catch {
        case _: NumberFormatException => JsError(JsonValidationError("error.expected.numberformatexception"))
      }
    case JsNumber(d) =>
      d.toBigIntExact() match {
        case Some(bigInt) => JsSuccess(bigInt)
        case None         => JsError(JsonValidationError("error.expected.numberformatexception"))
      }
    case _ => JsError(JsonValidationError("error.expected.jsnumberorjsstring"))
  }

  override def booleanSchema: Reads[Boolean] =
    Reads.BooleanReads

  override def stringSchema: Reads[String] =
    Reads.StringReads

  override def dateTimeSchema: Reads[ZonedDateTime] =
    Reads.DefaultZonedDateTimeReads

  override def timeSchema: Reads[LocalTime] =
    Reads.DefaultLocalTimeReads

  override def dateSchema: Reads[LocalDate] =
    Reads.DefaultLocalDateReads
}
