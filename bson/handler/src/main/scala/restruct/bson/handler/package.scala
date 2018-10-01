package restruct.bson

import reactivemongo.bson.{ BSONHandler, BSONValue }

package object handler {
  type BsonHandler[T] = BSONHandler[BSONValue, T]

  object BsonHandler {
    def apply[T](read: BSONValue => T, write: T => BSONValue): BsonHandler[T] = BSONHandler[BSONValue, T](read, write)
  }
}
