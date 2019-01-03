package io.github.methrat0n.restruct.writers.jsonschema

import io.github.methrat0n.restruct.core.data.schema.ComplexSchemaAlgebra
import io.github.methrat0n.restruct.writers.json.jsonWrites
import play.api.libs.json._

trait ComplexJsonSchemaWriterInterpreter extends ComplexSchemaAlgebra[JsonSchemaWriter] {

  private val writer = jsonWrites

  override def many[T](schema: JsonSchemaWriter[T]): JsonSchemaWriter[List[T]] =
    (Json.obj("type" -> "array", "items" -> schema._1), writer.many(schema._2))
}
