package io.github.methrat0n.restruct.readers

import io.github.methrat0n.restruct.core.data.schema.{ IntStep, Path, StringStep }
import reactivemongo.bson.{ BSONArray, BSONDocument, BSONElementSet, BSONReader, BSONValue }

package object bson {
  type BsonReader[T] = BSONReader[BSONValue, T]

  object BsonReader {
    def apply[T](f: BSONValue => T): BsonReader[T] = BSONReader[BSONValue, T](f)
  }

  private[readers] def readDocumentWithDefault[T](path: Path, schema: BsonReader[T], default: Option[T]): BsonReader[Option[T]] = BsonReader[Option[T]] {
    case bson: BSONValue with BSONElementSet => default
      .map(defaultValue => Some(getAs(path, bson, schema).getOrElse(defaultValue)))
      .getOrElse(getAs(path, bson, schema))
    case value => throw new RuntimeException(s"bsonvalue $value should be a BSONDocument")
  }

  private[this] def getAs[T](path: Path, bson: BSONElementSet, schema: BsonReader[T]): Option[T] =
    path.steps.toList.tail.foldLeft(path.steps.head match {
      case StringStep(name) => bson match {
        case document: BSONDocument => document.get(name)
        case _                      => throw new RuntimeException(s"invalid path : ${path.steps.toList}")
      }
      case IntStep(index) => bson match {
        case arr: BSONArray => arr.get(index.toString)
        case _              => throw new RuntimeException(s"invalid path : ${path.steps.toList}")
      }
    })(
      (acc, step) => step match {
        case StringStep(name) => acc.flatMap {
          case document: BSONDocument => document.get(name)
          case _                      => throw new RuntimeException(s"invalid path : ${path.steps.toList}")
        }
        case IntStep(index) => acc.flatMap {
          case arr: BSONArray => arr.get(index.toString)
          case _              => throw new RuntimeException(s"invalid path : ${path.steps.toList}")
        }
      }
    ).map(_.as(schema))
}
