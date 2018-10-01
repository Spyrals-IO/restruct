package restruct.bson.handler

import reactivemongo.bson.{ BSONHandler, BSONValue }
import restruct.core.Program
import restruct.core.data.schema.ComplexSchemaAlgebra

object BsonFormaterInterpreter extends ComplexBsonFormaterInterpreter {
  def run[T](program: Program[ComplexSchemaAlgebra, T]): BSONHandler[BSONValue, T] = program.run(this)
}
