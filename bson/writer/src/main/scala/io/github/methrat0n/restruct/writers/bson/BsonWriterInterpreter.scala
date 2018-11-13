package io.github.methrat0n.restruct.writers.bson

import io.github.methrat0n.restruct.core.Program
import io.github.methrat0n.restruct.core.data.schema.SimpleSchemaAlgebra

object BsonWriterInterpreter extends SimpleBsonWriterInterpreter with ComplexBsonWriterInterpreter with FieldBsonWriterInterpreter {
  def run[T](program: Program[SimpleSchemaAlgebra, T]): BsonWriter[T] = program.run[BsonWriter](this)
}
