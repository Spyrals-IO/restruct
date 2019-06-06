package io.github.methrat0n.restruct.readers.json

import java.time._

import io.github.methrat0n.restruct.core.data.schema.FieldAlgebra
import io.github.methrat0n.restruct.schema.Schema
import org.scalatest.{ FlatSpec, Matchers, Outcome, Succeeded }
import play.api.libs.json._

import scala.collection.mutable.ListBuffer
import scala.util.{ Failure, Success, Try }

class SimpleJsonReaderInterpreterSpecs extends FlatSpec with Matchers {

  private val stringTest: JsString = JsString("methrat0n")
  private val decimalTest: JsNumber = JsNumber(BigDecimal(1343333333333333.2d))
  private val integerTest: JsNumber = JsNumber(BigDecimal(33333))
  private val booleanTest: JsBoolean = JsBoolean(true)
  private val charTest: JsString = JsString('a'.toString)
  private val byteTest: JsNumber = JsNumber(BigDecimal(123))
  private val shortTest: JsNumber = JsNumber(BigDecimal(23456))
  private val floatTest: JsNumber = JsNumber(WorkAround.Intern.decimal(12.2f))
  private val bigDecimalTest: JsNumber = JsNumber(BigDecimal("1267888889999999999999999999998888867699767611111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111.1"))
  private val longTest: JsNumber = JsNumber(BigDecimal(1267888889999999999l))
  private val bigIntTest: JsNumber = JsNumber(BigDecimal(BigInt("1267888889999999999999999999998888867699767611111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111")))
  private val dateTimeTest: JsString = JsString("2018-12-26T00:08:16.025415+01:00[Europe/Paris]")
  private val timeTest: JsString = JsString("00:08:16.025415")
  private val dateTest: JsString = JsString("2018-12-26")

  //This exist to be able to call BigDecimal.apply without a warning being raised. When a SuppressWarning or equivalent is added to scala
  //this code should be removed
  object WorkAround { @deprecated("", "") class Intern { def decimal(f: Float): BigDecimal = BigDecimal(f) }; object Intern extends Intern }

