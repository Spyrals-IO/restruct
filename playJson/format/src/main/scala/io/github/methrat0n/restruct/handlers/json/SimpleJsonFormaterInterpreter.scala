package io.github.methrat0n.restruct.handlers.json

import java.time.{ LocalDate, LocalTime, ZonedDateTime }

import play.api.libs.json.Format
import io.github.methrat0n.restruct.core.data.schema.SimpleSchemaAlgebra
import io.github.methrat0n.restruct.readers.json.SimpleJsonReaderInterpreter
import io.github.methrat0n.restruct.writers.json.SimpleJsonWriterInterpreter

trait SimpleJsonFormaterInterpreter extends SimpleSchemaAlgebra[Format] {

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

  override def dateTimeSchema: Format[ZonedDateTime] =
    Format(
      SimpleReader.dateTimeSchema,
      SimpleWriter.dateTimeSchema
    )

  override def timeSchema: Format[LocalTime] =
    Format(
      SimpleReader.timeSchema,
      SimpleWriter.timeSchema
    )

  override def dateSchema: Format[LocalDate] =
    Format(
      SimpleReader.dateSchema,
      SimpleWriter.dateSchema
    )
}
