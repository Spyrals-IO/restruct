package io.github.methrat0n.restruct.handlers.bson

import reactivemongo.bson.{ BSONHandler, BSONValue }
import io.github.methrat0n.restruct.core.Program
import io.github.methrat0n.restruct.core.data.schema.ComplexSchemaAlgebra

object BsonFormaterInterpreter extends ComplexBsonFormaterInterpreter {
  def run[T](program: Program[ComplexSchemaAlgebra, T]): BSONHandler[BSONValue, T] = program.run(this)
}
