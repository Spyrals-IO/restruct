package io.github.methrat0n.restruct.writers

import reactivemongo.bson.{ BSONValue, BSONWriter }

package object bson {
  type BsonWriter[T] = BSONWriter[T, _ <: BSONValue]

  object BsonWriter {
    def apply[T, B <: BSONValue](f: T => B): BsonWriter[T] = BSONWriter[T, B](f)
  }
}
