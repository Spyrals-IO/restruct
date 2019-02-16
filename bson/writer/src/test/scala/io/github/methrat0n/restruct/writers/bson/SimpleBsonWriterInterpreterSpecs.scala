package io.github.methrat0n.restruct.writers.bson

import java.time.temporal.ChronoField
import java.time.{LocalDate, LocalTime, ZonedDateTime}

import bsonWriter
import io.github.methrat0n.restruct.schema.Syntax._
import org.scalatest.{FlatSpec, Matchers}
import reactivemongo.bson._

class SimpleBsonWriterInterpreterSpecs extends FlatSpec with Matchers {

  private val stringTest: String = "methrat0n"
  private val decimalTest: Double = 12678888899999999999999999999988888676997676.2d
  private val integerTest: Int = 33333
  private val booleanTest: Boolean = true
  private val charTest: Char = 'a'
  private val byteTest: Byte = 123
  private val shortTest: Short = 23456
  private val floatTest: Float = 12.2f
  private val bigDecimal128: BigDecimal = BigDecimal("170141183460469231731687303715884.1")
  private val longTest: Long = 1267888889999999999l
  private val bigInt128: BigInt = BigInt("1701411834604692317316873037158841")
  private val dateTimeTest: ZonedDateTime = ZonedDateTime.parse("2018-12-26T00:08:16.025415+01:00[Europe/Paris]")
  private val timeTest: LocalTime = LocalTime.parse("00:08:16.025415")
  private val dateTest: LocalDate = LocalDate.parse("2018-12-26")

  behavior of "BsonWriter for strings"

  "BsonWriter" should "find a bson writer for strings" in {
    string.bind(bsonWriter)
  }
  it should "write string as BSONString" in {
    val stringWriter = string.bind(bsonWriter)

    val found = stringWriter.write(stringTest)
    val expect = BSONString(stringTest)
    found shouldBe expect
  }
  it should "write the same than the default string writer" in {
    val derived = string.bind(bsonWriter)
    val default = DefaultBSONHandlers.BSONStringHandler

    val found = derived.write(stringTest)
    val expect = default.write(stringTest)

    found shouldBe expect
  }

  behavior of "BsonWriter for decimal"

  "BsonWriter" should "find a bson writer for decimals" in {
    decimal.bind(bsonWriter)
  }
  it should "write decimal as BSONDouble" in {
    val decimalWriter = decimal.bind(bsonWriter)

    val found = decimalWriter.write(decimalTest)
    val expect = BSONDouble(decimalTest)
    found shouldBe expect
  }
  it should "write the same than the default decimal writer" in {
    val derived = decimal.bind(bsonWriter)
    val default = DefaultBSONHandlers.BSONDoubleHandler

    val found = derived.write(decimalTest)
    val expect = default.write(decimalTest)

    found shouldBe expect
  }
  it should "write integer as BSONDouble using decimal writer" in {
    val decimalWriter = decimal.bind(bsonWriter)

    val found = decimalWriter.write(integerTest)
    val expect = BSONDouble(integerTest)
    found shouldBe expect
  }
  it should "write float as BSONDouble using decimal writer" in {
    val decimalWriter = decimal.bind(bsonWriter)

    val found = decimalWriter.write(floatTest)
    val expect = BSONDouble(floatTest)
    found shouldBe expect
  }
  it should "write byte as BSONDouble using decimal writer" in {
    val decimalWriter = decimal.bind(bsonWriter)

    val found = decimalWriter.write(byteTest)
    val expect = BSONDouble(byteTest)
    found shouldBe expect
  }
  it should "write short as BSONDouble using decimal writer" in {
    val decimalWriter = decimal.bind(bsonWriter)

    val found = decimalWriter.write(shortTest)
    val expect = BSONDouble(shortTest)
    found shouldBe expect
  }

  behavior of "BsonWriter for integer"

