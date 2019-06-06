package io.github.methrat0n.restruct.writers.json

import java.time.{ LocalDate, LocalTime, ZonedDateTime }

import org.scalatest.{ FlatSpec, Matchers }
import play.api.libs.json.{ JsBoolean, JsNumber, JsString, Writes }

class SimpleJsonWriterInterpreterSpecs extends FlatSpec with Matchers {

  private val stringTest: String = "methrat0n"
  private val decimalTest: Double = 12678888899999999999999999999988888676997676.2d
  private val integerTest: Int = 33333
  private val booleanTest: Boolean = true
  private val charTest: Char = 'a'
  private val byteTest: Byte = 123
  private val shortTest: Short = 23456
  private val floatTest: Float = 12.2f
  private val bigDecimalTest: BigDecimal = BigDecimal("1267888889999999999999999999998888867699767611111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111.1")
  private val longTest: Long = 1267888889999999999l
  private val bigIntTest: BigInt = BigInt("1267888889999999999999999999998888867699767611111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111")
  private val dateTimeTest: ZonedDateTime = ZonedDateTime.parse("2018-12-26T00:08:16.025415+01:00[Europe/Paris]")
  private val timeTest: LocalTime = LocalTime.parse("00:08:16.025415")
  private val dateTest: LocalDate = LocalDate.parse("2018-12-26")

  //This exist to be able to call BigDecimal.apply without a warning being raised. When a SuppressWarning or equivalent is added to scala
  //this code should be removed
  object WorkAround { @deprecated("", "") class Intern { def decimal(f: Float): BigDecimal = BigDecimal(f) }; object Intern extends Intern }

  behavior of "JsonWriterInterpreter for strings"

  "JsonWriterInterpreter" should "find a json writer for strings" in {
    string.bind(jsonWrites)
  }
  it should "write string as JsString" in {
    val stringWriter = string.bind(jsonWrites)

    val found = stringWriter.writes(stringTest)
    val expect = JsString(stringTest)
    found shouldBe expect
  }
  it should "write the same than the default string writer" in {
    val derived: Writes[String] = string.bind(jsonWrites)
    val default: Writes[String] = Writes.StringWrites

    val found = derived.writes(stringTest)
    val expect = default.writes(stringTest)

    found shouldBe expect
  }

  behavior of "JsonWriterInterpreter for decimal"

  "JsonWriterInterpreter" should "find a json writer for decimals" in {
    decimal.bind(jsonWrites)
  }
  it should "write decimal as JsNumber" in {
    val decimalWriter = decimal.bind(jsonWrites)

    val found = decimalWriter.writes(decimalTest)
    val expect = JsNumber(decimalTest)
    found shouldBe expect
  }
  it should "write the same than the default decimal writer" in {
    val derived: Writes[Double] = decimal.bind(jsonWrites)
    val default: Writes[Double] = Writes.DoubleWrites

    val found = derived.writes(decimalTest)
    val expect = default.writes(decimalTest)

    found shouldBe expect
  }
  it should "write integer as JsNumber using decimal writer" in {
    val decimalWriter = decimal.bind(jsonWrites)

    val found = decimalWriter.writes(integerTest)
    val expect = JsNumber(integerTest)
    found shouldBe expect
  }
  it should "write float as JsNumber using decimal writer" in {
    val decimalWriter = decimal.bind(jsonWrites)

    val found = decimalWriter.writes(floatTest)
    val expect = JsNumber(WorkAround.Intern.decimal(floatTest))
    found shouldBe expect
  }
  it should "write byte as JsNumber using decimal writer" in {
    val decimalWriter = decimal.bind(jsonWrites)

    val found = decimalWriter.writes(byteTest)
    val expect = JsNumber(BigDecimal(byteTest))
    found shouldBe expect
  }
  it should "write short as JsNumber using decimal writer" in {
    val decimalWriter = decimal.bind(jsonWrites)

    val found = decimalWriter.writes(shortTest)
    val expect = JsNumber(BigDecimal(shortTest))
    found shouldBe expect
  }

  behavior of "JsonWriterInterpreter for integer"

  "JsonWriterInterpreter" should "find a json writer for integers" in {
    integer.bind(jsonWrites)
  }
  it should "write integer as JsNumber" in {
    val integerWriter = integer.bind(jsonWrites)

    val found = integerWriter.writes(integerTest)
    val expect = JsNumber(integerTest)
    found shouldBe expect
  }
  it should "write the same than the default integer writer" in {
    val derived: Writes[Int] = integer.bind(jsonWrites)
    val default: Writes[Int] = Writes.IntWrites

    val found = derived.writes(integerTest)
    val expect = default.writes(integerTest)

    found shouldBe expect
  }
  it should "write byte as JsNumber using integer writer" in {
    val integerWriter = integer.bind(jsonWrites)

    val found = integerWriter.writes(byteTest)
    val expect = JsNumber(BigDecimal(byteTest))
    found shouldBe expect
  }
  it should "write short as JsNumber using integer writer" in {
    val integerWriter = integer.bind(jsonWrites)

    val found = integerWriter.writes(shortTest)
    val expect = JsNumber(BigDecimal(shortTest))
    found shouldBe expect
  }

