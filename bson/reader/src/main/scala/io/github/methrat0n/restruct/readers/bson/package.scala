package io.github.methrat0n.restruct.readers

import reactivemongo.bson.{ BSONReader, BSONValue }

package object bson {
  type BsonReader[T] = BSONReader[BSONValue, T]

  object BsonReader {
    def apply[T](f: BSONValue => T): BsonReader[T] = BSONReader[BSONValue, T](f)
  }
}
