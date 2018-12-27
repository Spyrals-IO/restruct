package io.github.methrat0n.restruct.readers.config

import io.github.methrat0n.restruct.schema.Schema
import play.api.ConfigLoader

object configLoader extends SimpleConfigInterpreter with ComplexConfigInterpreter with FieldConfigInterpreter {
  def run[T](schema: Schema[T]): ConfigLoader[T] = schema.bind(this)
}
