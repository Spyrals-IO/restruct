package restruct.algebras.json.playjson.reads

import play.api.libs.json._
import restruct.core.data.schema.SimpleSchemaAlgebra

private[playjson] trait SimpleJsonReaderInterpreter extends SimpleSchemaAlgebra[Reads] {
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

  override def bigIntSchema: Reads[BigInt] =
    Reads.bigDecReads.map(_.toBigInt())

  override def booleanSchema: Reads[Boolean] =
    Reads.BooleanReads

  override def stringSchema: Reads[String] =
    Reads.StringReads

}
