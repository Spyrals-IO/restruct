package restruct.algebras.json.playjson.format

import play.api.libs.json.Format
import restruct.algebras.json.playjson.reads.SimpleJsonReaderInterpreter
import restruct.algebras.json.playjson.writes.SimpleJsonWriterInterpreter
import restruct.core.data.schema.SimpleSchemaAlgebra

private[format] trait SimpleJsonFormaterInterpreter extends SimpleSchemaAlgebra[Format] {

  private[this] object SimpleReader extends SimpleJsonReaderInterpreter
  private[this] object SimpleWriter extends SimpleJsonWriterInterpreter

  override def charSchema: Format[Char] =
    Format(
      SimpleReader.charSchema,
      SimpleWriter.charSchema
    )

  override def byteSchema: Format[Byte] =
    Format(
      SimpleReader.byteSchema,
      SimpleWriter.byteSchema
    )

  override def shortSchema: Format[Short] =
    Format(
      SimpleReader.shortSchema,
      SimpleWriter.shortSchema
    )

  override def floatSchema: Format[Float] =
    Format(
      SimpleReader.floatSchema,
      SimpleWriter.floatSchema
    )

  override def decimalSchema: Format[Double] =
    Format(
      SimpleReader.decimalSchema,
      SimpleWriter.decimalSchema
    )

  override def bigDecimalSchema: Format[BigDecimal] =
    Format(
      SimpleReader.bigDecimalSchema,
      SimpleWriter.bigDecimalSchema
    )

  override def integerSchema: Format[Int] =
    Format(
      SimpleReader.integerSchema,
      SimpleWriter.integerSchema
    )

  override def longSchema: Format[Long] =
    Format(
      SimpleReader.longSchema,
      SimpleWriter.longSchema
    )

  override def bigIntSchema: Format[BigInt] =
    Format(
      SimpleReader.bigIntSchema,
      SimpleWriter.bigIntSchema
    )

  override def booleanSchema: Format[Boolean] =
    Format(
      SimpleReader.booleanSchema,
      SimpleWriter.booleanSchema
    )

  override def stringSchema: Format[String] =
    Format(
      SimpleReader.stringSchema,
      SimpleWriter.stringSchema
    )

}
