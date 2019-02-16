package io.github.methrat0n.restruct.readers.bson

import java.time._

import io.github.methrat0n.restruct.core.data.schema.FieldAlgebra
import io.github.methrat0n.restruct.schema.Schema
import io.github.methrat0n.restruct.schema.Syntax._
import org.scalatest.{ FlatSpec, Matchers, Outcome, Succeeded }
import reactivemongo.bson._

import scala.collection.mutable.ListBuffer
import scala.util.{ Failure, Try }

class SimpleBsonReaderInterpreterSpecs extends FlatSpec with Matchers {

  private val stringTest: BSONString = BSONString("methrat0n")
  private val decimalTest: BSONDouble = BSONDouble(1343333333333333.2d)
  private val integerTest: BSONInteger = BSONInteger(33333)
  private val booleanTest: BSONBoolean = BSONBoolean(true)
  private val charTest: BSONString = BSONString('a'.toString)
  private val byteTest: BSONInteger = BSONInteger(123)
  private val shortTest: BSONInteger = BSONInteger(23456)
  private val floatTest: BSONDouble = BSONDouble(12.2f)
  private val bigDecimalTest: BSONDecimal = BSONDecimal.fromBigDecimal(BigDecimal("1267888889999999911111111.1")).get
  private val longTest: BSONLong = BSONLong(1267888889999999999l)
  private val bigIntTest: BSONDecimal = BSONDecimal.fromBigDecimal(BigDecimal(BigInt("1267888889999111111111111"))).get
  private val dateTimeTest: BSONDateTime = BSONDateTime(1550313668)
  private val stringdateTimeTest: BSONString = BSONString("2019-02-16T11:40:26.236230+01:00[Europe/Paris]")
  private val longDateTimeTest: BSONLong = BSONLong(1550313668)
  private val stringTimeTest: BSONString = BSONString("11:42:49.348114")
  private val longTimeTest: BSONLong = BSONLong(42207)
  private val stringDateTest: BSONString = BSONString("2019-02-16")
  private val longDateTest: BSONLong = BSONLong(17943)

  private def shouldBeAnError[T](found: Try[T]) = found match {
    case Failure(_) => Succeeded
    case _          => throw new RuntimeException(s"this test should have return an error but instead return $found")
  }
  import language.higherKinds
  private def shouldFail[T, Form[_], Err, Value](
    interpreter: FieldAlgebra[Form],
    localSchema: Schema[T],
    formatName: String,
    localFormat: String,
    failures: Seq[Value],
    test: (Form[T], Value) => Err,
    errorSwitch: Try[Err] => Outcome
  ) = {
    failures.foreach(failure => {
      it should s"not read $failure with $localFormat $formatName" in {
        errorSwitch(Try(test(localSchema.bind(interpreter), failure)))
      }
    })
  }

  val allValues = ListBuffer[BSONValue](
    stringTest,
    decimalTest,
    integerTest,
    booleanTest,
    charTest,
    byteTest,
    shortTest,
    floatTest,
    bigDecimalTest,
    longTest,
    bigIntTest,
    dateTimeTest,
    stringdateTimeTest,
    longDateTimeTest,
    stringTimeTest,
    longTimeTest,
    stringDateTest,
    longDateTest
  )

  val allStrings = ListBuffer[BSONString](
    stringTest,
    charTest,
    stringdateTimeTest,
    stringTimeTest,
    stringDateTest
  )

  val allNumbers = ListBuffer[BSONValue](
    decimalTest,
    integerTest,
    byteTest,
    shortTest,
    floatTest,
    bigDecimalTest,
    longTest,
    bigIntTest,
    longDateTimeTest,
    longTimeTest,
    longDateTest
  )

  behavior of "BSONReaderInterpreter for strings"

  "BSONReaderInterpreter" should "find a Bson reads for strings" in {
    string.bind(bsonReader)
  }
  it should "read string from BSONString" in {
    val stringReads = string.bind(bsonReader)

    val found = stringReads.read(stringTest)
    val expect = stringTest.value
    found shouldBe expect
  }
  it should "read the same than the default string reads" in {
    val derived = string.bind(bsonReader)
    val default = DefaultBSONHandlers.BSONStringHandler

    val found = derived.read(stringTest)
    val expect = default.read(stringTest)

    found shouldBe expect
  }
  shouldFail[String, BsonReader, String, BSONValue](
    bsonReader,
    string,
    "bsonReader",
    "string",
    allValues -- allStrings,
    (reads, value) => reads.read(value),
    shouldBeAnError _
  )

