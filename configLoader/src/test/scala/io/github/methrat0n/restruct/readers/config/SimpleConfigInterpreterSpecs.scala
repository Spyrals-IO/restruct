package io.github.methrat0n.restruct.readers.config

import java.time.{ LocalDate, LocalTime, ZonedDateTime }

import com.typesafe.config.ConfigFactory
import org.scalatest.{ FlatSpec, Matchers }
import play.api.{ ConfigLoader, Configuration }

class SimpleConfigInterpreterSpecs extends FlatSpec with Matchers {
  private val configuration = Configuration(ConfigFactory.load("test.conf"))

  behavior of "ConfigLoader for strings"

  import io.github.methrat0n.restruct.schema.Syntax.string

  "ConfigLoader" should "find a loader for strings" in {
    string.bind(configLoader)
  }
  it should "load string without any change" in {
    val stringLoader = string.bind(configLoader)

    val found = configuration.get[String]("stringTest")(stringLoader)
    val expect = "test"
    found shouldBe expect
  }

  it should "load the same string than the default string ConfigLoader" in {
    val derived = string.bind(configLoader)
    val default = ConfigLoader.stringLoader

    val found = configuration.get[String]("stringTest")(derived)
    val expect = configuration.get[String]("stringTest")(default)

    found shouldBe expect
  }

  behavior of "ConfigLoader for decimal"

  import io.github.methrat0n.restruct.schema.Syntax.decimal

  "ConfigLoader" should "find a loader for decimals" in {
    decimal.bind(configLoader)
  }
  it should "load decimal" in {
    val decimalLoader = decimal.bind(configLoader)

    val found = configuration.get[Double]("decimalTest")(decimalLoader)
    val expect = 9999999.99999
    found shouldBe expect
  }

  it should "load the same decimal than the default decimal ConfigLoader" in {
    val derived = decimal.bind(configLoader)
    val default = ConfigLoader.doubleLoader

    val found = configuration.get[Double]("decimalTest")(derived)
    val expect = configuration.get[Double]("decimalTest")(default)

    found shouldBe expect
  }

  behavior of "ConfigLoader for integer"

  import io.github.methrat0n.restruct.schema.Syntax.integer

  "ConfigLoader" should "find a loader for integers" in {
    integer.bind(configLoader)
  }
  it should "load integer" in {
    val integerLoader = integer.bind(configLoader)

    val found = configuration.get[Int]("integerTest")(integerLoader)
    val expect = 9999
    found shouldBe expect
  }

  it should "load the same integer than the default integer loader" in {
    val derived = integer.bind(configLoader)
    val default = ConfigLoader.intLoader

    val found = configuration.get[Int]("integerTest")(derived)
    val expect = configuration.get[Int]("integerTest")(default)

    found shouldBe expect
  }

  behavior of "ConfigLoader for boolean"

  import io.github.methrat0n.restruct.schema.Syntax.boolean

  "ConfigLoader" should "find a loader for booleans" in {
    boolean.bind(configLoader)
  }
  it should "load boolean" in {
    val booleanLoader = boolean.bind(configLoader)

    val found = configuration.get[Boolean]("booleanTest")(booleanLoader)
    val expect = true
    found shouldBe expect
  }

  it should "load the same boolean than the default boolean loader" in {
    val derived = boolean.bind(configLoader)
    val default = ConfigLoader.booleanLoader

    val found = configuration.get[Boolean]("booleanTest")(derived)
    val expect = configuration.get[Boolean]("booleanTest")(default)

    found shouldBe expect
  }

  behavior of "ConfigLoader for char"

  import io.github.methrat0n.restruct.schema.Syntax.char

  "configLoader" should "find a loader for chars" in {
    char.bind(configLoader)
  }
  it should "load char" in {
    val charLoader = char.bind(configLoader)

    val found = configuration.get[Char]("charTest")(charLoader)
    val expect = 'a'
    found shouldBe expect
  }

  behavior of "ConfigLoader for byte"

  import io.github.methrat0n.restruct.schema.Syntax.byte

  "ConfigLoader" should "find a loader for bytes" in {
    byte.bind(configLoader)
  }
  it should "load byte" in {
    val byteLoader = byte.bind(configLoader)

    val found = configuration.get[Byte]("byteTest")(byteLoader)
    val expect = 'a'.toByte
    found shouldBe expect
  }

  behavior of "ConfigLoader for short"

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
  }
}
