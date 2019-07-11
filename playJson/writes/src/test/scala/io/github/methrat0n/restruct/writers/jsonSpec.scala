package io.github.methrat0n.restruct.writers

import java.time.{ LocalDate, LocalTime, ZonedDateTime }

import io.github.methrat0n.restruct.schema.{ Path, Schema }
import io.github.methrat0n.restruct.schema.Schema._
import io.github.methrat0n.restruct.writers.json._
import org.scalatest.{ FlatSpec, Matchers }
import play.api.libs.json._

class jsonSpec extends FlatSpec with Matchers {

  private val stringTest: String = "methrat0n"
  private val decimalTest: Double = 12678888899999999999999999999988888676997676.2d
  private val integerTest: Int = 33333
  private val booleanTest: Boolean = true
  private val charTest: Char = 'a'
  private val byteTest: Byte = 123
  private val shortTest: Short = 23456
  private val floatTest: Float = 12.2f
  private val bigDecimalTest: BigDecimal = BigDecimal("1267888889999999999999999999998888867699767611111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111.1")
  private val longTest: Long = 1267888889999999999L
  private val bigIntTest: BigInt = BigInt("1267888889999999999999999999998888867699767611111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111")
  private val dateTimeTest: ZonedDateTime = ZonedDateTime.parse("2018-12-26T00:08:16.025415+01:00[Europe/Paris]")
  private val timeTest: LocalTime = LocalTime.parse("00:08:16.025415")
  private val dateTest: LocalDate = LocalDate.parse("2018-12-26")

  private val emptyList = List.empty
  private val stringList = List("string", "string")
  private val intList = List(0, 1)

  private val requiredString = (Path \ "string").as[String]()
  private val complexRequiredString = (Path \ "level one" \ "level two").as[String]()
  //private val complexRequiredStringWithIndex = ("string" \ 0).as[String]
  private val optionalString = (Path \ "string").asOption[String]()

  //This exist to be able to call BigDecimal.apply without a warning being raised. When a SuppressWarning or equivalent is added to scala
  //this code should be removed
  object WorkAround { @deprecated("", "") class Intern { def decimal(f: Float): BigDecimal = BigDecimal(f) }; object Intern extends Intern }

  behavior of "JsonWriterInterpreter for strings"

  "JsonWriterInterpreter" should "find a json writer for strings" in {
    simpleString.bind[Writes]
  }
  it should "write string as JsString" in {
    val stringWriter = simpleString.bind[Writes]

    val found = stringWriter.writes(stringTest)
    val expect = JsString(stringTest)
    found shouldBe expect
  }
  it should "write the same than the default string writer" in {
    val derived: Writes[String] = simpleString.bind[Writes]
    val default: Writes[String] = Writes.StringWrites

    val found = derived.writes(stringTest)
    val expect = default.writes(stringTest)

    found shouldBe expect
  }

  behavior of "JsonWriterInterpreter for decimal"

  "JsonWriterInterpreter" should "find a json writer for decimals" in {
    simple[Double].bind[Writes]
  }
  it should "write decimal as JsNumber" in {
    val decimalWriter = simple[Double].bind[Writes]

    val found = decimalWriter.writes(decimalTest)
    val expect = JsNumber(decimalTest)
    found shouldBe expect
  }
  it should "write the same than the default decimal writer" in {
    val derived: Writes[Double] = simple[Double].bind[Writes]
    val default: Writes[Double] = Writes.DoubleWrites

    val found = derived.writes(decimalTest)
    val expect = default.writes(decimalTest)

    found shouldBe expect
  }
  it should "write integer as JsNumber using decimal writer" in {
    val decimalWriter = simple[Double].bind[Writes]

    val found = decimalWriter.writes(integerTest)
    val expect = JsNumber(integerTest)
    found shouldBe expect
  }
  it should "write float as JsNumber using decimal writer" in {
    val decimalWriter = simple[Double].bind[Writes]

    val found = decimalWriter.writes(floatTest)
    val expect = JsNumber(WorkAround.Intern.decimal(floatTest))
    found shouldBe expect
  }
  it should "write byte as JsNumber using decimal writer" in {
    val decimalWriter = simple[Double].bind[Writes]

    val found = decimalWriter.writes(byteTest)
    val expect = JsNumber(BigDecimal(byteTest))
    found shouldBe expect
  }
  it should "write short as JsNumber using decimal writer" in {
    val decimalWriter = simple[Double].bind[Writes]

    val found = decimalWriter.writes(shortTest)
    val expect = JsNumber(BigDecimal(shortTest))
    found shouldBe expect
  }

