package io.github.methrat0n.restruct.schema

import enumeratum.{ Enum, EnumEntry }

trait SchemaEnum[E <: EnumEntry] { self: Enum[E] =>
  implicit val schema: Schema[E] = EnumeratumSchema.enum(self)
}
