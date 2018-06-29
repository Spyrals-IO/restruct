package restruct.core.data.schema

import restruct.core.Program

object Schema {
  def from[T](implicit schemaProvider: SchemaProvider[T]): Program[SchemaAlgebra, T] = schemaProvider()
}