  behavior of "JsonWriterInterpreter for integer"

  "JsonWriterInterpreter" should "find a json writer for integers" in {
    simple[Int].bind[Writes]
  }
  it should "write integer as JsNumber" in {
    val integerWriter = simple[Int].bind[Writes]

    val found = integerWriter.writes(integerTest)
    val expect = JsNumber(integerTest)
    found shouldBe expect
  }
  it should "write the same than the default integer writer" in {
    val derived: Writes[Int] = simple[Int].bind[Writes]
    val default: Writes[Int] = Writes.IntWrites

    val found = derived.writes(integerTest)
    val expect = default.writes(integerTest)

    found shouldBe expect
  }
  it should "write byte as JsNumber using integer writer" in {
    val integerWriter = simple[Int].bind[Writes]

    val found = integerWriter.writes(byteTest)
    val expect = JsNumber(BigDecimal(byteTest))
    found shouldBe expect
  }
  it should "write short as JsNumber using integer writer" in {
    val integerWriter = simple[Int].bind[Writes]

    val found = integerWriter.writes(shortTest)
    val expect = JsNumber(BigDecimal(shortTest))
    found shouldBe expect
  }

  behavior of "JsonWriterInterpreter for boolean"

  "JsonWriterInterpreter" should "find a json writer for booleans" in {
    simple[Boolean].bind[Writes]
  }
  it should "write boolean as JsBoolean" in {
    val booleanWriter = simple[Boolean].bind[Writes]

    val found = booleanWriter.writes(booleanTest)
    val expect = JsBoolean(booleanTest)
    found shouldBe expect
  }
  it should "write the same than the default boolean writer" in {
    val derived: Writes[Boolean] = simple[Boolean].bind[Writes]
    val default: Writes[Boolean] = Writes.BooleanWrites

    val found = derived.writes(booleanTest)
    val expect = default.writes(booleanTest)

    found shouldBe expect
  }

  behavior of "JsonWriterInterpreter for char"

  "JsonWriterInterpreter" should "find a json writer for chars" in {
    simple[Char].bind[Writes]
  }
  it should "write char as JsString" in {
    val charWriter = simple[Char].bind[Writes]

    val found = charWriter.writes(charTest)
    val expect = JsString(charTest.toString)
    found shouldBe expect
  }

  behavior of "JsonWriterInterpreter for byte"

  "JsonWriterInterpreter" should "find a json writer for bytes" in {
    simple[Byte].bind[Writes]
  }
  it should "write byte as JsNumber" in {
    val byteWriter = simple[Byte].bind[Writes]

    val found = byteWriter.writes(byteTest)
    val expect = JsNumber(BigDecimal(byteTest))
    found shouldBe expect
  }

  behavior of "JsonWriterInterpreter for short"

  "JsonWriterInterpreter" should "find a json writer for shorts" in {
    simple[Short].bind[Writes]
  }
  it should "write short as JsNumber" in {
    val shortWriter = simple[Short].bind[Writes]

    val found = shortWriter.writes(shortTest)
    val expect = JsNumber(BigDecimal(shortTest))
    found shouldBe expect
  }

  behavior of "JsonWriterInterpreter for float"

  "JsonWriterInterpreter" should "find a json writer for floats" in {
    simple[Float].bind[Writes]
  }
  it should "write float as JsNumber" in {
    val floatWriter = simple[Float].bind[Writes]

    val found = floatWriter.writes(floatTest)
    val expect = JsNumber(WorkAround.Intern.decimal(floatTest))
    found shouldBe expect
  }
  it should "write the same than the default float writer" in {
    val derived: Writes[Float] = simple[Float].bind[Writes]
    val default: Writes[Float] = Writes.FloatWrites

    val found = derived.writes(floatTest)
    val expect = default.writes(floatTest)

    found shouldBe expect
  }
  it should "write integer as JsNumber using float writer" in {
    val floatWriter = simple[Float].bind[Writes]

    val found = floatWriter.writes(integerTest)
    val expect = JsNumber(integerTest)
    found shouldBe expect
  }
  it should "write byte as JsNumber using float writer" in {
    val floatWriter = simple[Float].bind[Writes]

    val found = floatWriter.writes(byteTest)
    val expect = JsNumber(BigDecimal(byteTest))
    found shouldBe expect
  }
  it should "write short as JsNumber using float writer" in {
    val floatWriter = simple[Float].bind[Writes]

    val found = floatWriter.writes(shortTest)
    val expect = JsNumber(BigDecimal(shortTest))
    found shouldBe expect
  }

