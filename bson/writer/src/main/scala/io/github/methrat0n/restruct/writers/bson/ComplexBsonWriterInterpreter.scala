package io.github.methrat0n.restruct.writers.bson

import reactivemongo.bson.{ BSONDocument, DefaultBSONHandlers }
import io.github.methrat0n.restruct.core.data.constraints.Constraint
import io.github.methrat0n.restruct.core.data.schema.ComplexSchemaAlgebra

trait ComplexBsonWriterInterpreter extends ComplexSchemaAlgebra[BsonWriter]
  with SimpleBsonWriterInterpreter with SemiGroupalBsonWriterInterpreter
  with InvariantBsonWriterInterpreter with IdentityBsonWriterInterpreter {

  override def many[T](name: String, schema: BsonWriter[T], default: Option[List[T]]): BsonWriter[List[T]] =
    BsonWriter(
      list => BSONDocument(
        name -> DefaultBSONHandlers.collectionToBSONArrayCollectionWriter[T, List[T]](identity, schema).write(list)
      )
    )

  override def optional[T](name: String, schema: BsonWriter[T], default: Option[Option[T]]): BsonWriter[Option[T]] =
    BsonWriter(
      option => BSONDocument(
        name -> option.map(schema.write)
      )
    )

  override def required[T](name: String, schema: BsonWriter[T], default: Option[T]): BsonWriter[T] =
    BsonWriter(
      required => BSONDocument(
        name -> schema.write(required)
      )
    )

  override def verifying[T](schema: BsonWriter[T], constraint: Constraint[T]): BsonWriter[T] =
    schema
}