  behavior of "JsonWriterInterpreter for boolean"

  "JsonWriterInterpreter" should "find a json writer for booleans" in {
    boolean.bind(jsonWrites)
  }
  it should "write boolean as JsBoolean" in {
    val booleanWriter = boolean.bind(jsonWrites)

    val found = booleanWriter.writes(booleanTest)
    val expect = JsBoolean(booleanTest)
    found shouldBe expect
  }
  it should "write the same than the default boolean writer" in {
    val derived: Writes[Boolean] = boolean.bind(jsonWrites)
    val default: Writes[Boolean] = Writes.BooleanWrites

    val found = derived.writes(booleanTest)
    val expect = default.writes(booleanTest)

    found shouldBe expect
  }

  behavior of "JsonWriterInterpreter for char"

  "JsonWriterInterpreter" should "find a json writer for chars" in {
    char.bind(jsonWrites)
  }
  it should "write char as JsString" in {
    val charWriter = char.bind(jsonWrites)

    val found = charWriter.writes(charTest)
    val expect = JsString(charTest.toString)
    found shouldBe expect
  }

  behavior of "JsonWriterInterpreter for byte"

  "JsonWriterInterpreter" should "find a json writer for bytes" in {
    byte.bind(jsonWrites)
  }
  it should "write byte as JsNumber" in {
    val byteWriter = byte.bind(jsonWrites)

    val found = byteWriter.writes(byteTest)
    val expect = JsNumber(BigDecimal(byteTest))
    found shouldBe expect
  }

  behavior of "JsonWriterInterpreter for short"

  "JsonWriterInterpreter" should "find a json writer for shorts" in {
    short.bind(jsonWrites)
  }
  it should "write short as JsNumber" in {
    val shortWriter = short.bind(jsonWrites)

    val found = shortWriter.writes(shortTest)
    val expect = JsNumber(BigDecimal(shortTest))
    found shouldBe expect
  }

  behavior of "JsonWriterInterpreter for float"

  "JsonWriterInterpreter" should "find a json writer for floats" in {
    float.bind(jsonWrites)
  }
  it should "write float as JsNumber" in {
    val floatWriter = float.bind(jsonWrites)

    val found = floatWriter.writes(floatTest)
    val expect = JsNumber(WorkAround.Intern.decimal(floatTest))
    found shouldBe expect
  }
  it should "write the same than the default float writer" in {
    val derived: Writes[Float] = float.bind(jsonWrites)
    val default: Writes[Float] = Writes.FloatWrites

    val found = derived.writes(floatTest)
    val expect = default.writes(floatTest)

    found shouldBe expect
  }
  it should "write integer as JsNumber using float writer" in {
    val floatWriter = float.bind(jsonWrites)

    val found = floatWriter.writes(integerTest)
    val expect = JsNumber(integerTest)
    found shouldBe expect
  }
  it should "write byte as JsNumber using float writer" in {
    val floatWriter = float.bind(jsonWrites)

    val found = floatWriter.writes(byteTest)
    val expect = JsNumber(BigDecimal(byteTest))
    found shouldBe expect
  }
  it should "write short as JsNumber using float writer" in {
    val floatWriter = float.bind(jsonWrites)

    val found = floatWriter.writes(shortTest)
    val expect = JsNumber(BigDecimal(shortTest))
    found shouldBe expect
  }

  behavior of "JsonWriterInterpreter for BigDecimal"

  "JsonWriterInterpreter" should "find a json writer for bigDecimal" in {
    bigDecimal.bind(jsonWrites)
  }
  it should "write bigDecimal as JsNumber" in {
    val bigDecimalWriter = bigDecimal.bind(jsonWrites)

    val found = bigDecimalWriter.writes(bigDecimalTest)
    val expect = JsNumber(bigDecimalTest)
    found shouldBe expect
  }
  it should "write the same than the default bigDecimal writer" in {
    val derived: Writes[BigDecimal] = bigDecimal.bind(jsonWrites)
    val default: Writes[BigDecimal] = Writes.BigDecimalWrites

    val found = derived.writes(bigDecimalTest)
    val expect = default.writes(bigDecimalTest)

    found shouldBe expect
  }
  it should "write integer as JsNumber using bigDecimal writer" in {
    val bigDecimalWriter = bigDecimal.bind(jsonWrites)

    val found = bigDecimalWriter.writes(integerTest)
    val expect = JsNumber(integerTest)
    found shouldBe expect
  }
  it should "write decimal as JsNumber using bigDecimal writer" in {
    val bigDecimalWriter = bigDecimal.bind(jsonWrites)

    val found = bigDecimalWriter.writes(decimalTest)
    val expect = JsNumber(decimalTest)
    found shouldBe expect
  }

