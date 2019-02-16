package io.github.methrat0n.restruct.readers.bson

import java.time.format.DateTimeFormatter
import java.time._

import reactivemongo.bson.{ BSONBoolean, BSONDateTime, BSONDecimal, BSONDouble, BSONInteger, BSONLong, BSONString }
import io.github.methrat0n.restruct.core.data.schema.SimpleSchemaAlgebra

import scala.util.Try

trait SimpleBsonReaderInterpreter extends SimpleSchemaAlgebra[BsonReader] {
  override def charSchema: BsonReader[Char] =
    BsonReader {
      case string: BSONString if string.value.length <= 1 => Try { string.value.charAt(0) }.toOption.getOrElse(Char.MinValue)
      case other => throw new RuntimeException(s"Cannot parse $other as a char")
    }

  override def byteSchema: BsonReader[Byte] =
    BsonReader {
      case int: BSONInteger if int.value < Byte.MaxValue && int.value > Byte.MinValue => int.value.toByte
      case other => throw new RuntimeException(s"Cannot parse $other as a byte")
    }

  override def shortSchema: BsonReader[Short] =
    BsonReader {
      case int: BSONInteger if int.value < Short.MaxValue && int.value > Short.MinValue => int.value.toShort
      case other => throw new RuntimeException(s"Cannot parse $other as a short")
    }

  override def floatSchema: BsonReader[Float] =
    BsonReader {
      case double: BSONDouble if double.value > Float.MinValue && double.value < Float.MaxValue => double.value.toFloat
      case int: BSONInteger if int.value > Float.MinValue && int.value < Float.MaxValue => int.value.toFloat
      case other => throw new RuntimeException(s"Cannot parse $other as a float")
    }

  override def decimalSchema: BsonReader[Double] =
    BsonReader {
      case double: BSONDouble => double.value
      case int: BSONInteger   => int.value
      case other              => throw new RuntimeException(s"Cannot parse $other as a double")
    }

  override def bigDecimalSchema: BsonReader[BigDecimal] =
    BsonReader {
      case decimal: BSONDecimal => BSONDecimal.toBigDecimal(decimal).get
      case double: BSONDouble   => double.value
      case int: BSONInteger     => int.value
      case other                => throw new RuntimeException(s"Cannot parse $other as a bigDecimal")
    }

  override def integerSchema: BsonReader[Int] =
    BsonReader {
      case integer: BSONInteger => integer.value
      case other                => throw new RuntimeException(s"Cannot parse $other as an integer")
    }

  override def longSchema: BsonReader[Long] =
    BsonReader {
      case long: BSONLong   => long.value
      case int: BSONInteger => int.value
      case other            => throw new RuntimeException(s"Cannot parse $other as a long")
    }

  override def bigIntSchema: BsonReader[BigInt] =
    bigDecimalSchema.afterRead(_.toBigIntExact().get)

  override def booleanSchema: BsonReader[Boolean] =
    BsonReader {
      case boolean: BSONBoolean => boolean.value
      case other                => throw new RuntimeException(s"Cannot parse $other as a boolean")
    }

  override def stringSchema: BsonReader[String] =
    BsonReader {
      case string: BSONString => string.value
      case other              => throw new RuntimeException(s"Cannot parse $other as a boolean")
    }

  override def dateTimeSchema: BsonReader[ZonedDateTime] =
    BsonReader {
      case dateTime: BSONDateTime => ZonedDateTime.ofInstant(Instant.ofEpochSecond(dateTime.value), ZoneId.of("UTC"))
      case dateTime: BSONString   => Try { ZonedDateTime.parse(dateTime.value, DateTimeFormatter.ISO_OFFSET_DATE_TIME) }.getOrElse(throw new RuntimeException(s"cannot parse $dateTime as a datetime"))
      case dateTime: BSONLong     => ZonedDateTime.ofInstant(Instant.ofEpochSecond(dateTime.value), ZoneId.of("UTC"))
      case dateTime: BSONInteger  => ZonedDateTime.ofInstant(Instant.ofEpochSecond(dateTime.value), ZoneId.of("UTC"))
      case other                  => throw new RuntimeException(s"cannot parse $other as a datetime")
    }

  override def timeSchema: BsonReader[LocalTime] =
    BsonReader {
      case time: BSONString  => Try { LocalTime.parse(time.value, DateTimeFormatter.ISO_LOCAL_TIME) }.getOrElse(throw new RuntimeException(s"cannot parse $time as a time"))
      case time: BSONLong    => LocalTime.ofSecondOfDay(time.value)
      case time: BSONInteger => LocalTime.ofSecondOfDay(time.value)
      case other             => throw new RuntimeException(s"cannot parse $other as a time")
    }

  override def dateSchema: BsonReader[LocalDate] =
    BsonReader {
      case date: BSONString  => Try { LocalDate.parse(date.value, DateTimeFormatter.ISO_LOCAL_DATE) }.getOrElse(throw new RuntimeException(s"cannot parse $date as a date"))
      case date: BSONLong    => LocalDate.ofEpochDay(date.value)
      case date: BSONInteger => LocalDate.ofEpochDay(date.value)
      case other             => throw new RuntimeException(s"cannot parse $other as a date")
    }
}
