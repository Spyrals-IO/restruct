package io.github.methrat0n.restruct.readers.bson

import io.github.methrat0n.restruct.core.data.schema.IdentityAlgebra

trait IdentityBsonReaderInterpreter extends IdentityAlgebra[BsonReader] {
  override def pure[T](t: T): BsonReader[T] =
    BsonReader[T](_ => t)
}
