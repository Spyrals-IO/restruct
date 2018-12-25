package io.github.methrat0n.restruct.handlers.bson

import io.github.methrat0n.restruct.schema.Schema
import reactivemongo.bson.{ BSONHandler, BSONValue }

object BsonFormaterInterpreter extends SimpleBsonFormaterInterpreter with ComplexBsonFormaterInterpreter with FieldBsonFormaterInterpreter {
  def run[T](program: Schema[T]): BSONHandler[BSONValue, T] = program.bind(this)
}
