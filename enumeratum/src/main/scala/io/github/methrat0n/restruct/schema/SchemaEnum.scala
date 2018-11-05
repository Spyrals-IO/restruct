package io.github.methrat0n.restruct.schema

import enumeratum.{ Enum, EnumEntry }
import io.github.methrat0n.restruct.core.Program
import io.github.methrat0n.restruct.core.data.schema.ComplexSchemaAlgebra

trait SchemaEnum[E <: EnumEntry] { self: Enum[E] =>
  implicit val schema: Program[ComplexSchemaAlgebra, E] = EnumeratumSchema.enum(self)
}