  behavior of "BSONReaderInterpreter for decimal"

  "BSONReaderInterpreter" should "find a Bson reads for decimals" in {
    decimal.bind(bsonReader)
  }
  it should "read decimal from BSONDouble" in {
    val decimalReads = decimal.bind(bsonReader)

    val found = decimalReads.read(decimalTest)
    val expect = decimalTest.value
    found shouldBe expect
  }
  it should "read the same than the default decimal reads" in {
    val derived = decimal.bind(bsonReader)
    val default = DefaultBSONHandlers.BSONDoubleHandler

    val found = derived.read(decimalTest)
    val expect = default.read(decimalTest)

    found shouldBe expect
  }
  shouldFail[Double, BsonReader, Double, BSONValue](
    bsonReader,
    decimal,
    "bsonReader",
    "decimal",
    allStrings :+ booleanTest,
    (reads, value) => reads.read(value),
    shouldBeAnError _
  )

  behavior of "BSONReaderInterpreter for integer"

  "BSONReaderInterpreter" should "find a Bson reads for integers" in {
    integer.bind(bsonReader)
  }
  it should "read integer from BSONInteger" in {
    val integerReads = integer.bind(bsonReader)

    val found = integerReads.read(integerTest)
    val expect = integerTest.value
    found shouldBe expect
  }
  it should "read the same than the default integer reads" in {
    val derived = integer.bind(bsonReader)
    val default = DefaultBSONHandlers.BSONIntegerHandler

    val found = derived.read(integerTest)
    val expect = default.read(integerTest)

    found shouldBe expect
  }
  shouldFail[Int, BsonReader, Int, BSONValue](
    bsonReader,
    integer,
    "bsonReader",
    "integer",
    (allStrings :+ booleanTest) ++ List(decimalTest, floatTest, bigDecimalTest),
    (reads, value) => reads.read(value),
    shouldBeAnError _
  )

  behavior of "BSONReaderInterpreter for boolean"

  "BSONReaderInterpreter" should "find a Bson reads for booleans" in {
    boolean.bind(bsonReader)
  }
  it should "read boolean from BSONBoolean" in {
    val booleanReads = boolean.bind(bsonReader)

    val found = booleanReads.read(booleanTest)
    val expect = booleanTest.value
    found shouldBe expect
  }
  it should "read the same than the default boolean reads" in {
    val derived = boolean.bind(bsonReader)
    val default = DefaultBSONHandlers.BSONBooleanHandler

    val found = derived.read(booleanTest)
    val expect = default.read(booleanTest)

    found shouldBe expect
  }
  shouldFail[Boolean, BsonReader, Boolean, BSONValue](
    bsonReader,
    boolean,
    "bsonReader",
    "boolean",
    allStrings ++ allNumbers,
    (reads, value) => reads.read(value),
    shouldBeAnError _
  )

  behavior of "BSONReaderInterpreter for char"

  "BSONReaderInterpreter" should "find a Bson reads for chars" in {
    char.bind(bsonReader)
  }
  it should "reads char from BSONString" in {
    val charReads = char.bind(bsonReader)

    val found = charReads.read(charTest)
    val expect = charTest.value.charAt(0)
    found shouldBe expect
  }
  shouldFail[Char, BsonReader, Char, BSONValue](
    bsonReader,
    char,
    "bsonReader",
    "char",
    allValues - charTest,
    (reads, value) => reads.read(value),
    shouldBeAnError _
  )

  behavior of "BSONReaderInterpreter for byte"

  "BSONReaderInterpreter" should "find a Bson reads for bytes" in {
    byte.bind(bsonReader)
  }
  it should "read byte from BSONInteger" in {
    val byteReads = byte.bind(bsonReader)

    val found = byteReads.read(byteTest)
    val expect = byteTest.value
    found shouldBe expect
  }
  shouldFail[Byte, BsonReader, Byte, BSONValue](
    bsonReader,
    byte,
    "bsonReader",
    "byte",
    allValues - byteTest,
    (reads, value) => reads.read(value),
    shouldBeAnError _
  )

  behavior of "BSONReaderInterpreter for short"

