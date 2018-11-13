package io.github.methrat0n.restruct.writers.json

import play.api.libs.json._
import io.github.methrat0n.restruct.core.Program
import io.github.methrat0n.restruct.core.data.schema.SimpleSchemaAlgebra

object JsonWriterInterpreter extends FieldJsonWriterInterpreter with ComplexJsonWriterInterpreter with SimpleJsonWriterInterpreter {
  def run[T](program: Program[SimpleSchemaAlgebra, T]): Writes[T] = program.run(this)
}
