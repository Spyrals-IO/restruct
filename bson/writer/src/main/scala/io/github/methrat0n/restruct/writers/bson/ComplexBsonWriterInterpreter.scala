package io.github.methrat0n.restruct.writers.bson

import io.github.methrat0n.restruct.core.data.schema.ComplexSchemaAlgebra
import reactivemongo.bson.DefaultBSONHandlers

trait ComplexBsonWriterInterpreter extends ComplexSchemaAlgebra[BsonWriter] {
  override def many[T](schema: BsonWriter[T]): BsonWriter[List[T]] = BsonWriter(
    DefaultBSONHandlers.collectionToBSONArrayCollectionWriter[T, List[T]](identity, schema).write
  )
}
