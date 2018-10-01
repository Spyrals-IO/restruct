package restruct.bson.reader

import restruct.core.data.schema.IdentityAlgebra

trait IdentityBsonReaderInterpreter extends IdentityAlgebra[BsonReader] {
  override def pure[T](t: T): BsonReader[T] =
    BsonReader[T](_ => t)
}
