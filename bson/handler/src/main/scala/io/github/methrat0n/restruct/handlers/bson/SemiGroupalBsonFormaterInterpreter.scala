package io.github.methrat0n.restruct.handlers.bson

import io.github.methrat0n.restruct.core.data.schema.SemiGroupalAlgebra
import io.github.methrat0n.restruct.readers.bson.SemiGroupalBsonReaderInterpreter
import io.github.methrat0n.restruct.writers.bson.SemiGroupalBsonWriterInterpreter

trait SemiGroupalBsonFormaterInterpreter extends SemiGroupalAlgebra[BsonHandler] {

  private[this] object Reader extends SemiGroupalBsonReaderInterpreter
  private[this] object Writer extends SemiGroupalBsonWriterInterpreter

  override def either[A, B](a: BsonHandler[A], b: BsonHandler[B]): BsonHandler[Either[A, B]] =
    BsonHandler(
      Reader.either(a, b).read,
      Writer.either(a, b).write
    )

  override def product[A, B](fa: BsonHandler[A], fb: BsonHandler[B]): BsonHandler[(A, B)] =
    BsonHandler(
      Reader.product(fa, fb).read,
      Writer.product(fa, fb).write
    )

}
