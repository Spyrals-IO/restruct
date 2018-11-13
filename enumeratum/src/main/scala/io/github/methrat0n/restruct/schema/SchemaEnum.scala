package io.github.methrat0n.restruct.schema

import enumeratum.{ Enum, EnumEntry }
import io.github.methrat0n.restruct.core.Program
import io.github.methrat0n.restruct.core.data.schema.FieldAlgebra

trait SchemaEnum[E <: EnumEntry] { self: Enum[E] =>
  implicit val schema: Program[FieldAlgebra, E] = EnumeratumSchema.enum(self)
}
