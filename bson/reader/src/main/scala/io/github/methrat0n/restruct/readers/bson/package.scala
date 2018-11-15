package io.github.methrat0n.restruct.readers

import io.github.methrat0n.restruct.core.data.schema.{ IntStep, Path, StringStep }
import reactivemongo.bson.{ BSONDocument, BSONReader, BSONValue }

package object bson {
  type BsonReader[T] = BSONReader[BSONValue, T]

  object BsonReader {
    def apply[T](f: BSONValue => T): BsonReader[T] = BSONReader[BSONValue, T](f)
  }

  private[readers] def readDocumentWithDefault[T](path: Path, schema: BsonReader[T], default: Option[T]): BsonReader[Option[T]] = BsonReader[Option[T]] {
    case document: BSONDocument => default
      .map(defaultValue => Some(getAs(path, document, schema).getOrElse(defaultValue)))
      .getOrElse(getAs(path, document, schema))
    //(schema)
    case value => throw new RuntimeException(s"bsonvalue $value should be a BSONDocument")
  }

  private[this] def getAs[T](path: Path, document: BSONDocument, schema: BsonReader[T]): Option[T] =
    path.steps.toList.foldLeft(document.get(path.steps.head match {
      case StringStep(name) => name
      case IntStep(index)   => index.toString
    }))(
      (acc, step) => step match {
        case StringStep(name) => acc.flatMap {
          case document: BSONDocument => document.get(name)
          case _                      => throw new RuntimeException(s"invalid path : ${path.steps.toList}")
        }
        case IntStep(index) => acc.flatMap {
          case document: BSONDocument => document.get(index.toString)
          case _                      => throw new RuntimeException(s"invalid path : ${path.steps.toList}")
        }
      }
    ).map(_.as(schema))
}
