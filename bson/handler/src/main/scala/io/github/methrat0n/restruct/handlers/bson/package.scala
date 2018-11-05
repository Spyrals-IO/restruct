package io.github.methrat0n.restruct.handlers

import reactivemongo.bson.{ BSONHandler, BSONValue }

package object bson {
  type BsonHandler[T] = BSONHandler[BSONValue, T]

  object BsonHandler {
    def apply[T](read: BSONValue => T, write: T => BSONValue): BsonHandler[T] = BSONHandler[BSONValue, T](read, write)
  }
}
