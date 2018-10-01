package restruct.bson.reader

import reactivemongo.bson.{ BSONReader, BSONValue }
import restruct.core.Program
import restruct.core.data.schema.ComplexSchemaAlgebra

object BsonReaderFormater extends ComplexBsonReaderInterpreter {
  def run[T](program: Program[ComplexSchemaAlgebra, T]): BSONReader[BSONValue, T] = program.run(this)
}
