package io.github.methrat0n.restruct.writers.json

import java.time.{ LocalDate, LocalTime, ZonedDateTime }

import org.scalatest.{ FlatSpec, Matchers }
import play.api.libs.json
import play.api.libs.json.{ JsBoolean, JsNumber, JsString, Writes }

class JsonStringWriterInterpreterSpecs extends FlatSpec with Matchers {

  private val jsonWriter = JsonWriterInterpreter

  behavior of "JsonWriterInterpreter for strings"

  import io.github.methrat0n.restruct.schema.Syntax.string

  "JsonWriterInterpreter" should "find a json writer for strings" in {
    string.bind(jsonWriter)
  }
  it should "write string without any change" in {
    val stringWriter = string.bind(jsonWriter)

    val found = stringWriter.writes("test case")
    val expect = JsString("test case")
    found shouldBe expect
  }

  it should "write the same string than the default string writer" in {
    val derived: Writes[String] = string.bind(jsonWriter)
    val default: Writes[String] = Writes.StringWrites

    val test = "test"
    val found = derived.writes(test)
    val expect = default.writes(test)

    found shouldBe expect
  }

  behavior of "JsonWriterInterpreter for decimal"

  import io.github.methrat0n.restruct.schema.Syntax.decimal

  "JsonWriterInterpreter" should "find a json writer for decimals" in {
    decimal.bind(jsonWriter)
  }
  it should "write decimal as string" in {
    val decimalWriter = decimal.bind(jsonWriter)

    val found = decimalWriter.writes(22.2)
    val expect = JsNumber(22.2)
    found shouldBe expect
  }

  it should "write the same decimal than the default decimal writer" in {
    val derived: Writes[Double] = decimal.bind(jsonWriter)
    val default: Writes[Double] = Writes.DoubleWrites

      def simpleDecimal = {
        val test = 22.2
        val found = derived.writes(test)
        val expect = default.writes(test)

        found shouldBe expect
      }

      def simpleInteger = {
        val test = 22
        val found = derived.writes(test)
        val expect = default.writes(test)

        found shouldBe expect
      }

    simpleDecimal
    simpleInteger
  }

  behavior of "JsonWriterInterpreter for integer"

  import io.github.methrat0n.restruct.schema.Syntax.integer

  "JsonWriterInterpreter" should "find a json writer for integers" in {
    integer.bind(jsonWriter)
  }
  it should "write integer as string" in {
    val integerWriter = integer.bind(jsonWriter)

    val found = integerWriter.writes(22)
    val expect = JsNumber(22)
    found shouldBe expect
  }

  it should "write the same integer than the default integer writer" in {
    val derived: Writes[Int] = integer.bind(jsonWriter)
    val default: Writes[Int] = Writes.IntWrites

    val test = 22
    val found = derived.writes(test)
    val expect = default.writes(test)

    found shouldBe expect
  }

  behavior of "JsonWriterInterpreter for boolean"

  import io.github.methrat0n.restruct.schema.Syntax.boolean

  "JsonWriterInterpreter" should "find a json writer for booleans" in {
    boolean.bind(jsonWriter)
  }
  it should "write boolean as string" in {
    val booleanWriter = boolean.bind(jsonWriter)

    val found = booleanWriter.writes(true)
    val expect = JsBoolean(true)
    found shouldBe expect
  }

  it should "write the same boolean than the default boolean writer" in {
    val derived: Writes[Boolean] = boolean.bind(jsonWriter)
    val default: Writes[Boolean] = Writes.BooleanWrites

    val test = true
    val found = derived.writes(test)
    val expect = default.writes(test)

    found shouldBe expect
  }

  behavior of "JsonWriterInterpreter for char"

  import io.github.methrat0n.restruct.schema.Syntax.char

  "JsonWriterInterpreter" should "find a json writer for chars" in {
    char.bind(jsonWriter)
  }
  it should "write char as string" in {
    val booleanWriter = char.bind(jsonWriter)

    val found = booleanWriter.writes('s')
    val expect = JsString('s'.toString)
    found shouldBe expect
  }

  behavior of "JsonWriterInterpreter for byte"

  import io.github.methrat0n.restruct.schema.Syntax.byte

  "JsonWriterInterpreter" should "find a json writer for bytes" in {
    byte.bind(jsonWriter)
  }
  it should "write byte as string" in {
    val byteWriter = byte.bind(jsonWriter)

    val found = byteWriter.writes('s')
    val expect = json.JsNumber('s'.toInt)
    found shouldBe expect
  }

  behavior of "JsonWriterInterpreter for short"

  import io.github.methrat0n.restruct.schema.Syntax.short

  "JsonWriterInterpreter" should "find a json writer for shorts" in {
    short.bind(jsonWriter)
  }
  it should "write short as string" in {
    val shortWriter = short.bind(jsonWriter)

    val found = shortWriter.writes('s')
    val expect = json.JsNumber('s'.toInt)
    found shouldBe expect
  }

  behavior of "JsonWriterInterpreter for float"

  import io.github.methrat0n.restruct.schema.Syntax.float

  "JsonWriterInterpreter" should "find a json writer for floats" in {
    float.bind(jsonWriter)
  }
  it should "write float as string" in {
    val floatWriter = float.bind(jsonWriter)

    val found = floatWriter.writes(12.2f)
    val expect = json.JsNumber(12.2f.toDouble)
    found shouldBe expect
  }

