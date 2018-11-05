package io.github.methrat0n.restruct.writers.jsonschema

import java.time.{ LocalDate, LocalTime, ZonedDateTime }

import cats.data.Const
import io.github.methrat0n.restruct.core.data.schema.SimpleSchemaAlgebra
import io.github.methrat0n.restruct.writers.json.SimpleJsonWriterInterpreter
import play.api.libs.json.Json

trait SimpleJsonSchemaWriterInterpreter extends SimpleSchemaAlgebra[JsonSchemaWriter] {

  private object JsonWriter extends SimpleJsonWriterInterpreter

  override def charSchema: JsonSchemaWriter[Char] =
    (Const(Json.obj(
      "type" -> "string"
    )), JsonWriter.charSchema) //TODO constraint, max size = 1

  override def byteSchema: JsonSchemaWriter[Byte] =
    (Const(Json.obj(
      "type" -> "number" //TODO constaint, min -128 max 127
    )), JsonWriter.byteSchema)

  override def shortSchema: JsonSchemaWriter[Short] =
    (Const(Json.obj(
      "type" -> "number" //TODO constaint, min -32768 max 32767
    )), JsonWriter.shortSchema)

  override def floatSchema: JsonSchemaWriter[Float] =
    (Const(Json.obj(
      "type" -> "number"
    )), JsonWriter.floatSchema)

  override def decimalSchema: JsonSchemaWriter[Double] =
    (Const(Json.obj(
      "type" -> "number"
    )), JsonWriter.decimalSchema)

  override def bigDecimalSchema: JsonSchemaWriter[BigDecimal] =
    (Const(Json.obj(
      "type" -> "number"
    )), JsonWriter.bigDecimalSchema)

  override def integerSchema: JsonSchemaWriter[Int] =
    (Const(Json.obj(
      "type" -> "integer"
    )), JsonWriter.integerSchema)

  override def longSchema: JsonSchemaWriter[Long] =
    (Const(Json.obj(
      "type" -> "integer"
    )), JsonWriter.longSchema)

  override def bigIntSchema: JsonSchemaWriter[BigInt] =
    (Const(Json.obj(
      "type" -> "integer"
    )), JsonWriter.bigIntSchema)

  override def booleanSchema: JsonSchemaWriter[Boolean] =
    (Const(Json.obj(
      "type" -> "boolean"
    )), JsonWriter.booleanSchema)

  override def stringSchema: JsonSchemaWriter[String] =
    (Const(Json.obj(
      "type" -> "string"
    )), JsonWriter.stringSchema)

  override def dateTimeSchema: JsonSchemaWriter[ZonedDateTime] =
    (Const(Json.obj(
      "type" -> "date-time"
    )), JsonWriter.dateTimeSchema)

  override def timeSchema: JsonSchemaWriter[LocalTime] =
    (Const(Json.obj(
      "type" -> "time"
    )), JsonWriter.timeSchema)

  override def dateSchema: JsonSchemaWriter[LocalDate] =
    (Const(Json.obj(
      "type" -> "date"
    )), JsonWriter.dateSchema)
}
