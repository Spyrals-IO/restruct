package io.github.methrat0n.restruct.handlers.queryStringBindable

import org.scalatest.{ FlatSpec, Matchers }
import play.api.mvc.QueryStringBindable
import io.github.methrat0n.restruct.schema.Syntax._

class SimpleQueryStringBindableInterpreterSpecs extends FlatSpec with Matchers {

  private val query = Map(
    "string" -> Seq("methrat0n"),
    "double" -> Seq("12.2"),
    "int" -> Seq("1234"),
    "boolean" -> Seq("true"),
    "char" -> Seq("a"),
    "byte" -> Seq("a")
  )

  behavior of "QueryStringBindable errors"

  it should "not read abscent value" in {
    val decimalBindable = decimal.bind(queryStringBindable)

    val found = decimalBindable.bind("str", query)
    val expect = None
    found shouldBe expect
  }

  behavior of "QueryStringBindable for strings"

  "QueryStringBindable" should "find a parser for strings" in {
    string.bind(queryStringBindable)
  }
  it should "read string without any change" in {
    val stringBindable = string.bind(queryStringBindable)

    val found = stringBindable.bind("string", query)
    val expect = Some(Right("methrat0n"))
    found shouldBe expect
  }
  it should "write string without any change" in {
    val stringBindable = string.bind(queryStringBindable)

    val value = "methrat0n"
    val found = stringBindable.unbind("string", value)
    val expect = "string=methrat0n"
    found shouldBe expect
  }

  it should "read the same string than the default string QueryStringBindable" in {
    val derived = string.bind(queryStringBindable)
    val default = QueryStringBindable.bindableString

    val found = derived.bind("string", query)
    val expect = default.bind("string", query)

    found shouldBe expect
  }

  it should "write the same string than the default string QueryStringBindable" in {
    val derived = string.bind(queryStringBindable)
    val default = QueryStringBindable.bindableString

    val value = "methrat0n"
    val found = derived.unbind("string", value)
    val expect = default.unbind("string", value)

    found shouldBe expect
  }

  behavior of "QueryStringBindable for decimal"

  "QueryStringBindable" should "find a bindable for decimals" in {
    decimal.bind(queryStringBindable)
  }

  it should "read decimal" in {
    val decimalBindable = decimal.bind(queryStringBindable)

    val found = decimalBindable.bind("double", query)
    val expect = Some(Right(12.2))
    found shouldBe expect
  }

  it should "write decimal" in {
    val decimalBindable = decimal.bind(queryStringBindable)

    val found = decimalBindable.unbind("double", 12.2)
    val expect = "double=12.2"
    found shouldBe expect
  }

  it should "read the same decimal than the default decimal QueryStringBindable" in {
    val derived = decimal.bind(queryStringBindable)
    val default = QueryStringBindable.bindableDouble

    val found = derived.bind("double", query)
    val expect = default.bind("double", query)

    found shouldBe expect
  }

  it should "write the same decimal than the default decimal QueryStringBindable" in {
    val derived = decimal.bind(queryStringBindable)
    val default = QueryStringBindable.bindableDouble

    val found = derived.unbind("double", 12.2)
    val expect = default.unbind("double", 12.2)

    found shouldBe expect
  }

  it should "not read string with decimal bindable" in {
    val decimalBindable = decimal.bind(queryStringBindable)

    val found = decimalBindable.bind("string", query)
    val expect = Some(Left("Cannot parse parameter string as Double: For input string: \"methrat0n\""))
    found shouldBe expect
  }

  behavior of "QueryStringBindable for integer"

