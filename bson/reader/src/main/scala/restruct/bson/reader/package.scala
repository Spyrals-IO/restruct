package restruct.bson

import reactivemongo.bson.{ BSONReader, BSONValue }

package object reader {
  type BsonReader[T] = BSONReader[BSONValue, T]

  object BsonReader {
    def apply[T](f: BSONValue => T): BsonReader[T] = BSONReader[BSONValue, T](f)
  }
}