  "BsonWriter" should "find a bson writer for integers" in {
    integer.bind(bsonWriter)
  }
  it should "write integer as BSONInteger" in {
    val integerWriter = integer.bind(bsonWriter)

    val found = integerWriter.write(integerTest)
    val expect = BSONInteger(integerTest)
    found shouldBe expect
  }
  it should "write the same than the default integer writer" in {
    val derived = integer.bind(bsonWriter)
    val default = DefaultBSONHandlers.BSONIntegerHandler

    val found = derived.write(integerTest)
    val expect = default.write(integerTest)

    found shouldBe expect
  }
  it should "write byte as BSONInteger using integer writer" in {
    val integerWriter = integer.bind(bsonWriter)

    val found = integerWriter.write(byteTest)
    val expect = BSONInteger(byteTest)
    found shouldBe expect
  }
  it should "write short as BSONInteger using integer writer" in {
    val integerWriter = integer.bind(bsonWriter)

    val found = integerWriter.write(shortTest)
    val expect = BSONInteger(shortTest)
    found shouldBe expect
  }

  behavior of "BsonWriter for boolean"

  "BsonWriter" should "find a bson writer for booleans" in {
    boolean.bind(bsonWriter)
  }
  it should "write boolean as BSONBoolean" in {
    val booleanWriter = boolean.bind(bsonWriter)

    val found = booleanWriter.write(booleanTest)
    val expect = BSONBoolean(booleanTest)
    found shouldBe expect
  }
  it should "write the same than the default boolean writer" in {
    val derived = boolean.bind(bsonWriter)
    val default = DefaultBSONHandlers.BSONBooleanHandler

    val found = derived.write(booleanTest)
    val expect = default.write(booleanTest)

    found shouldBe expect
  }

  behavior of "BsonWriter for char"

  "BsonWriter" should "find a bson writer for chars" in {
    char.bind(bsonWriter)
  }
  it should "write char as BSONString" in {
    val charWriter = char.bind(bsonWriter)

    val found = charWriter.write(charTest)
    val expect = BSONString(charTest.toString)
    found shouldBe expect
  }

  behavior of "BsonWriter for byte"

  "BsonWriter" should "find a bson writer for bytes" in {
    byte.bind(bsonWriter)
  }
  it should "write byte as BSONInteger" in {
    val byteWriter = byte.bind(bsonWriter)

    val found = byteWriter.write(byteTest)
    val expect = BSONInteger(byteTest)
    found shouldBe expect
  }

  behavior of "BsonWriter for short"

  "BsonWriter" should "find a bson writer for shorts" in {
    short.bind(bsonWriter)
  }
  it should "write short as BSONInteger" in {
    val shortWriter = short.bind(bsonWriter)

    val found = shortWriter.write(shortTest)
    val expect = BSONInteger(shortTest)
    found shouldBe expect
  }

  behavior of "BsonWriter for float"

  "BsonWriter" should "find a bson writer for floats" in {
    float.bind(bsonWriter)
  }
  it should "write float as BSONDouble" in {
    val floatWriter = float.bind(bsonWriter)

    val found = floatWriter.write(floatTest)
    val expect = BSONDouble(floatTest)
    found shouldBe expect
  }
  it should "write integer as BSONDouble using float writer" in {
    val floatWriter = float.bind(bsonWriter)

    val found = floatWriter.write(integerTest)
    val expect = BSONDouble(integerTest)
    found shouldBe expect
  }
  it should "write byte as BSONDouble using float writer" in {
    val floatWriter = float.bind(bsonWriter)

    val found = floatWriter.write(byteTest)
    val expect = BSONDouble(byteTest)
    found shouldBe expect
  }
  it should "write short as BSONDouble using float writer" in {
    val floatWriter = float.bind(bsonWriter)

    val found = floatWriter.write(shortTest)
    val expect = BSONDouble(shortTest)
    found shouldBe expect
  }

  behavior of "BsonWriter for BigDecimal"

