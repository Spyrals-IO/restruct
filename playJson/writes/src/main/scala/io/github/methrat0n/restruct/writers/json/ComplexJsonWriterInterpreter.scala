package io.github.methrat0n.restruct.writers.json

import io.github.methrat0n.restruct.core.data.schema.ComplexSchemaAlgebra
import play.api.libs.json.Writes

trait ComplexJsonWriterInterpreter extends ComplexSchemaAlgebra[Writes] {

  override def many[T](schema: Writes[T]): Writes[Seq[T]] =
    Writes.traversableWrites(schema)
}
