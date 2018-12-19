package io.github.methrat0n.restruct.writers.json

import play.api.libs.json._
import io.github.methrat0n.restruct.schema.Schema

object JsonWriterInterpreter extends FieldJsonWriterInterpreter with ComplexJsonWriterInterpreter with SimpleJsonWriterInterpreter {
  def run[T](program: Schema[T]): Writes[T] = program.bind(this)
}