  "QueryStringBindable" should "find a bindable for integers" in {
    integer.bind(queryStringBindable)
  }
  it should "read integer" in {
    val integerBindable = integer.bind(queryStringBindable)

    val found = integerBindable.bind("int", query)
    val expect = Some(Right(1234))
    found shouldBe expect
  }
  it should "write integer" in {
    val integerBindable = integer.bind(queryStringBindable)

    val found = integerBindable.unbind("int", 1234)
    val expect = "int=1234"
    found shouldBe expect
  }
  it should "read the same integer than the default integer QueryStringBindable" in {
    val derived = integer.bind(queryStringBindable)
    val default = QueryStringBindable.bindableInt

    val found = derived.bind("int", query)
    val expect = default.bind("int", query)

    found shouldBe expect
  }
  it should "write the same integer than the default integer QueryStringBindable" in {
    val derived = integer.bind(queryStringBindable)
    val default = QueryStringBindable.bindableInt

    val found = derived.unbind("int", 1234)
    val expect = default.unbind("int", 1234)

    found shouldBe expect
  }
  it should "not read string with integer bindable" in {
    val integerBindable = integer.bind(queryStringBindable)

    val found = integerBindable.bind("string", query)
    val expect = Some(Left("Cannot parse parameter string as Int: For input string: \"methrat0n\""))
    found shouldBe expect
  }
  it should "not read double with integer bindable" in {
    val integerBindable = integer.bind(queryStringBindable)

    val found = integerBindable.bind("double", query)
    val expect = Some(Left("Cannot parse parameter double as Int: For input string: \"12.2\""))
    found shouldBe expect
  }

  behavior of "QueryStringBindable for boolean"

  "QueryStringBindable" should "find a bindable for booleans" in {
    boolean.bind(queryStringBindable)
  }
  it should "read boolean" in {
    val booleanBindable = boolean.bind(queryStringBindable)

    val found = booleanBindable.bind("boolean", query)
    val expect = Some(Right(true))
    found shouldBe expect
  }
  it should "write boolean" in {
    val booleanBindable = boolean.bind(queryStringBindable)

    val found = booleanBindable.unbind("boolean", true)
    val expect = "boolean=true"
    found shouldBe expect
  }

  it should "read the same boolean than the default boolean bindable" in {
    val derived = boolean.bind(queryStringBindable)
    val default = QueryStringBindable.bindableBoolean

    val found = derived.bind("boolean", query)
    val expect = default.bind("boolean", query)

    found shouldBe expect
  }

  it should "write the same boolean than the default boolean bindable" in {
    val derived = boolean.bind(queryStringBindable)
    val default = QueryStringBindable.bindableBoolean

    val found = derived.unbind("boolean", true)
    val expect = default.unbind("boolean", true)

    found shouldBe expect
  }

  it should "not read string with boolean bindable" in {
    val booleanBindable = boolean.bind(queryStringBindable)

    val found = booleanBindable.bind("string", query)
    val expect = Some(Left("Cannot parse parameter string as Boolean: should be true, false, 0 or 1"))
    found shouldBe expect
  }

  it should "not read decimal with boolean bindable" in {
    val booleanBindable = boolean.bind(queryStringBindable)

    val found = booleanBindable.bind("double", query)
    val expect = Some(Left("Cannot parse parameter double as Boolean: should be true, false, 0 or 1"))
    found shouldBe expect
  }

  it should "not read integer with boolean bindable" in {
    val booleanBindable = boolean.bind(queryStringBindable)

    val found = booleanBindable.bind("int", query)
    val expect = Some(Left("Cannot parse parameter int as Boolean: should be true, false, 0 or 1"))
    found shouldBe expect
  }

  behavior of "QueryStringBindable for char"

  "QueryStringBindable" should "find a bindable for chars" in {
    char.bind(queryStringBindable)
  }
  it should "read char" in {
    val charBindable = char.bind(queryStringBindable)

    val found = charBindable.bind("char", query)
    val expect = Some(Right('a'))
    found shouldBe expect
  }
  it should "write char" in {
    val charBindable = char.bind(queryStringBindable)

    val found = charBindable.unbind("char", 'a')
    val expect = "char=a"
    found shouldBe expect
  }
  it should "read the same char than the default char bindable" in {
    val derived = char.bind(queryStringBindable)
    val default = QueryStringBindable.bindableChar

    val found = derived.bind("char", query)
    val expect = default.bind("char", query)

    found shouldBe expect
  }