  "BSONReaderInterpreter" should "find a Bson reads for shorts" in {
    short.bind(bsonReader)
  }
  it should "read short from BSONInteger" in {
    val shortReads = short.bind(bsonReader)

    val found = shortReads.read(shortTest)
    val expect = shortTest.value
    found shouldBe expect
  }
  it should "read byte from BSONInteger using short reads" in {
    val shortReads = short.bind(bsonReader)

    val found = shortReads.read(byteTest)
    val expect = byteTest.value
    found shouldBe expect
  }
  shouldFail[Short, BsonReader, Short, BSONValue](
    bsonReader,
    short,
    "bsonReader",
    "short",
    allValues -- List(byteTest, shortTest),
    (reads, value) => reads.read(value),
    shouldBeAnError _
  )

  behavior of "BSONReaderInterpreter for float"

  "BSONReaderInterpreter" should "find a Bson reads for floats" in {
    float.bind(bsonReader)
  }
  it should "read float from BSONDouble" in {
    val floatReads = float.bind(bsonReader)

    val found = floatReads.read(floatTest)
    val expect = floatTest.value.floatValue()
    found shouldBe expect
  }
  it should "read integer from BSONDouble using float reads" in {
    val floatReads = float.bind(bsonReader)

    val found = floatReads.read(integerTest)
    val expect = integerTest.value
    found shouldBe expect
  }
  it should "read byte from BSONDouble using float reads" in {
    val floatReads = float.bind(bsonReader)

    val found = floatReads.read(byteTest)
    val expect = byteTest.value
    found shouldBe expect
  }
  it should "read short from BSONDouble using float reads" in {
    val floatReads = float.bind(bsonReader)

    val found = floatReads.read(shortTest)
    val expect = shortTest.value
    found shouldBe expect
  }
  shouldFail[Float, BsonReader, Float, BSONValue](
    bsonReader,
    float,
    "bsonReader",
    "float",
    allStrings :+ booleanTest,
    (reads, value) => reads.read(value),
    shouldBeAnError _
  )

  behavior of "BSONReaderInterpreter for BigDecimal"

  "BSONReaderInterpreter" should "find a Bson reads for bigDecimal" in {
    bigDecimal.bind(bsonReader)
  }
  it should "read bigDecimal from BSONDecimal" in {
    val bigDecimalReads = bigDecimal.bind(bsonReader)

    val found = bigDecimalReads.read(bigDecimalTest)
    val expect = BSONDecimal.toBigDecimal(bigDecimalTest).get
    found shouldBe expect
  }
  it should "read the same than the default bigDecimal reads" in {
    val derived = bigDecimal.bind(bsonReader)
    val default = DefaultBSONHandlers.BSONDecimalHandler

    val found = derived.read(bigDecimalTest)
    val expect = default.read(bigDecimalTest)

    found shouldBe expect
  }
  it should "read integer from BSONDecimal using bigDecimal reads" in {
    val bigDecimalReads = bigDecimal.bind(bsonReader)

    val found = bigDecimalReads.read(integerTest)
    val expect = integerTest.value
    found shouldBe expect
  }
  it should "read decimal from BSONDecimal using bigDecimal reads" in {
    val bigDecimalReads = bigDecimal.bind(bsonReader)

    val found = bigDecimalReads.read(decimalTest)
    val expect = decimalTest.value
    found shouldBe expect
  }
  shouldFail[BigDecimal, BsonReader, BigDecimal, BSONValue](
    bsonReader,
    bigDecimal,
    "bsonReader",
    "bigDecimal",
    allValues -- allNumbers,
    (reads, value) => reads.read(value),
    shouldBeAnError _
  )

  behavior of "BSONReaderInterpreter for Long"

  "BSONReaderInterpreter" should "find a Bson reads for long" in {
    long.bind(bsonReader)
  }
  it should "read long from BSONLong" in {
    val longReads = long.bind(bsonReader)

    val found = longReads.read(longTest)
    val expect = longTest.value
    found shouldBe expect
  }
  it should "read the same than the default long reads" in {
    val derived = long.bind(bsonReader)
    val default = DefaultBSONHandlers.BSONLongHandler

    val found = derived.read(longTest)
    val expect = default.read(longTest)

    found shouldBe expect
  }
  shouldFail[Long, BsonReader, Long, BSONValue](
    bsonReader,
    long,
    "bsonReader",
    "long",
    allValues -- List(longTest, integerTest, byteTest, shortTest, longDateTest, longTimeTest, longDateTimeTest),
    (reads, value) => reads.read(value),
    shouldBeAnError _
  )