  behavior of "JsonWriterInterpreter for Long"

  "JsonWriterInterpreter" should "find a json writer for long" in {
    long.bind(jsonWrites)
  }
  it should "write long as JsNumber" in {
    val longWriter = long.bind(jsonWrites)

    val found = longWriter.writes(longTest)
    val expect = JsNumber(longTest)
    found shouldBe expect
  }
  it should "write the same than the default long writer" in {
    val derived: Writes[Long] = long.bind(jsonWrites)
    val default: Writes[Long] = Writes.LongWrites

    val found = derived.writes(longTest)
    val expect = default.writes(longTest)

    found shouldBe expect
  }
  it should "write byte as JsNumber using long writer" in {
    val longWriter = long.bind(jsonWrites)

    val found = longWriter.writes(byteTest)
    val expect = JsNumber(BigDecimal(byteTest))
    found shouldBe expect
  }
  it should "write short as JsNumber using long writer" in {
    val longWriter = long.bind(jsonWrites)

    val found = longWriter.writes(shortTest)
    val expect = JsNumber(BigDecimal(shortTest))
    found shouldBe expect
  }
  it should "write integer as JsNumber using long writer" in {
    val longWriter = long.bind(jsonWrites)

    val found = longWriter.writes(integerTest)
    val expect = JsNumber(integerTest)
    found shouldBe expect
  }

  behavior of "JsonWriterInterpreter for bigInt"

  "JsonWriterInterpreter" should "find a json writer for bigInt" in {
    bigInt.bind(jsonWrites)
  }
  it should "write bigInt as JsNumber" in {
    val bigIntWriter = bigInt.bind(jsonWrites)

    val found = bigIntWriter.writes(bigIntTest)
    val expect = JsNumber(BigDecimal(bigIntTest))
    found shouldBe expect
  }
  it should "write integer as JsNumber using bigInt writer" in {
    val bigIntWriter = bigInt.bind(jsonWrites)

    val found = bigIntWriter.writes(integerTest)
    val expect = JsNumber(BigDecimal(integerTest))
    found shouldBe expect
  }

  behavior of "JsonWriterInterpreter for dateTime"

  "JsonWriterInterpreter" should "find a json writer for dateTime" in {
    dateTime.bind(jsonWrites)
  }
  it should "write dateTime as JsString" in {
    val dateTimeWriter = dateTime.bind(jsonWrites)

    val found = dateTimeWriter.writes(dateTimeTest)
    val expect = JsString(dateTimeTest.toString)
    found shouldBe expect
  }
  it should "write the same than the default dateTime writer" in {
    val derived: Writes[ZonedDateTime] = dateTime.bind(jsonWrites)
    val default: Writes[ZonedDateTime] = Writes.DefaultZonedDateTimeWrites

    val found = derived.writes(dateTimeTest)
    val expect = default.writes(dateTimeTest)

    found shouldBe expect
  }

  behavior of "JsonWriterInterpreter for time"

  "JsonWriterInterpreter" should "find a json writer for time" in {
    time.bind(jsonWrites)
  }
  it should "write time as JsString" in {
    val timeWriter = time.bind(jsonWrites)

    val found = timeWriter.writes(timeTest)
    val expect = JsString(timeTest.toString)

    found shouldBe expect
  }
  it should "write the same than the default time writer" in {
    val derived: Writes[LocalTime] = time.bind(jsonWrites)
    val default: Writes[LocalTime] = Writes.DefaultLocalTimeWrites

    val found = derived.writes(timeTest)
    val expect = default.writes(timeTest)

    found shouldBe expect
  }

  behavior of "JsonWriterInterpreter for date"

  "JsonWriterInterpreter" should "find a json writer for date" in {
    date.bind(jsonWrites)
  }
  it should "write date as JsString" in {
    val dateWriter = date.bind(jsonWrites)

    val found = dateWriter.writes(dateTest)
    val expect = JsString(dateTest.toString)
    found shouldBe expect
  }
  it should "write the same than the default date writer" in {
    val derived: Writes[LocalDate] = date.bind(jsonWrites)
    val default: Writes[LocalDate] = Writes.DefaultLocalDateWrites

    val found = derived.writes(dateTest)
    val expect = default.writes(dateTest)

    found shouldBe expect
  }

}
