package io.github.methrat0n.restruct.core.data.schema

import io.github.methrat0n.restruct.core.Program

object Schema {
  def from[T](implicit schemaProvider: SchemaProvider[T]): Program[ComplexSchemaAlgebra, T] = schemaProvider()
}
