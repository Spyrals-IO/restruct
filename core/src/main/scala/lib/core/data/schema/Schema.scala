package lib.core.data.schema

import lib.core.Program

object Schema {
  def from[T](implicit schemaProvider: SchemaProvider[T]): Program[SchemaAlgebra, T] = schemaProvider()
}
