package io.github.methrat0n.restruct.writers.jsonschema

import java.time.{ LocalDate, LocalTime, ZonedDateTime }

import io.github.methrat0n.restruct.core.data.schema.SimpleSchemaAlgebra
import io.github.methrat0n.restruct.writers.json.SimpleJsonWriterInterpreter
import play.api.libs.json.Json

trait SimpleJsonSchemaWriterInterpreter extends SimpleSchemaAlgebra[JsonSchemaWriter] {

  private object JsonWriter extends SimpleJsonWriterInterpreter

  override def charSchema: JsonSchemaWriter[Char] =
    (Json.obj(
      "type" -> "string"
    ), JsonWriter.charSchema) //TODO constraint, max size = 1

  override def byteSchema: JsonSchemaWriter[Byte] =
    (Json.obj(
      "type" -> "number" //TODO constaint, min -128 max 127
    ), JsonWriter.byteSchema)

  override def shortSchema: JsonSchemaWriter[Short] =
    (Json.obj(
      "type" -> "number" //TODO constaint, min -32768 max 32767
    ), JsonWriter.shortSchema)

  override def floatSchema: JsonSchemaWriter[Float] =
    (Json.obj(
      "type" -> "number"
    ), JsonWriter.floatSchema)

  override def decimalSchema: JsonSchemaWriter[Double] =
    (Json.obj(
      "type" -> "number"
    ), JsonWriter.decimalSchema)

  override def bigDecimalSchema: JsonSchemaWriter[BigDecimal] =
    (Json.obj(
      "type" -> "number"
    ), JsonWriter.bigDecimalSchema)

  override def integerSchema: JsonSchemaWriter[Int] =
    (Json.obj(
      "type" -> "integer"
    ), JsonWriter.integerSchema)

  override def longSchema: JsonSchemaWriter[Long] =
    (Json.obj(
      "type" -> "integer"
    ), JsonWriter.longSchema)

  override def bigIntSchema: JsonSchemaWriter[BigInt] =
    (Json.obj(
      "type" -> "integer"
    ), JsonWriter.bigIntSchema)

  override def booleanSchema: JsonSchemaWriter[Boolean] =
    (Json.obj(
      "type" -> "boolean"
    ), JsonWriter.booleanSchema)

  override def stringSchema: JsonSchemaWriter[String] =
    (Json.obj(
      "type" -> "string"
    ), JsonWriter.stringSchema)

  override def dateTimeSchema: JsonSchemaWriter[ZonedDateTime] =
    (Json.obj(
      "type" -> "date-time"
    ), JsonWriter.dateTimeSchema)

  override def timeSchema: JsonSchemaWriter[LocalTime] =
    (Json.obj(
      "type" -> "time"
    ), JsonWriter.timeSchema)

  override def dateSchema: JsonSchemaWriter[LocalDate] =
    (Json.obj(
      "type" -> "date"
    ), JsonWriter.dateSchema)
}
