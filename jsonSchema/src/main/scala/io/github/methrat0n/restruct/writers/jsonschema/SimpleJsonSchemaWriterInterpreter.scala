package io.github.methrat0n.restruct.writers.jsonschema

import java.time.{ LocalDate, LocalTime, ZonedDateTime }
import io.github.methrat0n.restruct.writers.json.SimpleJsonWriterInterpreter
import play.api.libs.json.Json

trait SimpleJsonSchemaWriterInterpreter extends SimpleSchemaAlgebra[JsonSchemaWriter] {

  private object JsonWriter extends SimpleJsonWriterInterpreter

  override def charSchema: JsonSchemaWriter[Char] = JsonSchemaWriter(
    Json.obj(
      "type" -> "string"
    ),
    JsonWriter.charSchema
  ) //TODO constraint, max size = 1

  override def byteSchema: JsonSchemaWriter[Byte] = JsonSchemaWriter(
    Json.obj(
      "type" -> "number"
    ),
    JsonWriter.byteSchema
  ) //TODO constaint, min -128 max 127

  override def shortSchema: JsonSchemaWriter[Short] = JsonSchemaWriter(
    Json.obj(
      "type" -> "number"
    ),
    JsonWriter.shortSchema
  ) //TODO constaint, min -32768 max 32767

  override def floatSchema: JsonSchemaWriter[Float] = JsonSchemaWriter(
    Json.obj(
      "type" -> "number"
    ),
    JsonWriter.floatSchema
  )

  override def decimalSchema: JsonSchemaWriter[Double] = JsonSchemaWriter(
    Json.obj(
      "type" -> "number"
    ), JsonWriter.decimalSchema
  )

  override def bigDecimalSchema: JsonSchemaWriter[BigDecimal] = JsonSchemaWriter(
    Json.obj(
      "type" -> "number"
    ),
    JsonWriter.bigDecimalSchema
  )

  override def integerSchema: JsonSchemaWriter[Int] = JsonSchemaWriter(
    Json.obj(
      "type" -> "integer"
    ),
    JsonWriter.integerSchema
  )

  override def longSchema: JsonSchemaWriter[Long] = JsonSchemaWriter(
    Json.obj(
      "type" -> "integer"
    ),
    JsonWriter.longSchema
  )

  override def bigIntSchema: JsonSchemaWriter[BigInt] = JsonSchemaWriter(
    Json.obj(
      "type" -> "integer"
    ),
    JsonWriter.bigIntSchema
  )

  override def booleanSchema: JsonSchemaWriter[Boolean] = JsonSchemaWriter(
    Json.obj(
      "type" -> "boolean"
    ),
    JsonWriter.booleanSchema
  )

  override def stringSchema: JsonSchemaWriter[String] = JsonSchemaWriter(
    Json.obj(
      "type" -> "string"
    ),
    JsonWriter.stringSchema
  )

  override def dateTimeSchema: JsonSchemaWriter[ZonedDateTime] = JsonSchemaWriter(
    Json.obj(
      "type" -> "date-time"
    ),
    JsonWriter.dateTimeSchema
  )

  override def timeSchema: JsonSchemaWriter[LocalTime] = JsonSchemaWriter(
    Json.obj(
      "type" -> "time"
    ),
    JsonWriter.timeSchema
  )

  override def dateSchema: JsonSchemaWriter[LocalDate] = JsonSchemaWriter(
    Json.obj(
      "type" -> "date"
    ),
    JsonWriter.dateSchema
  )
}
