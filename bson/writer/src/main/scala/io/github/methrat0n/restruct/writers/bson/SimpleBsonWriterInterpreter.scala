package io.github.methrat0n.restruct.writers.bson

import java.time.temporal.ChronoField
import java.time.{ LocalDate, LocalTime, ZonedDateTime }

import io.github.methrat0n.restruct.core.data.schema.SimpleSchemaAlgebra
import reactivemongo.bson.{ BSONDateTime, BSONDecimal, BSONDouble, BSONInteger, BSONString, DefaultBSONHandlers }

trait SimpleBsonWriterInterpreter extends SimpleSchemaAlgebra[BsonWriter] {

  override def charSchema: BsonWriter[Char] =
    BsonWriter(char => BSONString(char.toString))

  override def byteSchema: BsonWriter[Byte] =
    BsonWriter(byte => BSONInteger(byte.toInt))

  override def shortSchema: BsonWriter[Short] =
    BsonWriter(short => BSONInteger(short.toInt))

  override def floatSchema: BsonWriter[Float] =
    BsonWriter(float => BSONDouble(float.toDouble))

  override def decimalSchema: BsonWriter[Double] =
    DefaultBSONHandlers.BSONDoubleHandler

  override def bigDecimalSchema: BsonWriter[BigDecimal] =
    DefaultBSONHandlers.BSONDecimalHandler

  override def integerSchema: BsonWriter[Int] =
    DefaultBSONHandlers.BSONIntegerHandler

  override def longSchema: BsonWriter[Long] =
    DefaultBSONHandlers.BSONLongHandler

  override def bigIntSchema: BsonWriter[BigInt] =
    BsonWriter(bigInt => BSONDecimal.fromBigDecimal(BigDecimal(bigInt)).get)

  override def booleanSchema: BsonWriter[Boolean] =
    DefaultBSONHandlers.BSONBooleanHandler

  override def stringSchema: BsonWriter[String] =
    DefaultBSONHandlers.BSONStringHandler

  override def dateTimeSchema: BsonWriter[ZonedDateTime] =
    BsonWriter(zoned => BSONDateTime(zoned.get(ChronoField.OFFSET_SECONDS)))

  override def timeSchema: BsonWriter[LocalTime] =
    BsonWriter(local => BSONDateTime(local.getLong(ChronoField.SECOND_OF_DAY)))

  override def dateSchema: BsonWriter[LocalDate] =
    BsonWriter(date => BSONDateTime(date.getLong(ChronoField.EPOCH_DAY) * 86400)) //Number of second in one day
}
