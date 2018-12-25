package io.github.methrat0n.restruct.writers.jsonschema

import io.github.methrat0n.restruct.schema.Schema
import play.api.libs.json._

object JsonSchemaWriterInterpreter extends SimpleJsonSchemaWriterInterpreter with ComplexJsonSchemaWriterInterpreter with FieldJsonSchemaWriterInterpreter {
  def run[T](schema: Schema[T]): JsValue = schema.bind(this)._1
}
