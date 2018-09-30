package restruct.bson.handler

import reactivemongo.bson.{BSONDocument, BSONDocumentHandler}
import restruct.core.Program
import restruct.core.data.schema.SchemaAlgebra


object BsonDocumentHandler {
  def run[T](program: Program[SchemaAlgebra, T]): BSONDocumentHandler[T] = {
    val handler = BsonHandler.run(program)
    BSONDocumentHandler[T](
      handler.read, handler.write _ andThen {
        case document: BSONDocument => document
        case _                      => throw new RuntimeException("cannot write a bson document")
      }
    )
  }
}