  "BsonWriter" should "find a bson writer for bigDecimal" in {
    bigDecimal.bind(bsonWriter)
  }
  it should "write bigDecimal as BSONDecimal" in {
    val bigDecimalWriter = bigDecimal.bind(bsonWriter)

    val found = bigDecimalWriter.write(bigDecimal128)
    val expect = BSONDecimal.fromBigDecimal(bigDecimal128).get
    found shouldBe expect
  }
  it should "write the same than the default bigDecimal writer" in {
    val derived = bigDecimal.bind(bsonWriter)
    val default = DefaultBSONHandlers.BSONDecimalHandler

    val found = derived.write(bigDecimal128)
    val expect = default.write(bigDecimal128)

    found shouldBe expect
  }
  it should "write integer as BSONDecimal using bigDecimal writer" in {
    val bigDecimalWriter = bigDecimal.bind(bsonWriter)

    val found = bigDecimalWriter.write(integerTest)
    val expect = BSONDecimal.fromLong(integerTest).get
    found shouldBe expect
  }
  it should "write decimal as BSONDecimal using bigDecimal writer" in {
    val bigDecimalWriter = bigDecimal.bind(bsonWriter)

    val found = bigDecimalWriter.write(decimalTest)
    val expect = BSONDecimal.fromBigDecimal(decimalTest).get
    found shouldBe expect
  }

  behavior of "BsonWriter for Long"

  "BsonWriter" should "find a bson writer for long" in {
    long.bind(bsonWriter)
  }
  it should "write long as BSONLong" in {
    val longWriter = long.bind(bsonWriter)

    val found = longWriter.write(longTest)
    val expect = BSONLong(longTest)
    found shouldBe expect
  }
  it should "write the same than the default long writer" in {
    val derived = long.bind(bsonWriter)
    val default = DefaultBSONHandlers.BSONLongHandler

    val found = derived.write(longTest)
    val expect = default.write(longTest)

    found shouldBe expect
  }
  it should "write byte as BSONLong using long writer" in {
    val longWriter = long.bind(bsonWriter)

    val found = longWriter.write(byteTest)
    val expect = BSONLong(byteTest)
    found shouldBe expect
  }
  it should "write short as BSONLong using long writer" in {
    val longWriter = long.bind(bsonWriter)

    val found = longWriter.write(shortTest)
    val expect = BSONLong(shortTest)
    found shouldBe expect
  }
  it should "write integer as BSONLong using long writer" in {
    val longWriter = long.bind(bsonWriter)

    val found = longWriter.write(integerTest)
    val expect = BSONLong(integerTest)
    found shouldBe expect
  }

  behavior of "BsonWriter for bigInt"

  "BsonWriter" should "find a bson writer for bigInt" in {
    bigInt.bind(bsonWriter)
  }
  it should "write bigInt as BSONDecimal" in {
    val bigIntWriter = bigInt.bind(bsonWriter)

    val found = bigIntWriter.write(bigInt128)
    val expect = BSONDecimal.fromBigDecimal(BigDecimal.exact(bigInt128)).get
    found shouldBe expect
  }
  it should "write integer as BSONDecimal using bigInt writer" in {
    val bigIntWriter = bigInt.bind(bsonWriter)

    val found = bigIntWriter.write(integerTest)
    val expect = BSONDecimal.fromBigDecimal(BigDecimal.exact(integerTest)).get
    found shouldBe expect
  }

  behavior of "BsonWriter for dateTime"

  "BsonWriter" should "find a bson writer for dateTime" in {
    dateTime.bind(bsonWriter)
  }
  it should "write dateTime as BSONDateTime" in {
    val dateTimeWriter = dateTime.bind(bsonWriter)

    val found = dateTimeWriter.write(dateTimeTest)
    val expect = BSONDateTime(dateTimeTest.getLong(ChronoField.OFFSET_SECONDS))
    found shouldBe expect
  }

  behavior of "BsonWriter for time"

  "BsonWriter" should "find a json writer for time" in {
    time.bind(bsonWriter)
  }
  it should "write time as BSONDateTime" in {
    val timeWriter = time.bind(bsonWriter)

    val found = timeWriter.write(timeTest)
    val expect = BSONDateTime(timeTest.getLong(ChronoField.SECOND_OF_DAY))

    found shouldBe expect
  }

  behavior of "BsonWriter for date"

  "BsonWriter" should "find a bson writer for date" in {
    date.bind(bsonWriter)
  }
  it should "write date as BSONDateTime" in {
    val dateWriter = date.bind(bsonWriter)

    val found = dateWriter.write(dateTest)
    val expect = BSONDateTime(dateTest.getLong(ChronoField.EPOCH_DAY) * 86400)
    found shouldBe expect
  }
}
