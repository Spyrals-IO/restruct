package restruct.bson

import reactivemongo.bson.{ BSONValue, BSONWriter }

package object writer {
  type BsonWriter[T] = BSONWriter[T, _ <: BSONValue]

  object BsonWriter {
    def apply[T, B <: BSONValue](f: T => B): BsonWriter[T] = BSONWriter[T, B](f)
  }
}
