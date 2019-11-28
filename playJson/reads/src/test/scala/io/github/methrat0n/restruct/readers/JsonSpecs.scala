package io.github.methrat0n.restruct.readers

import java.time._

import io.github.methrat0n.restruct.readers.json._
import io.github.methrat0n.restruct.schema.Interpreter.SimpleInterpreter
import io.github.methrat0n.restruct.schema.Schema._
import io.github.methrat0n.restruct.schema.{ Interpreter, Path, Schema }
import org.scalatest.{ FlatSpec, Matchers, Outcome, Succeeded }
import play.api.libs.json._

import scala.collection.mutable.ListBuffer
import scala.util.{ Failure, Success, Try }

class JsonSpecs extends FlatSpec with Matchers {

  private val stringTest: JsString = JsString("methrat0n")
  private val decimalTest: JsNumber = JsNumber(BigDecimal(1343333333333333.2d))
  private val integerTest: JsNumber = JsNumber(BigDecimal(33333))
  private val booleanTest: JsBoolean = JsBoolean(true)
  private val charTest: JsString = JsString('a'.toString)
  private val byteTest: JsNumber = JsNumber(BigDecimal(123))
  private val shortTest: JsNumber = JsNumber(BigDecimal(23456))
  private val floatTest: JsNumber = JsNumber(WorkAround.Intern.decimal(12.2f))
  private val bigDecimalTest: JsNumber = JsNumber(BigDecimal("1267888889999999999999999999998888867699767611111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111.1"))
  private val longTest: JsNumber = JsNumber(BigDecimal(1267888889999999999L))
  private val bigIntTest: JsNumber = JsNumber(BigDecimal(BigInt("1267888889999999999999999999998888867699767611111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111")))
  private val dateTimeTest: JsString = JsString("2018-12-26T00:08:16.025415+01:00[Europe/Paris]")
  private val timeTest: JsString = JsString("00:08:16.025415")
  private val dateTest: JsString = JsString("2018-12-26")

  private val emptyList = JsArray.empty
  private val stringList = Json.arr("string", "string")
  private val intList = Json.arr(0, 1)

  private val requiredString = (Path \ "string").as[String]()
  private val complexRequiredString = (Path \ "level one" \ "level two").as[String]()
  private val complexRequiredStringWithIndex = (Path \ "string" \ 0).as[String]()
  private val optionalString = (Path \ "string").asOption[String]()

  private val deepStringTest = Json.obj(
    "string" -> "a string"
  )
  private val deeperStringTest = Json.obj(
    "level one" -> Json.obj(
      "level two" -> "a string"
    )
  )
  private val deepIndexedStringTest = Json.obj(
    "string" -> Json.arr(
      "a string"
    )
  )
  private val instanceTest = Json.obj(
    "string" -> "string",
    "int" -> 0
  )

  private val complexeInstanceTest = Json.obj(
    "str" -> "string",
    "int" -> Json.arr(
      Json.obj(
        "test" -> 100
      )
    )
  )

  //This exist to be able to call BigDecimal.apply without a warning being raised. When a SuppressWarning or equivalent is added to scala
  //this code should be removed
  object WorkAround { @deprecated("", "") class Intern { def decimal(f: Float): BigDecimal = BigDecimal(f) }; object Intern extends Intern }

  private def shouldBeAnError[T](found: Try[JsResult[T]]) = found match {
    case Failure(_) | Success(JsError(_)) => Succeeded
    case _                                => throw new RuntimeException(s"this test should have return an error but instead return $found")
  }

