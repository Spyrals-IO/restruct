package io.github.methrat0n.restruct.readers.config

import io.github.methrat0n.restruct.core.Program
import io.github.methrat0n.restruct.core.data.schema.SimpleSchemaAlgebra
import play.api.ConfigLoader

object configLoader extends SimpleConfigInterpreter with ComplexConfigInterpreter with FieldConfigInterpreter {
  def run[T](program: Program[SimpleSchemaAlgebra, T]): ConfigLoader[T] = program.run(this)
}
