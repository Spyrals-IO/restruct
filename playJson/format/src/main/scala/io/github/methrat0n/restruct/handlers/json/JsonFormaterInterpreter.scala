package io.github.methrat0n.restruct.handlers.json

import play.api.libs.json.Format
import io.github.methrat0n.restruct.core.Program
import io.github.methrat0n.restruct.core.data.schema.SimpleSchemaAlgebra

object JsonFormaterInterpreter extends ComplexJsonFormaterInterpreter {
  def run[T](program: Program[SimpleSchemaAlgebra, T]): Format[T] = program.run(this)
}
