package restruct.enumeratum.data.schema

import enumeratum.{ Enum, EnumEntry }
import lib.core.Program
import lib.core.data.schema.SchemaAlgebra

trait SchemaEnum[E <: EnumEntry] { self: Enum[E] =>
  implicit val schema: Program[SchemaAlgebra, E] = EnumeratumSchema.enum(self)
}
