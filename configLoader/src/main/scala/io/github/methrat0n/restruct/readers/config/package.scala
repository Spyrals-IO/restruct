package io.github.methrat0n.restruct.readers

import io.github.methrat0n.restruct.core.Program
import io.github.methrat0n.restruct.core.data.schema.SimpleSchemaAlgebra
import play.api.ConfigLoader

package object config extends SimpleConfigInterpreter with ComplexConfigInterpreter with FieldConfigInterpreter {
  def run[T](program: Program[SimpleSchemaAlgebra, T]): ConfigLoader[T] = program.run(this)
}
