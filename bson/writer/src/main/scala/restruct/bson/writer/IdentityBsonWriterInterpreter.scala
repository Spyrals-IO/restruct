package restruct.bson.writer

import reactivemongo.bson.BSONDocument
import restruct.core.data.schema.IdentityAlgebra

trait IdentityBsonWriterInterpreter extends IdentityAlgebra[BsonWriter] {
  override def pure[T](a: T): BsonWriter[T] = BsonWriter(
    _ => BSONDocument.empty
  )
}
