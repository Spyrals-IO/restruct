package io.github.methrat0n.restruct.handlers.bson

import io.github.methrat0n.restruct.schema.Schema
import reactivemongo.bson.{ BSONDocument, BSONDocumentHandler }

object BsonDocumentHandler {
  def run[T](program: Schema[T]): BSONDocumentHandler[T] = {
    val handler = BsonFormaterInterpreter.run(program)
    BSONDocumentHandler[T](
      handler.read, handler.write _ andThen {
        case document: BSONDocument => document
        case _                      => throw new RuntimeException("cannot write a bson document")
      }
    )
  }
}
