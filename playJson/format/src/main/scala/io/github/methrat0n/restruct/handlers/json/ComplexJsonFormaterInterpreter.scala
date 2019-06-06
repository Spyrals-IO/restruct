package io.github.methrat0n.restruct.handlers.json

import io.github.methrat0n.restruct.core.data.schema.ComplexSchemaAlgebra
import io.github.methrat0n.restruct.writers.json.jsonWrites
import play.api.libs.json.Format

trait ComplexJsonFormaterInterpreter extends ComplexSchemaAlgebra[Format] {

  private[this] val writer = jsonWrites
  private[this] val reader = jsonReads

  override def many[T](schema: Format[T]): Format[List[T]] =
    Format(
      reader.many(schema),
      writer.many(schema)
    )
}
