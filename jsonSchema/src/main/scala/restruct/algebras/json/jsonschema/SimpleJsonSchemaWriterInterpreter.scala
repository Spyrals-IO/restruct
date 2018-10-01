package restruct.algebras.json.jsonschema

import cats.data.Const
import play.api.libs.json.Json
import restruct.algebras.json.playjson.writes.SimpleJsonWriterInterpreter
import restruct.core.data.schema.SimpleSchemaAlgebra

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

}
