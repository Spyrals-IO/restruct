package io.github.methrat0n.restruct.handlers.bson

import io.github.methrat0n.restruct.core.data.schema.InvariantAlgebra
import io.github.methrat0n.restruct.readers.bson.InvariantBsonReaderInterpreter
import io.github.methrat0n.restruct.writers.bson.InvariantBsonWriterInterpreter

trait InvariantBsonFormaterInterpreter extends InvariantAlgebra[BsonHandler] {

  private[this] object Reader extends InvariantBsonReaderInterpreter
  private[this] object Writer extends InvariantBsonWriterInterpreter

  override def imap[A, B](fa: BsonHandler[A])(f: A => B)(g: B => A): BsonHandler[B] =
    BsonHandler(
      Reader.imap(fa)(f)(g).read,
      Writer.imap(fa)(f)(g).write
    )
}
