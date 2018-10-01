package restruct.bson.handler

import restruct.bson.reader.IdentityBsonReaderInterpreter
import restruct.bson.writer.IdentityBsonWriterInterpreter
import restruct.core.data.schema.IdentityAlgebra

trait IdentityBsonFormaterInterpreter extends IdentityAlgebra[BsonHandler] {

  private[this] object Reader extends IdentityBsonReaderInterpreter
  private[this] object Writer extends IdentityBsonWriterInterpreter

  override def pure[T](t: T): BsonHandler[T] =
    BsonHandler(
      Reader.pure(t).read,
      Writer.pure(t).write
    )
}
