package io.github.methrat0n.restruct.writers.jsonschema

import cats.data.Const
import play.api.libs.json.Json
import io.github.methrat0n.restruct.core.data.schema.SemiGroupalAlgebra
import io.github.methrat0n.restruct.writers.json.SemiGroupalJsonWriterInterpreter

trait SemiGroupalJsonSchemaWriterInterpreter extends SemiGroupalAlgebra[JsonSchemaWriter] {

  private object JsonWriterInterpreter extends SemiGroupalJsonWriterInterpreter

  override def either[A, B](schemaA: JsonSchemaWriter[A], schemaB: JsonSchemaWriter[B]): JsonSchemaWriter[Either[A, B]] = ((schemaA._1.getConst, schemaB._1.getConst) match {
    case (a, b) if a.value.contains("oneOf") && b.value.contains("oneOf") =>
      Const(deepMerge(a, b))
    case (a, b) if a.value.contains("oneOf") =>
      Const(deepMerge(a, Json.obj(
        "oneOf" -> Json.arr(b)
      )))
    case (a, b) if b.value.contains("oneOf") =>
      Const(deepMerge(Json.obj(
        "oneOf" -> Json.arr(a)
      ), b))
    case (a, b) =>
      Const(Json.obj(
        "oneOf" -> Json.arr(a, b)
      ))
  }, JsonWriterInterpreter.either(schemaA._2, schemaB._2))

  override def product[A, B](fa: JsonSchemaWriter[A], fb: JsonSchemaWriter[B]): JsonSchemaWriter[(A, B)] =
    (Const(deepMerge(fa._1.getConst, fb._1.getConst)), JsonWriterInterpreter.product(fa._2, fb._2))
}