  import language.higherKinds
  private def shouldFail[T, Form[_]: Inter, Err, Value, Inter[Fomat[_]] <: Interpreter[Fomat, T]](
    localSchema: Schema[T, Inter],
    formatName: String,
    localFormat: String,
    failures: ListBuffer[Value],
    test: (Form[T], Value) => Err,
    errorSwitch: Try[Err] => Outcome
  ) = {
    failures.foreach(failure => {
      it should s"not read $failure with $localFormat $formatName" in {
        errorSwitch(Try(test(localSchema.bind[Form], failure)))
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
    simpleString.bind[Reads]
  }
  it should "read string from JsString" in {
    val stringReads = simpleString.bind[Reads]

    val found = stringReads.reads(stringTest)
    val expect = JsSuccess(stringTest.value)
    found shouldBe expect
  }
  it should "read the same than the default string reads" in {
    val derived: Reads[String] = simpleString.bind[Reads]
    val default: Reads[String] = Reads.StringReads

    val found = derived.reads(stringTest)
    val expect = default.reads(stringTest)

    found shouldBe expect
  }
  shouldFail[String, Reads, JsResult[String], JsValue, SimpleInterpreter[?[_], String]](
    simpleString,
    "jsonReads",
    "string",
    allNumbers :+ booleanTest,
    (reads, value) => reads.reads(value),
    shouldBeAnError _
  )

  behavior of "JsonReaderInterpreter for decimal"

  "JsonReaderInterpreter" should "find a json reads for decimals" in {
    simple[Double].bind[Reads]
  }
  it should "read decimal from JsNumber" in {
    val decimalReads = simple[Double].bind[Reads]

    val found = decimalReads.reads(decimalTest)
    val expect = JsSuccess(decimalTest.value.toDouble)
    found shouldBe expect
  }
  it should "read the same than the default decimal reads" in {
    val derived: Reads[Double] = simple[Double].bind[Reads]
    val default: Reads[Double] = Reads.DoubleReads

    val found = derived.reads(decimalTest)
    val expect = default.reads(decimalTest)

    found shouldBe expect
  }
  shouldFail[Double, Reads, JsResult[Double], JsValue, SimpleInterpreter[?[_], Double]](
    simple[Double],
    "jsonReads",
    "decimal",
    allStrings :+ booleanTest,
    (reads, value) => reads.reads(value),
    shouldBeAnError _
  )

  behavior of "JsonReaderInterpreter for integer"

  "JsonReaderInterpreter" should "find a json reads for integers" in {
    simple[Int].bind[Reads]
  }
  it should "read integer from JsNumber" in {
    val integerReads = simple[Int].bind[Reads]

    val found = integerReads.reads(integerTest)
    val expect = JsSuccess(integerTest.value.toIntExact)
    found shouldBe expect
  }
  it should "read the same than the default integer reads" in {
    val derived: Reads[Int] = simple[Int].bind[Reads]
    val default: Reads[Int] = Reads.IntReads

    val found = derived.reads(integerTest)
    val expect = default.reads(integerTest)

    found shouldBe expect
  }
  shouldFail[Int, Reads, JsResult[Int], JsValue, SimpleInterpreter[?[_], Int]](
    simple[Int],
    "jsonReads",
    "integer",
    (allStrings :+ booleanTest) ++ List(decimalTest, floatTest, bigDecimalTest),
    (reads, value) => reads.reads(value),
    shouldBeAnError _
  )

  behavior of "JsonReaderInterpreter for boolean"

  "JsonReaderInterpreter" should "find a json reads for booleans" in {
    simple[Boolean].bind[Reads]
  }
  it should "read boolean from JsBoolean" in {
    val booleanReads = simple[Boolean].bind[Reads]

    val found = booleanReads.reads(booleanTest)
    val expect = JsSuccess(booleanTest.value)
    found shouldBe expect
  }
  it should "read the same than the default boolean reads" in {
    val derived: Reads[Boolean] = simple[Boolean].bind[Reads]
    val default: Reads[Boolean] = Reads.BooleanReads

    val found = derived.reads(booleanTest)
    val expect = default.reads(booleanTest)

    found shouldBe expect
  }
  shouldFail[Boolean, Reads, JsResult[Boolean], JsValue, SimpleInterpreter[?[_], Boolean]](
    simple[Boolean],
    "jsonReads",
    "boolean",
    allStrings ++ allNumbers,
    (reads, value) => reads.reads(value),
    shouldBeAnError _
  )

  behavior of "JsonReaderInterpreter for char"

  "JsonReaderInterpreter" should "find a json reads for chars" in {
    simple[Char].bind[Reads]
  }
  it should "reads char from JsString" in {
    val charReads = simple[Char].bind[Reads]

    val found = charReads.reads(charTest)
    val expect = JsSuccess(charTest.value.charAt(0))
    found shouldBe expect
  }
  shouldFail[Char, Reads, JsResult[Char], JsValue, SimpleInterpreter[?[_], Char]](
    simple[Char],
    "jsonReads",
    "char",
    (allStrings ++ allNumbers :+ booleanTest) -= charTest,
    (reads, value) => reads.reads(value),
    shouldBeAnError _
  )

  behavior of "JsonReaderInterpreter for byte"

  "JsonReaderInterpreter" should "find a json reads for bytes" in {
    simple[Byte].bind[Reads]
  }
  it should "read byte from JsNumber" in {
    val byteReads = simple[Byte].bind[Reads]

    val found = byteReads.reads(byteTest)
    val expect = JsSuccess(byteTest.value.toByteExact)
    found shouldBe expect
  }
  shouldFail[Byte, Reads, JsResult[Byte], JsValue, SimpleInterpreter[?[_], Byte]](
    simple[Byte],
    "jsonReads",
    "byte",
    (allStrings ++ allNumbers :+ booleanTest) -= byteTest,
    (reads, value) => reads.reads(value),
    shouldBeAnError _
  )

  behavior of "JsonReaderInterpreter for short"

  "JsonReaderInterpreter" should "find a json reads for shorts" in {
    simple[Short].bind[Reads]
  }
  it should "read short from JsNumber" in {
    val shortReads = simple[Short].bind[Reads]

    val found = shortReads.reads(shortTest)
    val expect = JsSuccess(shortTest.value.toShortExact)
    found shouldBe expect
  }
  it should "read byte from JsNumber using short reads" in {
    val shortReads = simple[Short].bind[Reads]

    val found = shortReads.reads(byteTest)
    val expect = JsSuccess(byteTest.value.toByteExact)
    found shouldBe expect
  }
  shouldFail[Short, Reads, JsResult[Short], JsValue, SimpleInterpreter[?[_], Short]](
    simple[Short],
    "jsonReads",
    "short",
    (allStrings ++ allNumbers :+ booleanTest) --= List(byteTest, shortTest),
    (reads, value) => reads.reads(value),
    shouldBeAnError _
  )

  behavior of "JsonReaderInterpreter for float"

  "JsonReaderInterpreter" should "find a json reads for floats" in {
    simple[Float].bind[Reads]
  }
  it should "read float from JsNumber" in {
    val floatReads = simple[Float].bind[Reads]

    val found = floatReads.reads(floatTest)
    val expect = JsSuccess(floatTest.value.floatValue)
    found shouldBe expect
  }
  it should "read the same than the default float reads" in {
    val derived: Reads[Float] = simple[Float].bind[Reads]
    val default: Reads[Float] = Reads.FloatReads

    val found = derived.reads(floatTest)
    val expect = default.reads(floatTest)

    found shouldBe expect
  }
  it should "read integer from JsNumber using float reads" in {
    val floatReads = simple[Float].bind[Reads]

    val found = floatReads.reads(integerTest)
    val expect = JsSuccess(integerTest.value.toIntExact)
    found shouldBe expect
  }
  it should "read byte from JsNumber using float reads" in {
    val floatReads = simple[Float].bind[Reads]

    val found = floatReads.reads(byteTest)
    val expect = JsSuccess(byteTest.value.toByteExact)
    found shouldBe expect
  }
  it should "read short from JsNumber using float reads" in {
    val floatReads = simple[Float].bind[Reads]

    val found = floatReads.reads(shortTest)
    val expect = JsSuccess(shortTest.value.toShortExact)
    found shouldBe expect
  }
  it should "read Float.PositiveInfinity from JsNumber > Float.MaxValue" in {
    val floatReads = simple[Float].bind[Reads]

    val found = floatReads.reads(bigDecimalTest)
    val expect = JsSuccess(Float.PositiveInfinity)
    found shouldBe expect
  }
  shouldFail[Float, Reads, JsResult[Float], JsValue, SimpleInterpreter[?[_], Float]](
    simple[Float],
    "jsonReads",
    "float",
    allStrings :+ booleanTest,
    (reads, value) => reads.reads(value),
    shouldBeAnError _
  )

  behavior of "JsonReaderInterpreter for BigDecimal"

  "JsonReaderInterpreter" should "find a json reads for bigDecimal" in {
    simpleBigDecimal.bind[Reads]
  }
  it should "read bigDecimal from JsNumber" in {
    val bigDecimalReads = simpleBigDecimal.bind[Reads]

    val found = bigDecimalReads.reads(bigDecimalTest)
    val expect = JsSuccess(bigDecimalTest.value)
    found shouldBe expect
  }
  it should "read the same than the default bigDecimal reads" in {
    val derived: Reads[BigDecimal] = simpleBigDecimal.bind[Reads]
    val default: Reads[BigDecimal] = Reads.bigDecReads

    val found = derived.reads(bigDecimalTest)
    val expect = default.reads(bigDecimalTest)

    found shouldBe expect
  }
  it should "read integer from JsNumber using bigDecimal reads" in {
    val bigDecimalReads = simpleBigDecimal.bind[Reads]

    val found = bigDecimalReads.reads(integerTest)
    val expect = JsSuccess(integerTest.value.toIntExact)
    found shouldBe expect
  }
  it should "read decimal from JsNumber using bigDecimal reads" in {
    val bigDecimalReads = simpleBigDecimal.bind[Reads]

    val found = bigDecimalReads.reads(decimalTest)
    val expect = JsSuccess(decimalTest.value.doubleValue)
    found shouldBe expect
  }
  shouldFail[BigDecimal, Reads, JsResult[BigDecimal], JsValue, SimpleInterpreter[?[_], BigDecimal]](
    simpleBigDecimal,
    "jsonReads",
    "bigDecimal",
    allStrings :+ booleanTest,
    (reads, value) => reads.reads(value),
    shouldBeAnError _
  )

  behavior of "JsonReaderInterpreter for Long"

  "JsonReaderInterpreter" should "find a json reads for long" in {
    simple[Long].bind[Reads]
  }
  it should "read long from JsNumber" in {
    val longReads = simple[Long].bind[Reads]

    val found = longReads.reads(longTest)
    val expect = JsSuccess(longTest.value.toLongExact)
    found shouldBe expect
  }
  it should "read the same than the default long reads" in {
    val derived: Reads[Long] = simple[Long].bind[Reads]
    val default: Reads[Long] = Reads.LongReads

    val found = derived.reads(longTest)
    val expect = default.reads(longTest)

    found shouldBe expect
  }
  it should "read byte from JsNumber using long reads" in {
    val longReads = simple[Long].bind[Reads]

    val found = longReads.reads(byteTest)
    val expect = JsSuccess(byteTest.value.toByteExact)
    found shouldBe expect
  }
  it should "read short from JsNumber using long reads" in {
    val longReads = simple[Long].bind[Reads]

    val found = longReads.reads(shortTest)
    val expect = JsSuccess(shortTest.value.toShortExact)
    found shouldBe expect
  }
  it should "read integer from JsNumber using long reads" in {
    val longReads = simple[Long].bind[Reads]

    val found = longReads.reads(integerTest)
    val expect = JsSuccess(integerTest.value.toIntExact)
    found shouldBe expect
  }
  shouldFail[Long, Reads, JsResult[Long], JsValue, SimpleInterpreter[?[_], Long]](
    simple[Long],
    "jsonReads",
    "long",
    allStrings ++ List(booleanTest, decimalTest, floatTest, bigDecimalTest, bigIntTest),
    (reads, value) => reads.reads(value),
    shouldBeAnError _
  )

  behavior of "JsonReaderInterpreter for bigInt"

  "JsonReaderInterpreter" should "find a json reads for bigInt" in {
    simpleBigInt.bind[Reads]
  }
  it should "read bigInt from JsNumber" in {
    val bigIntReads = simpleBigInt.bind[Reads]

    val found = bigIntReads.reads(bigIntTest)
    val expect = JsSuccess(bigIntTest.value.toBigIntExact.get)
    found shouldBe expect
  }
  it should "read integer from JsNumber using bigInt reads" in {
    val bigIntReads = simpleBigInt.bind[Reads]

    val found = bigIntReads.reads(integerTest)
    val expect = JsSuccess(integerTest.value.toIntExact)
    found shouldBe expect
  }
  shouldFail[BigInt, Reads, JsResult[BigInt], JsValue, SimpleInterpreter[?[_], BigInt]](
    simpleBigInt,
    "jsonReads",
    "bigInt",
    allStrings ++ List(booleanTest, decimalTest, floatTest, bigDecimalTest),
    (reads, value) => reads.reads(value),
    shouldBeAnError _
  )

  behavior of "JsonReaderInterpreter for dateTime"

  "JsonReaderInterpreter" should "find a json reads for dateTime" in {
    simpleZDT.bind[Reads]
  }
  it should "read dateTime from JsString" in {
    val dateTimeReads = simpleZDT.bind[Reads]

    val found = dateTimeReads.reads(dateTimeTest)
    val expect = JsSuccess(ZonedDateTime.parse(dateTimeTest.value))
    found shouldBe expect
  }
  it should "read the same than the default dateTime read" in {
    val derived: Reads[ZonedDateTime] = simpleZDT.bind[Reads]
    val default: Reads[ZonedDateTime] = Reads.DefaultZonedDateTimeReads

    val found = derived.reads(dateTimeTest)
    val expect = default.reads(dateTimeTest)

    found shouldBe expect
  }
  shouldFail[ZonedDateTime, Reads, JsResult[ZonedDateTime], JsValue, SimpleInterpreter[?[_], ZonedDateTime]](
    simpleZDT,
    "jsonReads",
    "dateTime",
    (allStrings :+ booleanTest) -= dateTimeTest,
    (reads, value) => reads.reads(value),
    shouldBeAnError _
  )

  behavior of "JsonReaderInterpreter for time"

  "JsonReaderInterpreter" should "find a json reads for time" in {
    simpleT.bind[Reads]
  }
  it should "read time from JsString" in {
    val timeReads = simpleT.bind[Reads]

    val found = timeReads.reads(timeTest)
    val expect = JsSuccess(LocalTime.parse(timeTest.value))

    found shouldBe expect
  }
  it should "read the same than the default time read" in {
    val derived: Reads[LocalTime] = simpleT.bind[Reads]
    val default: Reads[LocalTime] = Reads.DefaultLocalTimeReads

    val found = derived.reads(timeTest)
    val expect = default.reads(timeTest)

    found shouldBe expect
  }
  it should "read time from JsNumber within 0 - 86399999999999" in {
    val timeReads = simpleT.bind[Reads]

    val found = timeReads.reads(integerTest)
    val expect = JsSuccess(
      LocalTime.ofNanoOfDay(integerTest.value.toLong)
    )
    found shouldBe expect
  }
  shouldFail[LocalTime, Reads, JsResult[LocalTime], JsValue, SimpleInterpreter[?[_], LocalTime]](
    simpleT,
    "jsonReads",
    "time",
    (allStrings ++ List(booleanTest, decimalTest, bigDecimalTest)) -= timeTest,
    (reads, value) => reads.reads(value),
    shouldBeAnError _
  )

  behavior of "JsonReaderInterpreter for date"

  "JsonReaderInterpreter" should "find a json reads for date" in {
    simpleD.bind[Reads]
  }
  it should "read date from JsString" in {
    val dateReads = simpleD.bind[Reads]

    val found = dateReads.reads(dateTest)
    val expect = JsSuccess(LocalDate.parse(dateTest.value))
    found shouldBe expect
  }
  it should "read the same than the default date reads" in {
    val derived: Reads[LocalDate] = simpleD.bind[Reads]
    val default: Reads[LocalDate] = Reads.DefaultLocalDateReads

    val found = derived.reads(dateTest)
    val expect = default.reads(dateTest)

    found shouldBe expect
  }
  shouldFail[LocalDate, Reads, JsResult[LocalDate], JsValue, SimpleInterpreter[?[_], LocalDate]](
    simpleD,
    "jsonReads",
    "date",
    (allStrings :+ booleanTest) -= dateTest,
    (reads, value) => reads.reads(value),
    shouldBeAnError _
  )

  behavior of "JsonReaderInterpreter for empty list"

  it should "read an empty list from an empty JsArray" in {
    val emptyStringListReader = Schema.many[String, List]().bind[Reads]

    val found = emptyStringListReader.reads(emptyList)
    val expect = JsSuccess(List.empty)
    found shouldBe expect
  }

  it should "read a string list from a JsArray of JsString" in {
    val stringListReader = Schema.many[String, List]().bind[Reads]

    val found = stringListReader.reads(stringList)
    val expect = JsSuccess(List("string", "string"))
    found shouldBe expect
  }

  it should "read an int list  from a JsArray of JsNumber" in {
    val intListReader = Schema.many[Int, List]().bind[Reads]

    val found = intListReader.reads(intList)
    val expect = JsSuccess(List(0, 1))
    found shouldBe expect
  }

  behavior of "PlayJson Reads fields"

  it should "read an object which contains a required string" in {
    val requiredStringReader = requiredString.bind[Reads]

    val found = requiredStringReader.reads(deepStringTest)
    val expect = JsSuccess("a string", JsPath \ "string")
    found shouldBe expect
  }
  it should "read a second level of object if it's describe in the path" in {
    val complexRequiredStringReader = complexRequiredString.bind[Reads]

    val found = complexRequiredStringReader.reads(deeperStringTest)
    val expect = JsSuccess("a string", JsPath \ "level one" \ "level two")
    found shouldBe expect
  }
  it should "read an array if it's describe in the path" in {
    val complexRequiredStringWithIndexReader = complexRequiredStringWithIndex.bind[Reads]

    val found = complexRequiredStringWithIndexReader.reads(deepIndexedStringTest)
    val expect = JsSuccess("a string", JsPath \ "string" \ 0)
    found shouldBe expect
  }
  it should "read optional string if present" in {
    val optionalStringReader = optionalString.bind[Reads]

    val found = optionalStringReader.reads(deepStringTest)
    val expect = JsSuccess(Some("a string"), JsPath \ "string")
    found shouldBe expect
  }
  it should "read optional string if abscent" in {
    val optionalStringReader = optionalString.bind[Reads]

    val found = optionalStringReader.reads(JsObject.empty)
    val expect = JsSuccess(None)
    found shouldBe expect
  }
  it should "read case class instances" in {
    val caseClassReader = RequiredStringAndInt.schema.bind[Reads]

    val found = caseClassReader.reads(instanceTest)
    val expect = JsSuccess(RequiredStringAndInt("string", 0))
    found shouldBe expect
  }

  it should "read case class complexe instances" in {
    val caseClassReader = ComplexeCase.schema.bind[Reads]

    val found = caseClassReader.reads(complexeInstanceTest)
    val expect = JsSuccess(ComplexeCase("string", 100, None))
    found shouldBe expect
  }
}

final case class RequiredStringAndInt(string: String, int: Int)
object RequiredStringAndInt {
  import language.postfixOps
  implicit val schema = (
    (Path \ "string").as[String]() and
    (Path \ "int").as[Int]()
  ).inmap(RequiredStringAndInt.apply _ tupled)(RequiredStringAndInt.unapply _ andThen (_.get))
}

final case class ComplexeCase(str: String, int: Int, help: Option[String])

object ComplexeCase {
  implicit val schema = (
    (Path \ "str").as[String]() and
    (Path \ "int" \ 0 \ "test").as[Int]() and
    (Path \ "help").asOption[String]()
  ).inmap {
      case ((str, int), help) => ComplexeCase(str, int, help)
    } {
      case ComplexeCase(str, int, help) => ((str, int), help)
    }
}
