package io.github.methrat0n.restruct.writers.jsonschema

import cats.data.Const
import play.api.libs.json._
import io.github.methrat0n.restruct.core.data.constraints.Constraint
import io.github.methrat0n.restruct.core.data.schema.ComplexSchemaAlgebra
import io.github.methrat0n.restruct.writers.json.ComplexJsonWriterInterpreter

trait ComplexJsonSchemaWriterInterpreter extends ComplexSchemaAlgebra[JsonSchemaWriter]
  with SimpleJsonSchemaWriterInterpreter with InvariantJsonSchemaWriterInterpreter
  with IdentityJsonSchemaWriterInterpreter with SemiGroupalJsonSchemaWriterInterpreter {

  private object JsonWriter extends ComplexJsonWriterInterpreter

  override def many[T](name: String, schema: JsonSchemaWriter[T], default: Option[List[T]]): JsonSchemaWriter[List[T]] =
    (Const(Json.obj(
      "type" -> "object",
      "properties" -> Json.obj(
        name -> (Json.obj(
          "type" -> "array",
          "items" -> schema._1.getConst,
        ) ++ default.map( default =>
          Json.obj("default" -> Writes.traversableWrites(schema._2).writes(default))
        ).getOrElse(Json.obj()))
      ),
      "required" -> Json.arr()
    )), JsonWriter.many(name, schema._2, default))

  override def optional[T](name: String, schema: JsonSchemaWriter[T], default: Option[Option[T]]): JsonSchemaWriter[Option[T]] =
    (Const(Json.obj(
      "type" -> "object",
      "properties" -> Json.obj(
        name -> (schema._1.getConst ++ default.flatten.map( default =>
          Json.obj("default" -> schema._2.writes(default))
        ).getOrElse(Json.obj()))
      ),
      "required" -> Json.arr()
    )), JsonWriter.optional(name, schema._2, default))

  override def required[T](name: String, schema: JsonSchemaWriter[T], default: Option[T]): JsonSchemaWriter[T] =
    (Const(Json.obj(
      "type" -> "object",
      "properties" -> Json.obj(
        name -> (schema._1.getConst ++ default.map( default =>
          Json.obj("default" -> schema._2.writes(default))
        ).getOrElse(Json.obj()))
      ),
      "required" -> Json.arr(name)
    )), JsonWriter.required(name, schema._2, default))

  override def verifying[T](schema: JsonSchemaWriter[T], constraint: Constraint[T]): JsonSchemaWriter[T] =
    (Const(schema._1.getConst ++ Json.obj(
      constraint.name -> writeArgs(constraint.args)
    )), JsonWriter.verifying(schema._2, constraint))

  private def writeArgs(args: Any): JsValue = args match {
    case Seq(item)        => writeArgs(item)
    case seq @ Seq(_, _*) => JsArray(seq.map(writeArgs))
    case number: Number   => Json.toJson(BigDecimal(number.toString))
    case boolean: Boolean => Json.toJson(boolean)
    case other            => JsString(other.toString)
  }
}
