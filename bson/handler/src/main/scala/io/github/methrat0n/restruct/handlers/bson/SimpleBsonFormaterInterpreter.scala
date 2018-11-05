package io.github.methrat0n.restruct.handlers.bson

import reactivemongo.bson.DefaultBSONHandlers
import io.github.methrat0n.restruct.core.data.schema.SimpleSchemaAlgebra
import io.github.methrat0n.restruct.readers.bson.SimpleBsonReaderInterpreter
import io.github.methrat0n.restruct.writers.bson.SimpleBsonWriterInterpreter

trait SimpleBsonFormaterInterpreter extends SimpleSchemaAlgebra[BsonHandler] {

  private[this] object Reader extends SimpleBsonReaderInterpreter
  private[this] object Writer extends SimpleBsonWriterInterpreter

  override def charSchema: BsonHandler[Char] =
    BsonHandler(
      Reader.charSchema.read,
      Writer.charSchema.write
    )

  override def byteSchema: BsonHandler[Byte] =
    BsonHandler(
      Reader.byteSchema.read,
      Writer.byteSchema.write
    )

  override def shortSchema: BsonHandler[Short] =
    BsonHandler(
      Reader.shortSchema.read,
      Writer.shortSchema.write
    )

  override def floatSchema: BsonHandler[Float] =
    BsonHandler(
      Reader.floatSchema.read,
      Writer.floatSchema.write
    )

  override def decimalSchema: BsonHandler[Double] =
    DefaultBSONHandlers.BSONDoubleHandler.asInstanceOf[BsonHandler[Double]]

  override def bigDecimalSchema: BsonHandler[BigDecimal] =
    DefaultBSONHandlers.BSONDecimalHandler.asInstanceOf[BsonHandler[BigDecimal]]

  override def integerSchema: BsonHandler[Int] =
    DefaultBSONHandlers.BSONIntegerHandler.asInstanceOf[BsonHandler[Int]]

  override def longSchema: BsonHandler[Long] =
    DefaultBSONHandlers.BSONLongHandler.asInstanceOf[BsonHandler[Long]]

  override def bigIntSchema: BsonHandler[BigInt] =
    BsonHandler(
      Reader.bigIntSchema.read,
      Writer.bigIntSchema.write
    )

  override def booleanSchema: BsonHandler[Boolean] =
    DefaultBSONHandlers.BSONBooleanHandler.asInstanceOf[BsonHandler[Boolean]]

  override def stringSchema: BsonHandler[String] =
    DefaultBSONHandlers.BSONStringHandler.asInstanceOf[BsonHandler[String]]

}
