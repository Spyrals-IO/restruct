package io.github.methrat0n.restruct.writers.bson

import reactivemongo.bson.BSONDocument
import io.github.methrat0n.restruct.core.data.schema.IdentityAlgebra

trait IdentityBsonWriterInterpreter extends IdentityAlgebra[BsonWriter] {
  override def pure[T](a: T): BsonWriter[T] = BsonWriter(
    _ => BSONDocument.empty
  )
}