  it should "write the same char than the default char bindable" in {
    val derived = char.bind(queryStringBindable)
    val default = QueryStringBindable.bindableChar

    val found = derived.unbind("char", 'a')
    val expect = default.unbind("char", 'a')

    found shouldBe expect
  }
  it should "not read string with char bindable" in {
    val charBindable = char.bind(queryStringBindable)

    val found = charBindable.bind("string", query)
    val expect = Some(Left("Cannot parse parameter string with value 'methrat0n' as Char: string must be exactly one digit in length."))
    found shouldBe expect
  }
  it should "not read decimal with char bindable" in {
    val charBindable = char.bind(queryStringBindable)

    val found = charBindable.bind("double", query)
    val expect = Some(Left("Cannot parse parameter double with value '12.2' as Char: double must be exactly one digit in length."))
    found shouldBe expect
  }
  it should "not read integer with char bindable" in {
    val charBindable = char.bind(queryStringBindable)

    val found = charBindable.bind("int", query)
    val expect = Some(Left("Cannot parse parameter int with value '1234' as Char: int must be exactly one digit in length."))
    found shouldBe expect
  }
  it should "not read boolean with char bindable" in {
    val charBindable = char.bind(queryStringBindable)

    val found = charBindable.bind("boolean", query)
    val expect = Some(Left("Cannot parse parameter boolean with value 'true' as Char: boolean must be exactly one digit in length."))
    found shouldBe expect
  }

  behavior of "QueryStringBindable for byte"

