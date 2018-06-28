package lib.core.data.schema

import enumeratum.{Enum, EnumEntry}
import lib.core.Program

trait SchemaEnum[E <: EnumEntry] { self: Enum[E] =>
  implicit val schema: Program[SchemaAlgebra, E] = Schema.enum(self)
}