  behavior of "JsonWriterInterpreter for BigDecimal"

  "JsonWriterInterpreter" should "find a json writer for bigDecimal" in {
    simpleBigDecimal.bind[Writes]
  }
  it should "write bigDecimal as JsNumber" in {
    val bigDecimalWriter = simpleBigDecimal.bind[Writes]

    val found = bigDecimalWriter.writes(bigDecimalTest)
    val expect = JsNumber(bigDecimalTest)
    found shouldBe expect
  }
  it should "write the same than the default bigDecimal writer" in {
    val derived: Writes[BigDecimal] = simpleBigDecimal.bind[Writes]
    val default: Writes[BigDecimal] = Writes.BigDecimalWrites

    val found = derived.writes(bigDecimalTest)
    val expect = default.writes(bigDecimalTest)

    found shouldBe expect
  }
  it should "write integer as JsNumber using bigDecimal writer" in {
    val bigDecimalWriter = simpleBigDecimal.bind[Writes]

    val found = bigDecimalWriter.writes(integerTest)
    val expect = JsNumber(integerTest)
    found shouldBe expect
  }
  it should "write decimal as JsNumber using bigDecimal writer" in {
    val bigDecimalWriter = simpleBigDecimal.bind[Writes]

    val found = bigDecimalWriter.writes(decimalTest)
    val expect = JsNumber(decimalTest)
    found shouldBe expect
  }

  behavior of "JsonWriterInterpreter for Long"

  "JsonWriterInterpreter" should "find a json writer for long" in {
    simple[Long].bind[Writes]
  }
  it should "write long as JsNumber" in {
    val longWriter = simple[Long].bind[Writes]

    val found = longWriter.writes(longTest)
    val expect = JsNumber(longTest)
    found shouldBe expect
  }
  it should "write the same than the default long writer" in {
    val derived: Writes[Long] = simple[Long].bind[Writes]
    val default: Writes[Long] = Writes.LongWrites

    val found = derived.writes(longTest)
    val expect = default.writes(longTest)

    found shouldBe expect
  }
  it should "write byte as JsNumber using long writer" in {
    val longWriter = simple[Long].bind[Writes]

    val found = longWriter.writes(byteTest)
    val expect = JsNumber(BigDecimal(byteTest))
    found shouldBe expect
  }
  it should "write short as JsNumber using long writer" in {
    val longWriter = simple[Long].bind[Writes]

    val found = longWriter.writes(shortTest)
    val expect = JsNumber(BigDecimal(shortTest))
    found shouldBe expect
  }
  it should "write integer as JsNumber using long writer" in {
    val longWriter = simple[Long].bind[Writes]

    val found = longWriter.writes(integerTest)
    val expect = JsNumber(integerTest)
    found shouldBe expect
  }

  behavior of "JsonWriterInterpreter for bigInt"

  "JsonWriterInterpreter" should "find a json writer for bigInt" in {
    simpleBigInt.bind[Writes]
  }
  it should "write bigInt as JsNumber" in {
    val bigIntWriter = simpleBigInt.bind[Writes]

    val found = bigIntWriter.writes(bigIntTest)
    val expect = JsNumber(BigDecimal(bigIntTest))
    found shouldBe expect
  }
  it should "write integer as JsNumber using bigInt writer" in {
    val bigIntWriter = simpleBigInt.bind[Writes]

    val found = bigIntWriter.writes(integerTest)
    val expect = JsNumber(BigDecimal(integerTest))
    found shouldBe expect
  }

  behavior of "JsonWriterInterpreter for dateTime"

  "JsonWriterInterpreter" should "find a json writer for dateTime" in {
    simpleZDT.bind[Writes]
  }
  it should "write dateTime as JsString" in {
    val dateTimeWriter = simpleZDT.bind[Writes]

    val found = dateTimeWriter.writes(dateTimeTest)
    val expect = JsString(dateTimeTest.toString)
    found shouldBe expect
  }
  it should "write the same than the default dateTime writer" in {
    val derived: Writes[ZonedDateTime] = simpleZDT.bind[Writes]
    val default: Writes[ZonedDateTime] = Writes.DefaultZonedDateTimeWrites

    val found = derived.writes(dateTimeTest)
    val expect = default.writes(dateTimeTest)

    found shouldBe expect
  }