  "QueryStringBindable" should "find a bindable for bytes" in {
    byte.bind(queryStringBindable)
  }
  it should "read byte" in {
    val byteBindable = byte.bind(queryStringBindable)

    val found = byteBindable.bind("byte", query)
    val expect = Some(Right('a'.toByte))
    found shouldBe expect
  }
  it should "write byte" in {
    val byteBindable = byte.bind(queryStringBindable)

    val found = byteBindable.unbind("byte", 'a'.toByte)
    val expect = "byte=a"
    found shouldBe expect
  }
  it should "not read string with byte bindable" in {
    val byteBindable = byte.bind(queryStringBindable)

    val found = byteBindable.bind("string", query)
    val expect = Some(Left("Cannot parse parameter string with value 'methrat0n' as Char: string must be exactly one digit in length."))
    found shouldBe expect
  }
  it should "not read decimal with byte bindable" in {
    val byteBindable = byte.bind(queryStringBindable)

    val found = byteBindable.bind("double", query)
    val expect = Some(Left("Cannot parse parameter double with value '12.2' as Char: double must be exactly one digit in length."))
    found shouldBe expect
  }
  it should "not read integer with byte bindable" in {
    val byteBindable = byte.bind(queryStringBindable)

    val found = byteBindable.bind("int", query)
    val expect = Some(Left("Cannot parse parameter int with value '1234' as Char: int must be exactly one digit in length."))
    found shouldBe expect
  }
  it should "not read boolean with byte bindable" in {
    val byteBindable = byte.bind(queryStringBindable)

    val found = byteBindable.bind("boolean", query)
    val expect = Some(Left("Cannot parse parameter boolean with value 'true' as Char: boolean must be exactly one digit in length."))
    found shouldBe expect
  }
  it should "read char with byte bindable" in {
    val byteBindable = byte.bind(queryStringBindable)

    val found = byteBindable.bind("char", query)
    val expect = Some(Right('a'.toByte))
    found shouldBe expect
  }
  /*behavior of "ConfigLoader for short"

  import io.github.methrat0n.restruct.schema.Syntax.short

  "ConfigLoader" should "find a loader for shorts" in {
    short.bind(configLoader)
  }
  it should "load short" in {
    val shortWriter = short.bind(configLoader)

    val found = configuration.get[Short]("shortTest")(shortWriter)
    val expect = 'a'.toShort
    found shouldBe expect
  }

  behavior of "ConfigLoader for float"

  import io.github.methrat0n.restruct.schema.Syntax.float

  "ConfigLoader" should "find a loader for floats" in {
    float.bind(configLoader)
  }
  it should "load float" in {
    val floatWriter = float.bind(configLoader)

    val found = configuration.get[Float]("floatTest")(floatWriter)
    val expect = 12.2f
    found shouldBe expect
  }

  behavior of "ConfigLoader for BigDecimal"

  import io.github.methrat0n.restruct.schema.Syntax.bigDecimal

  "ConfigLoader" should "find a loader for bigDecimal" in {
    bigDecimal.bind(configLoader)
  }
  it should "load bigDecimal" in {
    val bigDecimalLoader = bigDecimal.bind(configLoader)

    val found = configuration.get[BigDecimal]("bigDecimalTest")(bigDecimalLoader)
    val expect = BigDecimal("199999999999999.000000000001")
    found shouldBe expect
  }

  behavior of "ConfigLoader for Long"

  import io.github.methrat0n.restruct.schema.Syntax.long

  "ConfigLoader" should "find a loader for long" in {
    long.bind(configLoader)
  }
  it should "load long" in {
    val longLoader = long.bind(configLoader)

    val found = configuration.get[Long]("longTest")(longLoader)
    val expect = 88888888888888l
    found shouldBe expect
  }

  it should "load the same long than the default long loader" in {
    val derived = long.bind(configLoader)
    val default = ConfigLoader.longLoader

    val found = configuration.get[Long]("longTest")(derived)
    val expect = configuration.get[Long]("longTest")(default)

    found shouldBe expect
  }

  behavior of "ConfigLoader for bigInt"

  import io.github.methrat0n.restruct.schema.Syntax.bigInt

  "ConfigLoader" should "find a loader for bigInt" in {
    bigInt.bind(configLoader)
  }
  it should "load bigInt" in {
    val bigIntLoader = bigInt.bind(configLoader)

    val found = configuration.get[BigInt]("bigIntTest")(bigIntLoader)
    val expect = BigInt("888888888888889999999999999999999")
    found shouldBe expect
  }

  behavior of "ConfigLoader for dateTime"

  import io.github.methrat0n.restruct.schema.Syntax.dateTime

  "ConfigLoader" should "find a loader for dateTime" in {
    dateTime.bind(configLoader)
  }
  it should "load dateTime" in {
    val dateTimeLoader = dateTime.bind(configLoader)

    val found = configuration.get[ZonedDateTime]("DateTimeTest")(dateTimeLoader)
    val expect = ZonedDateTime.parse("2018-12-19T21:03:41.050953+01:00[Europe/Paris]")
    found shouldBe expect
  }

  behavior of "ConfigLoader for time"

  import io.github.methrat0n.restruct.schema.Syntax.time

  "ConfigLoader" should "find a loader for time" in {
    time.bind(configLoader)
  }
  it should "load time" in {
    val timeLoader = time.bind(configLoader)

    val found = configuration.get[LocalTime]("TimeTest")(timeLoader)
    val expect = LocalTime.parse("21:03:41.050953")
    found shouldBe expect
  }

  behavior of "ConfigLoader for date"

  import io.github.methrat0n.restruct.schema.Syntax.date

  "ConfigLoader" should "find a loader for date" in {
    date.bind(configLoader)
  }
  it should "load date" in {
    val dateLoader = date.bind(configLoader)

    val found = configuration.get[LocalDate]("DateTest")(dateLoader)
    val expect = LocalDate.parse("2018-12-19")
    found shouldBe expect
  }*/
}