  behavior of "BSONReaderInterpreter for bigInt"

  "BSONReaderInterpreter" should "find a Bson reads for bigInt" in {
    bigInt.bind(bsonReader)
  }
  it should "read bigInt from BSONDecimal" in {
    val bigIntReads = bigInt.bind(bsonReader)

    val found = bigIntReads.read(bigIntTest)
    val expect = BSONDecimal.toBigDecimal(bigIntTest).get.toBigIntExact().get
    found shouldBe expect
  }
  it should "read integer from BSONDecimal using bigInt reads" in {
    val bigIntReads = bigInt.bind(bsonReader)

    val found = bigIntReads.read(integerTest)
    val expect = integerTest.value
    found shouldBe expect
  }
  shouldFail[BigInt, BsonReader, BigInt, BSONValue](
    bsonReader,
    bigInt,
    "bsonReader",
    "bigInt",
    allValues -- List(bigIntTest, longTest, integerTest, shortTest, byteTest),
    (reads, value) => reads.read(value),
    shouldBeAnError _
  )

  behavior of "BSONReaderInterpreter for dateTime"

  "BSONReaderInterpreter" should "find a Bson reads for dateTime" in {
    dateTime.bind(bsonReader)
  }
  it should "read dateTime from BSONString" in {
    val dateTimeReads = dateTime.bind(bsonReader)

    val found = dateTimeReads.read(dateTimeTest)
    val expect = ZonedDateTime.ofInstant(Instant.ofEpochSecond(dateTimeTest.value), ZoneId.of("UTC"))
    found shouldBe expect
  }
  it should "read datetime from BSONLong" in {
    val dateTimeReads = dateTime.bind(bsonReader)

    val found = dateTimeReads.read(longDateTimeTest)
    val expect = ZonedDateTime.ofInstant(Instant.ofEpochSecond(longDateTimeTest.value), ZoneId.of("UTC"))
    found shouldBe expect
  }
  shouldFail[ZonedDateTime, BsonReader, ZonedDateTime, BSONValue](
    bsonReader,
    dateTime,
    "bsonReader",
    "dateTime",
    allValues -- List(dateTimeTest, longDateTimeTest, longTest, longTimeTest, longDateTest, integerTest, byteTest, shortTest),
    (reads, value) => reads.read(value),
    shouldBeAnError _
  )

  behavior of "BSONReaderInterpreter for time"

  "BSONReaderInterpreter" should "find a Bson reads for time" in {
    time.bind(bsonReader)
  }
  it should "read time from BSONString" in {
    val timeReads = time.bind(bsonReader)

    val found = timeReads.read(stringTimeTest)
    val expect = LocalTime.parse(stringTimeTest.value)

    found shouldBe expect
  }
  it should "read time from BSONLong within 0 - 86399999999999" in {
    val timeReads = time.bind(bsonReader)

    val found = timeReads.read(longTimeTest)
    val expect = LocalTime.ofSecondOfDay(longTimeTest.value)
    found shouldBe expect
  }
  shouldFail[LocalTime, BsonReader, LocalTime, BSONValue](
    bsonReader,
    time,
    "bsonReader",
    "time",
    allValues -- List(stringTimeTest, longTimeTest, longTest, longDateTimeTest, longDateTest, integerTest, byteTest, shortTest),
    (reads, value) => reads.read(value),
    shouldBeAnError _
  )

  behavior of "BSONReaderInterpreter for date"

  "BSONReaderInterpreter" should "find a Bson reads for date" in {
    date.bind(bsonReader)
  }
  it should "read date from BSONString" in {
    val dateReads = date.bind(bsonReader)

    val found = dateReads.read(stringDateTest)
    val expect = LocalDate.parse(stringDateTest.value)
    found shouldBe expect
  }
  it should "read date from BSONLong" in {
    val dateReads = date.bind(bsonReader)

    val found = dateReads.read(longDateTest)
    val expect = LocalDate.ofEpochDay(longDateTest.value)
    found shouldBe expect
  }
  shouldFail[LocalDate, BsonReader, LocalDate, BSONValue](
    bsonReader,
    date,
    "bsonReader",
    "date",
    allValues -- List(stringDateTest, longDateTest, longTest, longDateTimeTest, longTimeTest, integerTest, byteTest, shortTest),
    (reads, value) => reads.read(value),
    shouldBeAnError _
  )
}
