package restruct.bson.reader

import reactivemongo.bson.{ BSONDocument, DefaultBSONHandlers }
import restruct.core.data.constraints.Constraint
import restruct.core.data.schema.ComplexSchemaAlgebra

trait ComplexBsonReaderInterpreter extends ComplexSchemaAlgebra[BsonReader]
  with SimpleBsonReaderInterpreter with SemiGroupalBsonReaderInterpreter
  with InvariantBsonReaderInterpreter with IdentityBsonReaderInterpreter {

  override def many[T](name: String, schema: BsonReader[T], default: Option[List[T]]): BsonReader[List[T]] = {
    val listReader = DefaultBSONHandlers.bsonArrayToCollectionReader[List, T](implicitly, schema)
    readDocumentWithDefault(name, listReader.asInstanceOf[BsonReader[List[T]]], default).afterRead(_.getOrElse(List.empty))
  }

  override def optional[T](name: String, schema: BsonReader[T], default: Option[Option[T]]): BsonReader[Option[T]] =
    readDocumentWithDefault[T](name, schema, default.flatten)

  override def required[T](name: String, schema: BsonReader[T], default: Option[T]): BsonReader[T] =
    readDocumentWithDefault(name, schema, default).afterRead(_.get)

  override def verifying[T](schema: BsonReader[T], constraint: Constraint[T]): BsonReader[T] =
    schema.afterRead { parsed =>
      if (constraint.validate(parsed))
        parsed
      else
        throw new RuntimeException(s"Constraint ${constraint.name} check failed for $parsed")
    }

  private def readDocumentWithDefault[T](name: String, schema: BsonReader[T], default: Option[T]): BsonReader[Option[T]] = BsonReader[Option[T]] {
    case document: BSONDocument => default
      .map(defaultValue => Some(document.getAs(name)(schema).getOrElse(defaultValue)))
      .getOrElse(document.getAs(name)(schema))
    case value => throw new RuntimeException(s"bsonvalue $value should be a BSONDocument")
  }
}
