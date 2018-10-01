package restruct.enumeratum.data.schema

import enumeratum.{ Enum, EnumEntry }
import restruct.core.Program
import restruct.core.data.schema.ComplexSchemaAlgebra

trait SchemaEnum[E <: EnumEntry] { self: Enum[E] =>
  implicit val schema: Program[ComplexSchemaAlgebra, E] = EnumeratumSchema.enum(self)
}
