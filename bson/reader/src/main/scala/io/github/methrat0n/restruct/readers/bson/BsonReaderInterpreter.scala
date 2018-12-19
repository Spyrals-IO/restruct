package io.github.methrat0n.restruct.readers.bson

import reactivemongo.bson.{ BSONReader, BSONValue }
import io.github.methrat0n.restruct.schema.Schema

object BsonReaderInterpreter extends SimpleBsonReaderInterpreter with ComplexBsonReaderInterpreter with FieldBsonReaderInterpreter {
  def run[T](program: Schema[T]): BSONReader[BSONValue, T] = program.bind(this)
}
