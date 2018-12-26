package io.github.methrat0n.restruct.handlers.queryStringBindable

import io.github.methrat0n.restruct.schema.Schema
import play.api.mvc.QueryStringBindable

object queryStringBindable extends SimpleQueryStringBindableInterpreter with ComplexQueryStringBindableInterpreter with FieldQueryStringBindableInterpreter {
  def run[T](program: Schema[T]): QueryStringBindable[T] = program.bind(this)
}