  private def shouldBeAnError[T](found: Try[JsResult[T]]) = found match {
    case Failure(_) | Success(JsError(_)) => Succeeded
    case _                                => throw new RuntimeException(s"this test should have return an error but instead return $found")
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

  val allStrings = ListBuffer[JsValue](
    stringTest,
    charTest,
    dateTimeTest,
    timeTest,
    dateTest
  )

  val allNumbers = ListBuffer[JsValue](
    decimalTest,
    integerTest,
    byteTest,
    shortTest,
    floatTest,
    bigDecimalTest,
    longTest,
    bigIntTest
  )

  behavior of "JsonReaderInterpreter for strings"

  "JsonReaderInterpreter" should "find a json reads for strings" in {
    string.bind(jsonReads)
  }
  it should "read string from JsString" in {
    val stringReads = string.bind(jsonReads)

    val found = stringReads.reads(stringTest)
    val expect = JsSuccess(stringTest.value)
    found shouldBe expect
  }
  it should "read the same than the default string reads" in {
    val derived: Reads[String] = string.bind(jsonReads)
    val default: Reads[String] = Reads.StringReads

    val found = derived.reads(stringTest)
    val expect = default.reads(stringTest)

    found shouldBe expect
  }
  shouldFail[String, Reads, JsResult[String], JsValue](
    jsonReads,
    string,
    "jsonReads",
    "string",
    allNumbers :+ booleanTest,
    (reads, value) => reads.reads(value),
    shouldBeAnError _
  )

  behavior of "JsonReaderInterpreter for decimal"

  "JsonReaderInterpreter" should "find a json reads for decimals" in {
    decimal.bind(jsonReads)
  }
  it should "read decimal from JsNumber" in {
    val decimalReads = decimal.bind(jsonReads)

    val found = decimalReads.reads(decimalTest)
    val expect = JsSuccess(decimalTest.value.toDouble)
    found shouldBe expect
  }
  it should "read the same than the default decimal reads" in {
    val derived: Reads[Double] = decimal.bind(jsonReads)
    val default: Reads[Double] = Reads.DoubleReads

    val found = derived.reads(decimalTest)
    val expect = default.reads(decimalTest)

    found shouldBe expect
  }
  shouldFail[Double, Reads, JsResult[Double], JsValue](
    jsonReads,
    decimal,
    "jsonReads",
    "decimal",
    allStrings :+ booleanTest,
    (reads, value) => reads.reads(value),
    shouldBeAnError _
  )

  behavior of "JsonReaderInterpreter for integer"

  "JsonReaderInterpreter" should "find a json reads for integers" in {
    integer.bind(jsonReads)
  }
  it should "read integer from JsNumber" in {
    val integerReads = integer.bind(jsonReads)

    val found = integerReads.reads(integerTest)
    val expect = JsSuccess(integerTest.value.toIntExact)
    found shouldBe expect
  }
  it should "read the same than the default integer reads" in {
    val derived: Reads[Int] = integer.bind(jsonReads)
    val default: Reads[Int] = Reads.IntReads

    val found = derived.reads(integerTest)
    val expect = default.reads(integerTest)

    found shouldBe expect
  }
  shouldFail[Int, Reads, JsResult[Int], JsValue](
    jsonReads,
    integer,
    "jsonReads",
    "integer",
    (allStrings :+ booleanTest) ++ List(decimalTest, floatTest, bigDecimalTest),
    (reads, value) => reads.reads(value),
    shouldBeAnError _
  )

  behavior of "JsonReaderInterpreter for boolean"

  "JsonReaderInterpreter" should "find a json reads for booleans" in {
    boolean.bind(jsonReads)
  }
  it should "read boolean from JsBoolean" in {
    val booleanReads = boolean.bind(jsonReads)

    val found = booleanReads.reads(booleanTest)
    val expect = JsSuccess(booleanTest.value)
    found shouldBe expect
  }
  it should "read the same than the default boolean reads" in {
    val derived: Reads[Boolean] = boolean.bind(jsonReads)
    val default: Reads[Boolean] = Reads.BooleanReads

    val found = derived.reads(booleanTest)
    val expect = default.reads(booleanTest)

    found shouldBe expect
  }
  shouldFail[Boolean, Reads, JsResult[Boolean], JsValue](
    jsonReads,
    boolean,
    "jsonReads",
    "boolean",
    allStrings ++ allNumbers,
    (reads, value) => reads.reads(value),
    shouldBeAnError _
  )

  behavior of "JsonReaderInterpreter for char"

  "JsonReaderInterpreter" should "find a json reads for chars" in {
    char.bind(jsonReads)
  }
  it should "reads char from JsString" in {
    val charReads = char.bind(jsonReads)

    val found = charReads.reads(charTest)
    val expect = JsSuccess(charTest.value.charAt(0))
    found shouldBe expect
  }
  shouldFail[Char, Reads, JsResult[Char], JsValue](
    jsonReads,
    char,
    "jsonReads",
    "char",
    (allStrings ++ allNumbers :+ booleanTest) - charTest,
    (reads, value) => reads.reads(value),
    shouldBeAnError _
  )

  behavior of "JsonReaderInterpreter for byte"

  "JsonReaderInterpreter" should "find a json reads for bytes" in {
    byte.bind(jsonReads)
  }
  it should "read byte from JsNumber" in {
    val byteReads = byte.bind(jsonReads)

    val found = byteReads.reads(byteTest)
    val expect = JsSuccess(byteTest.value.toByteExact)
    found shouldBe expect
  }
  shouldFail[Byte, Reads, JsResult[Byte], JsValue](
    jsonReads,
    byte,
    "jsonReads",
    "byte",
    (allStrings ++ allNumbers :+ booleanTest) - byteTest,
    (reads, value) => reads.reads(value),
    shouldBeAnError _
  )

  behavior of "JsonReaderInterpreter for short"

  "JsonReaderInterpreter" should "find a json reads for shorts" in {
    short.bind(jsonReads)
  }
  it should "read short from JsNumber" in {
    val shortReads = short.bind(jsonReads)

    val found = shortReads.reads(shortTest)
    val expect = JsSuccess(shortTest.value.toShortExact)
    found shouldBe expect
  }
  it should "read byte from JsNumber using short reads" in {
    val shortReads = short.bind(jsonReads)

    val found = shortReads.reads(byteTest)
    val expect = JsSuccess(byteTest.value.toByteExact)
    found shouldBe expect
  }
  shouldFail[Short, Reads, JsResult[Short], JsValue](
    jsonReads,
    short,
    "jsonReads",
    "short",
    (allStrings ++ allNumbers :+ booleanTest) -- List(byteTest, shortTest),
    (reads, value) => reads.reads(value),
    shouldBeAnError _
  )

  behavior of "JsonReaderInterpreter for float"

  "JsonReaderInterpreter" should "find a json reads for floats" in {
    float.bind(jsonReads)
  }
  it should "read float from JsNumber" in {
    val floatReads = float.bind(jsonReads)

    val found = floatReads.reads(floatTest)
    val expect = JsSuccess(floatTest.value.floatValue())
    found shouldBe expect
  }
  it should "read the same than the default float reads" in {
    val derived: Reads[Float] = float.bind(jsonReads)
    val default: Reads[Float] = Reads.FloatReads

    val found = derived.reads(floatTest)
    val expect = default.reads(floatTest)

    found shouldBe expect
  }
  it should "read integer from JsNumber using float reads" in {
    val floatReads = float.bind(jsonReads)

    val found = floatReads.reads(integerTest)
    val expect = JsSuccess(integerTest.value.toIntExact)
    found shouldBe expect
  }
  it should "read byte from JsNumber using float reads" in {
    val floatReads = float.bind(jsonReads)

    val found = floatReads.reads(byteTest)
    val expect = JsSuccess(byteTest.value.toByteExact)
    found shouldBe expect
  }
  it should "read short from JsNumber using float reads" in {
    val floatReads = float.bind(jsonReads)

    val found = floatReads.reads(shortTest)
    val expect = JsSuccess(shortTest.value.toShortExact)
    found shouldBe expect
  }
  it should "read Float.PositiveInfinity from JsNumber > Float.MaxValue" in {
    val floatReads = float.bind(jsonReads)

    val found = floatReads.reads(bigDecimalTest)
    val expect = JsSuccess(Float.PositiveInfinity)
    found shouldBe expect
  }
  shouldFail[Float, Reads, JsResult[Float], JsValue](
    jsonReads,
    float,
    "jsonReads",
    "float",
    allStrings :+ booleanTest,
    (reads, value) => reads.reads(value),
    shouldBeAnError _
  )

  behavior of "JsonReaderInterpreter for BigDecimal"

  "JsonReaderInterpreter" should "find a json reads for bigDecimal" in {
    bigDecimal.bind(jsonReads)
  }
  it should "read bigDecimal from JsNumber" in {
    val bigDecimalReads = bigDecimal.bind(jsonReads)

    val found = bigDecimalReads.reads(bigDecimalTest)
    val expect = JsSuccess(bigDecimalTest.value)
    found shouldBe expect
  }
  it should "read the same than the default bigDecimal reads" in {
    val derived: Reads[BigDecimal] = bigDecimal.bind(jsonReads)
    val default: Reads[BigDecimal] = Reads.bigDecReads

    val found = derived.reads(bigDecimalTest)
    val expect = default.reads(bigDecimalTest)

    found shouldBe expect
  }
  it should "read integer from JsNumber using bigDecimal reads" in {
    val bigDecimalReads = bigDecimal.bind(jsonReads)

    val found = bigDecimalReads.reads(integerTest)
    val expect = JsSuccess(integerTest.value.toIntExact)
    found shouldBe expect
  }
  it should "read decimal from JsNumber using bigDecimal reads" in {
    val bigDecimalReads = bigDecimal.bind(jsonReads)

    val found = bigDecimalReads.reads(decimalTest)
    val expect = JsSuccess(decimalTest.value.doubleValue())
    found shouldBe expect
  }
  shouldFail[BigDecimal, Reads, JsResult[BigDecimal], JsValue](
    jsonReads,
    bigDecimal,
    "jsonReads",
    "bigDecimal",
    allStrings :+ booleanTest,
    (reads, value) => reads.reads(value),
    shouldBeAnError _
  )

  behavior of "JsonReaderInterpreter for Long"

  "JsonReaderInterpreter" should "find a json reads for long" in {
    long.bind(jsonReads)
  }
  it should "read long from JsNumber" in {
    val longReads = long.bind(jsonReads)

    val found = longReads.reads(longTest)
    val expect = JsSuccess(longTest.value.toLongExact)
    found shouldBe expect
  }
  it should "read the same than the default long reads" in {
    val derived: Reads[Long] = long.bind(jsonReads)
    val default: Reads[Long] = Reads.LongReads

    val found = derived.reads(longTest)
    val expect = default.reads(longTest)

    found shouldBe expect
  }
  it should "read byte from JsNumber using long reads" in {
    val longReads = long.bind(jsonReads)

    val found = longReads.reads(byteTest)
    val expect = JsSuccess(byteTest.value.toByteExact)
    found shouldBe expect
  }
  it should "read short from JsNumber using long reads" in {
    val longReads = long.bind(jsonReads)

    val found = longReads.reads(shortTest)
    val expect = JsSuccess(shortTest.value.toShortExact)
    found shouldBe expect
  }
  it should "read integer from JsNumber using long reads" in {
    val longReads = long.bind(jsonReads)

    val found = longReads.reads(integerTest)
    val expect = JsSuccess(integerTest.value.toIntExact)
    found shouldBe expect
  }
  shouldFail[Long, Reads, JsResult[Long], JsValue](
    jsonReads,
    long,
    "jsonReads",
    "long",
    allStrings ++ List(booleanTest, decimalTest, floatTest, bigDecimalTest, bigIntTest),
    (reads, value) => reads.reads(value),
    shouldBeAnError _
  )

  behavior of "JsonReaderInterpreter for bigInt"

  "JsonReaderInterpreter" should "find a json reads for bigInt" in {
    bigInt.bind(jsonReads)
  }
  it should "read bigInt from JsNumber" in {
    val bigIntReads = bigInt.bind(jsonReads)

    val found = bigIntReads.reads(bigIntTest)
    val expect = JsSuccess(bigIntTest.value.toBigIntExact().get)
    found shouldBe expect
  }
  it should "read integer from JsNumber using bigInt reads" in {
    val bigIntReads = bigInt.bind(jsonReads)

    val found = bigIntReads.reads(integerTest)
    val expect = JsSuccess(integerTest.value.toIntExact)
    found shouldBe expect
  }
  shouldFail[BigInt, Reads, JsResult[BigInt], JsValue](
    jsonReads,
    bigInt,
    "jsonReads",
    "bigInt",
    allStrings ++ List(booleanTest, decimalTest, floatTest, bigDecimalTest),
    (reads, value) => reads.reads(value),
    shouldBeAnError _
  )

  behavior of "JsonReaderInterpreter for dateTime"

  "JsonReaderInterpreter" should "find a json reads for dateTime" in {
    dateTime.bind(jsonReads)
  }
  it should "read dateTime from JsString" in {
    val dateTimeReads = dateTime.bind(jsonReads)

    val found = dateTimeReads.reads(dateTimeTest)
    val expect = JsSuccess(ZonedDateTime.parse(dateTimeTest.value))
    found shouldBe expect
  }
  it should "read datetime from JsNumber" in {
    val dateTimeReads = dateTime.bind(jsonReads)

    val found = dateTimeReads.reads(bigDecimalTest)
    val expect = JsSuccess(
      ZonedDateTime.ofInstant(Instant.ofEpochMilli(bigDecimalTest.value.toLong), ZoneOffset.UTC)
    )
    found shouldBe expect
  }
  it should "read the same than the default dateTime read" in {
    val derived: Reads[ZonedDateTime] = dateTime.bind(jsonReads)
    val default: Reads[ZonedDateTime] = Reads.DefaultZonedDateTimeReads

    val found = derived.reads(dateTimeTest)
    val expect = default.reads(dateTimeTest)

    found shouldBe expect
  }
  shouldFail[ZonedDateTime, Reads, JsResult[ZonedDateTime], JsValue](
    jsonReads,
    dateTime,
    "jsonReads",
    "dateTime",
    (allStrings :+ booleanTest) - dateTimeTest,
    (reads, value) => reads.reads(value),
    shouldBeAnError _
  )

  behavior of "JsonReaderInterpreter for time"

  "JsonReaderInterpreter" should "find a json reads for time" in {
    time.bind(jsonReads)
  }
  it should "read time from JsString" in {
    val timeReads = time.bind(jsonReads)

    val found = timeReads.reads(timeTest)
    val expect = JsSuccess(LocalTime.parse(timeTest.value))

    found shouldBe expect
  }
  it should "read the same than the default time read" in {
    val derived: Reads[LocalTime] = time.bind(jsonReads)
    val default: Reads[LocalTime] = Reads.DefaultLocalTimeReads

    val found = derived.reads(timeTest)
    val expect = default.reads(timeTest)

    found shouldBe expect
  }
  it should "read time from JsNumber within 0 - 86399999999999" in {
    val timeReads = time.bind(jsonReads)

    val found = timeReads.reads(integerTest)
    val expect = JsSuccess(
      LocalTime.ofNanoOfDay(integerTest.value.toLong)
    )
    found shouldBe expect
  }
  shouldFail[LocalTime, Reads, JsResult[LocalTime], JsValue](
    jsonReads,
    time,
    "jsonReads",
    "time",
    (allStrings ++ List(booleanTest, decimalTest, bigDecimalTest)) - timeTest,
    (reads, value) => reads.reads(value),
    shouldBeAnError _
  )

  behavior of "JsonReaderInterpreter for date"

  "JsonReaderInterpreter" should "find a json reads for date" in {
    date.bind(jsonReads)
  }
  it should "read date from JsString" in {
    val dateReads = date.bind(jsonReads)

    val found = dateReads.reads(dateTest)
    val expect = JsSuccess(LocalDate.parse(dateTest.value))
    found shouldBe expect
  }
  it should "read the same than the default date reads" in {
    val derived: Reads[LocalDate] = date.bind(jsonReads)
    val default: Reads[LocalDate] = Reads.DefaultLocalDateReads

    val found = derived.reads(dateTest)
    val expect = default.reads(dateTest)

    found shouldBe expect
  }
  it should "read date from JsNumber" in {
    val dateReads = date.bind(jsonReads)

    val found = dateReads.reads(bigDecimalTest)
    val expect = JsSuccess(LocalDate.now(
      Clock.fixed(Instant.ofEpochMilli(bigDecimalTest.value.toLong), ZoneOffset.UTC)
    ))
    found shouldBe expect
  }
  shouldFail[LocalDate, Reads, JsResult[LocalDate], JsValue](
    jsonReads,
    date,
    "jsonReads",
    "date",
    (allStrings :+ booleanTest) - dateTest,
    (reads, value) => reads.reads(value),
    shouldBeAnError _
  )
}
