package restruct.bson.handler

import restruct.bson.reader.InvariantBsonReaderInterpreter
import restruct.bson.writer.InvariantBsonWriterInterpreter
import restruct.core.data.schema.InvariantAlgebra

trait InvariantBsonFormaterInterpreter extends InvariantAlgebra[BsonHandler] {

  private[this] object Reader extends InvariantBsonReaderInterpreter
  private[this] object Writer extends InvariantBsonWriterInterpreter

  override def imap[A, B](fa: BsonHandler[A])(f: A => B)(g: B => A): BsonHandler[B] =
    BsonHandler(
      Reader.imap(fa)(f)(g).read,
      Writer.imap(fa)(f)(g).write
    )
}