  it should "write the same float than the default float writer" in {
    val derived: Writes[Float] = float.bind(jsonWriter)
    val default: Writes[Float] = Writes.FloatWrites

      def simpleFloat = {
        val test = 12.2f
        val found = derived.writes(test)
        val expect = default.writes(test)

        found shouldBe expect
      }

      def simpleInteger = {
        val test = 12
        val found = derived.writes(test)
        val expect = default.writes(test)

        found shouldBe expect
      }

    simpleFloat
    simpleInteger
  }

  behavior of "JsonWriterInterpreter for BigDecimal"

  import io.github.methrat0n.restruct.schema.Syntax.bigDecimal

  "JsonWriterInterpreter" should "find a json writer for bigDecimal" in {
    bigDecimal.bind(jsonWriter)
  }
  it should "write bigDecimal as string" in {
    val bigDecimalWriter = bigDecimal.bind(jsonWriter)

    val found = bigDecimalWriter.writes(BigDecimal.decimal(12.2f))
    val expect = json.JsNumber(BigDecimal.decimal(12.2f))
    found shouldBe expect
  }

  it should "write the same bigDecimal than the default bigDecimal writer" in {
    val derived: Writes[BigDecimal] = bigDecimal.bind(jsonWriter)
    val default: Writes[BigDecimal] = Writes.BigDecimalWrites

    val test = BigDecimal.decimal(12.2f)
    val found = derived.writes(test)
    val expect = default.writes(test)

    found shouldBe expect
  }

  behavior of "JsonWriterInterpreter for Long"

  import io.github.methrat0n.restruct.schema.Syntax.long

  "JsonWriterInterpreter" should "find a json writer for long" in {
    long.bind(jsonWriter)
  }
  it should "write long as string" in {
    val longWriter = long.bind(jsonWriter)

    val found = longWriter.writes(12l)
    val expect = json.JsNumber(12l)
    found shouldBe expect
  }

  it should "write the same long than the default long writer" in {
    val derived: Writes[Long] = long.bind(jsonWriter)
    val default: Writes[Long] = Writes.LongWrites

      def simpleLong = {
        val test = 12l
        val found = derived.writes(test)
        val expect = default.writes(test)

        found shouldBe expect
      }

      def simpleInteger = {
        val test = 12
        val found = derived.writes(test)
        val expect = default.writes(test)

        found shouldBe expect
      }

    simpleLong
    simpleInteger
  }

  behavior of "JsonWriterInterpreter for bigInt"

  import io.github.methrat0n.restruct.schema.Syntax.bigInt

  "JsonWriterInterpreter" should "find a json writer for bigInt" in {
    bigInt.bind(jsonWriter)
  }
  it should "write bigInt as string" in {
    val bigIntWriter = bigInt.bind(jsonWriter)

    val found = bigIntWriter.writes(12)
    val expect = json.JsNumber(12)
    found shouldBe expect
  }

  behavior of "JsonWriterInterpreter for dateTime"

  import io.github.methrat0n.restruct.schema.Syntax.dateTime

  "JsonWriterInterpreter" should "find a json writer for dateTime" in {
    dateTime.bind(jsonWriter)
  }
  it should "write dateTime as string" in {
    val dateTimeWriter = dateTime.bind(jsonWriter)

    val test = ZonedDateTime.now()
    val found = dateTimeWriter.writes(test)
    val expect = json.JsString(test.toString)
    found shouldBe expect
  }

  it should "write the same dateTime than the default dateTime writer" in {
    val derived: Writes[ZonedDateTime] = dateTime.bind(jsonWriter)
    val default: Writes[ZonedDateTime] = Writes.DefaultZonedDateTimeWrites

    val test = ZonedDateTime.now()
    val found = derived.writes(test)
    val expect = default.writes(test)

    found shouldBe expect
  }

  behavior of "JsonWriterInterpreter for time"

  import io.github.methrat0n.restruct.schema.Syntax.time

  "JsonWriterInterpreter" should "find a json writer for time" in {
    time.bind(jsonWriter)
  }
  it should "write time as string" in {
    val timeWriter = time.bind(jsonWriter)

    val test = LocalTime.now()
    val found = timeWriter.writes(test)
    val expect = json.JsString(test.toString)
    found shouldBe expect
  }

  it should "write the same time than the default time writer" in {
    val derived: Writes[LocalTime] = time.bind(jsonWriter)
    val default: Writes[LocalTime] = Writes.DefaultLocalTimeWrites

    val test = LocalTime.now()
    val found = derived.writes(test)
    val expect = default.writes(test)

    found shouldBe expect
  }

  behavior of "JsonWriterInterpreter for date"

  import io.github.methrat0n.restruct.schema.Syntax.date

  "JsonWriterInterpreter" should "find a json writer for date" in {
    date.bind(jsonWriter)
  }
  it should "write date as string" in {
    val dateWriter = date.bind(jsonWriter)

    val test = LocalDate.now()
    val found = dateWriter.writes(test)
    val expect = json.JsString(test.toString)
    found shouldBe expect
  }

  it should "write the same date than the default date writer" in {
    val derived: Writes[LocalDate] = date.bind(jsonWriter)
    val default: Writes[LocalDate] = Writes.DefaultLocalDateWrites

    val test = LocalDate.now()
    val found = derived.writes(test)
    val expect = default.writes(test)

    found shouldBe expect
  }

}
