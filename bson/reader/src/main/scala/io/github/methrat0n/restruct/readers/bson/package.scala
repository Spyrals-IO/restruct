package io.github.methrat0n.restruct.readers

import reactivemongo.bson.{ BSONDocument, BSONReader, BSONValue }

package object bson {
  type BsonReader[T] = BSONReader[BSONValue, T]

  object BsonReader {
    def apply[T](f: BSONValue => T): BsonReader[T] = BSONReader[BSONValue, T](f)
  }

  private[readers] def readDocumentWithDefault[T](name: String, schema: BsonReader[T], default: Option[T]): BsonReader[Option[T]] = BsonReader[Option[T]] {
    case document: BSONDocument => default
      .map(defaultValue => Some(document.getAs(name)(schema).getOrElse(defaultValue)))
      .getOrElse(document.getAs(name)(schema))
    case value => throw new RuntimeException(s"bsonvalue $value should be a BSONDocument")
  }
}
