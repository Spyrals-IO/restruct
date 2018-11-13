package io.github.methrat0n.restruct.handlers.json

import io.github.methrat0n.restruct.core.data.schema.ComplexSchemaAlgebra
import io.github.methrat0n.restruct.readers.json.JsonReaderInterpreter
import io.github.methrat0n.restruct.writers.json.JsonWriterInterpreter
import play.api.libs.json.Format

trait ComplexJsonFormaterInterpreter extends ComplexSchemaAlgebra[Format] {

  private[this] val writer = JsonWriterInterpreter
  private[this] val reader = JsonReaderInterpreter

  override def many[T](schema: Format[T]): Format[List[T]] =
    Format(
      reader.many(schema),
      writer.many(schema)
    )
}
