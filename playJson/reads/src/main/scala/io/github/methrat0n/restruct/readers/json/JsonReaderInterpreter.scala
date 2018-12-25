package io.github.methrat0n.restruct.readers.json

import play.api.libs.json.Reads
import io.github.methrat0n.restruct.schema.Schema

object JsonReaderInterpreter extends SimpleJsonReaderInterpreter with ComplexJsonReaderInterpreter with FieldJsonReaderInterpreter {
  def run[T](program: Schema[T]): Reads[T] = program.bind(this)
}
