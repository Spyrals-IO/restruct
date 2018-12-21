package io.github.methrat0n.restruct.handlers.queryStringBindable

import io.github.methrat0n.restruct.core.data.schema.ComplexSchemaAlgebra
import play.api.mvc.QueryStringBindable

trait ComplexQueryStringBindableInterpreter extends ComplexSchemaAlgebra[QueryStringBindable] {
  override def many[T](schema: QueryStringBindable[T]): QueryStringBindable[List[T]] =
    QueryStringBindable.bindableList[T](schema)
}
