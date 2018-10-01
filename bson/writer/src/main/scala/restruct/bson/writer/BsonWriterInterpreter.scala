package restruct.bson.writer

import restruct.core.Program
import restruct.core.data.schema.SimpleSchemaAlgebra

object BsonWriterInterpreter extends ComplexBsonWriterInterpreter {
  def run[T](program: Program[SimpleSchemaAlgebra, T]): BsonWriter[T] = program.run[BsonWriter](this)
}
