package io.github.methrat0n.restruct.writers.bson

import io.github.methrat0n.restruct.schema.Schema

object BsonWriterInterpreter extends SimpleBsonWriterInterpreter with ComplexBsonWriterInterpreter with FieldBsonWriterInterpreter {
  def run[T](program: Schema[T]): BsonWriter[T] = program.bind[BsonWriter](this)
}
