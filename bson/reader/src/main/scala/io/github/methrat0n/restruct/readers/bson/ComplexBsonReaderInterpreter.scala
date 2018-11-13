package io.github.methrat0n.restruct.readers.bson

import io.github.methrat0n.restruct.core.data.schema.ComplexSchemaAlgebra
import reactivemongo.bson.DefaultBSONHandlers

trait ComplexBsonReaderInterpreter extends ComplexSchemaAlgebra[BsonReader] {
  override def many[T](schema: BsonReader[T]): BsonReader[List[T]] =
    DefaultBSONHandlers.bsonArrayToCollectionReader[List, T](implicitly, schema).asInstanceOf[BsonReader[List[T]]]
}
