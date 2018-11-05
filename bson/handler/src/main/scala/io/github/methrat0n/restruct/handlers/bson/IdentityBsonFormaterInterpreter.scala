package io.github.methrat0n.restruct.handlers.bson

import io.github.methrat0n.restruct.core.data.schema.IdentityAlgebra
import io.github.methrat0n.restruct.readers.bson.IdentityBsonReaderInterpreter
import io.github.methrat0n.restruct.writers.bson.IdentityBsonWriterInterpreter

trait IdentityBsonFormaterInterpreter extends IdentityAlgebra[BsonHandler] {

  private[this] object Reader extends IdentityBsonReaderInterpreter
  private[this] object Writer extends IdentityBsonWriterInterpreter

  override def pure[T](t: T): BsonHandler[T] =
    BsonHandler(
      Reader.pure(t).read,
      Writer.pure(t).write
    )
}
