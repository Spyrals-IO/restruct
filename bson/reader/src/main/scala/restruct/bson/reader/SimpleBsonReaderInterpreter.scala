package restruct.bson.reader

import reactivemongo.bson.{ BSONBoolean, BSONDecimal, BSONDouble, BSONInteger, BSONLong, BSONString }
import restruct.core.data.schema.SimpleSchemaAlgebra

import scala.util.Try

trait SimpleBsonReaderInterpreter extends SimpleSchemaAlgebra[BsonReader] {
  override def charSchema: BsonReader[Char] =
    BsonReader {
      case string: BSONString if string.value.length <= 1 => Try { string.value.charAt(0) }.toOption.getOrElse(Char.MinValue)
      case other => throw new RuntimeException(s"Cannot parse $other as a char")
    }

  override def byteSchema: BsonReader[Byte] =
    BsonReader {
      case int: BSONInteger => int.value.toByte
      case other            => throw new RuntimeException(s"Cannot parse $other as a byte")
    }

  override def shortSchema: BsonReader[Short] =
    BsonReader {
      case int: BSONInteger => int.value.toShort
      case other            => throw new RuntimeException(s"Cannot parse $other as a short")
    }

  override def floatSchema: BsonReader[Float] =
    BsonReader {
      case double: BSONDouble => double.value.toFloat
      case other              => throw new RuntimeException(s"Cannot parse $other as a float")
    }

  override def decimalSchema: BsonReader[Double] =
    BsonReader {
      case double: BSONDouble => double.value
      case other              => throw new RuntimeException(s"Cannot parse $other as a double")
    }

  override def bigDecimalSchema: BsonReader[BigDecimal] =
    BsonReader {
      case decimal: BSONDecimal => BSONDecimal.toBigDecimal(decimal).get
      case other                => throw new RuntimeException(s"Cannot parse $other as a bigDecimal")
    }

  override def integerSchema: BsonReader[Int] =
    BsonReader {
      case integer: BSONInteger => integer.value
      case other                => throw new RuntimeException(s"Cannot parse $other as an integer")
    }

  override def longSchema: BsonReader[Long] =
    BsonReader {
      case long: BSONLong => long.value
      case other          => throw new RuntimeException(s"Cannot parse $other as a long")
    }

  override def bigIntSchema: BsonReader[BigInt] =
    BsonReader {
      case decimal: BSONDecimal => BSONDecimal.toBigDecimal(decimal).get.toBigInt()
      case other                => throw new RuntimeException(s"Cannot parse $other as a bigDecimal")
    }

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
}
