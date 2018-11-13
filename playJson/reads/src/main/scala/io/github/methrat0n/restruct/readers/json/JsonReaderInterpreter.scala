package io.github.methrat0n.restruct.readers.json

import play.api.libs.json.Reads
import io.github.methrat0n.restruct.core.Program
import io.github.methrat0n.restruct.core.data.schema.SimpleSchemaAlgebra

object JsonReaderInterpreter extends SimpleJsonReaderInterpreter with ComplexJsonReaderInterpreter with FieldJsonReaderInterpreter {
  def run[T](program: Program[SimpleSchemaAlgebra, T]): Reads[T] = program.run(this)
}