  behavior of "JsonWriterInterpreter for time"

  "JsonWriterInterpreter" should "find a json writer for time" in {
    simpleT.bind[Writes]
  }
  it should "write time as JsString" in {
    val timeWriter = simpleT.bind[Writes]

    val found = timeWriter.writes(timeTest)
    val expect = JsString(timeTest.toString)

    found shouldBe expect
  }
  it should "write the same than the default time writer" in {
    val derived: Writes[LocalTime] = simpleT.bind[Writes]
    val default: Writes[LocalTime] = Writes.DefaultLocalTimeWrites

    val found = derived.writes(timeTest)
    val expect = default.writes(timeTest)

    found shouldBe expect
  }

  behavior of "JsonWriterInterpreter for date"

  "JsonWriterInterpreter" should "find a json writer for date" in {
    simpleD.bind[Writes]
  }
  it should "write date as JsString" in {
    val dateWriter = simpleD.bind[Writes]

    val found = dateWriter.writes(dateTest)
    val expect = JsString(dateTest.toString)
    found shouldBe expect
  }
  it should "write the same than the default date writer" in {
    val derived: Writes[LocalDate] = simpleD.bind[Writes]
    val default: Writes[LocalDate] = Writes.DefaultLocalDateWrites

    val found = derived.writes(dateTest)
    val expect = default.writes(dateTest)

    found shouldBe expect
  }

  behavior of "JsonWriterInterpreter for empty list"

  it should "write an empty list to an empty JsArray" in {
    val emptyStringListWriter: Writes[List[String]] = Schema.many[String, List]().bind[Writes]

    val found = emptyStringListWriter.writes(emptyList)
    val expect = JsArray()
    found shouldBe expect
  }
  it should "write a string list to a JsArray of JsString" in {
    val stringListWriter: Writes[List[String]] = Schema.many[String, List]().bind[Writes]

    val found = stringListWriter.writes(stringList)
    val expect = JsArray(List(JsString("string"), JsString("string")))
    found shouldBe expect
  }
  it should "write an int list  to a JsArray of JsNumber" in {
    val intListWriter: Writes[List[Int]] = Schema.many[Int, List]().bind[Writes]

    val found = intListWriter.writes(intList)
    val expect = JsArray(List(JsNumber(0), JsNumber(1)))
    found shouldBe expect
  }

  behavior of "PlayJson Writes fields"

  it should "write an object to contains a required string" in {
    val requiredStringWriter = requiredString.bind[Writes]

    val found = requiredStringWriter.writes("a string")
    val expect = Json.obj(
      "string" -> "a string"
    )
    found shouldBe expect
  }
  it should "write a second level of object if it's describe in the path" in {
    val complexRequiredStringWriter = complexRequiredString.bind[Writes]

    val found = complexRequiredStringWriter.writes("a string")
    val expect = Json.obj(
      "level one" -> Json.obj(
        "level two" -> "a string"
      )
    )
    found shouldBe expect
  }

  it should "write optional string if present" in {
    val optionalStringWriter = optionalString.bind[Writes]

    val found = optionalStringWriter.writes(Some("string"))
    val expect = Json.obj(
      "string" -> "string"
    )
    found shouldBe expect
  }
  it should "write optional string if abscent" in {
    val optionalStringWriter = optionalString.bind[Writes]

    val found = optionalStringWriter.writes(None)
    val expect = Json.obj()
    found shouldBe expect
  }

  it should "write case class instances" in {
    val caseClassWriter = RequiredStringAndInt.schema.bind[Writes]

    val found = caseClassWriter.writes(RequiredStringAndInt("string", 0))
    val expect = Json.obj(
      "string" -> "string",
      "int" -> 0
    )
    found shouldBe expect
  }
}

final case class RequiredStringAndInt(string: String, int: Int)
object RequiredStringAndInt {
  import language.postfixOps
  val schema = (
    (Path \ "string").as[String]() and
    (Path \ "int").as[Int]()
  ).inmap(RequiredStringAndInt.apply _ tupled)(RequiredStringAndInt.unapply _ andThen (_.get))
}
