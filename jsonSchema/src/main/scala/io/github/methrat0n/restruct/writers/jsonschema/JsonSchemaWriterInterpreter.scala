package io.github.methrat0n.restruct.writers.jsonschema

import play.api.libs.json._
import io.github.methrat0n.restruct.core.Program
import io.github.methrat0n.restruct.core.data.schema.SimpleSchemaAlgebra

object JsonSchemaWriterInterpreter extends ComplexJsonSchemaWriterInterpreter {
  def run[T](program: Program[SimpleSchemaAlgebra, T]): JsValue = program.run(this)._1.getConst
}
