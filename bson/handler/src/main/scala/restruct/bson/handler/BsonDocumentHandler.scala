package restruct.bson.handler

import reactivemongo.bson.{ BSONDocument, BSONDocumentHandler }
import restruct.core.Program
import restruct.core.data.schema.ComplexSchemaAlgebra

object BsonDocumentHandler {
  def run[T](program: Program[ComplexSchemaAlgebra, T]): BSONDocumentHandler[T] = {
    val handler = BsonFormaterInterpreter.run(program)
    BSONDocumentHandler[T](
      handler.read, handler.write _ andThen {
        case document: BSONDocument => document
        case _                      => throw new RuntimeException("cannot write a bson document")
      }
    )
  }
}
