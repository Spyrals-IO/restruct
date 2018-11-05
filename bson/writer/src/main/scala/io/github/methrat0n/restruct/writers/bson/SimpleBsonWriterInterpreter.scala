package io.github.methrat0n.restruct.writers.bson

import reactivemongo.bson.{ BSONDecimal, BSONDouble, BSONInteger, BSONString, DefaultBSONHandlers }
import io.github.methrat0n.restruct.core.data.schema.SimpleSchemaAlgebra

trait SimpleBsonWriterInterpreter extends SimpleSchemaAlgebra[BsonWriter] {

  override def charSchema: BsonWriter[Char] =
    BsonWriter(char => BSONString.apply(char.toString))

  override def byteSchema: BsonWriter[Byte] =
    BsonWriter(byte => BSONInteger.apply(byte.toInt))

  override def shortSchema: BsonWriter[Short] =
    BsonWriter(short => BSONInteger.apply(short.toInt))

  override def floatSchema: BsonWriter[Float] =
    BsonWriter(float => BSONDouble.apply(float.toDouble))

  override def decimalSchema: BsonWriter[Double] =
    DefaultBSONHandlers.BSONDoubleHandler

  override def bigDecimalSchema: BsonWriter[BigDecimal] =
    DefaultBSONHandlers.BSONDecimalHandler

  override def integerSchema: BsonWriter[Int] =
    DefaultBSONHandlers.BSONIntegerHandler

  override def longSchema: BsonWriter[Long] =
    DefaultBSONHandlers.BSONLongHandler

  override def bigIntSchema: BsonWriter[BigInt] =
    BsonWriter(bigInt => BSONDecimal.parse(bigInt.toString).get)

  override def booleanSchema: BsonWriter[Boolean] =
    DefaultBSONHandlers.BSONBooleanHandler

  override def stringSchema: BsonWriter[String] =
    DefaultBSONHandlers.BSONStringHandler
}
