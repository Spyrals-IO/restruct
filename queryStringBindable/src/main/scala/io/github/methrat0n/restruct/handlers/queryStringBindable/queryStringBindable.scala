package io.github.methrat0n.restruct.handlers.queryStringBindable

import io.github.methrat0n.restruct.core.Program
import io.github.methrat0n.restruct.core.data.schema.SimpleSchemaAlgebra
import play.api.mvc.QueryStringBindable

object queryStringBindable extends SimpleQueryStringBindableInterpreter with ComplexConfigInterpreter with FieldConfigInterpreter {
  def run[T](program: Program[SimpleSchemaAlgebra, T]): QueryStringBindable[T] = program.run(this)
}
