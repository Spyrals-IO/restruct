package io.github.methrat0n.restruct.handlers.json

import play.api.libs.json.Format
import io.github.methrat0n.restruct.schema.Schema

object JsonFormaterInterpreter extends SimpleJsonFormaterInterpreter with ComplexJsonFormaterInterpreter with FieldJsonFormaterInterpreter {
  def run[T](program: Schema[T]): Format[T] = program.bind(this)
}
