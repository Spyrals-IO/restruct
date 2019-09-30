package io.github.methrat0n.restruct.readers

import java.time.{ LocalDate, LocalTime, ZonedDateTime }

import com.typesafe.config.ConfigFactory
import io.github.methrat0n.restruct.readers.ConfigLoaderSpecs.{ OptionalString, RequiredString }
import io.github.methrat0n.restruct.readers.configLoader._
import io.github.methrat0n.restruct.schema.Path
import io.github.methrat0n.restruct.schema.Schema._
import org.scalatest.{ FlatSpec, Matchers }
import play.api.{ ConfigLoader, Configuration }

class ConfigLoaderSpecs extends FlatSpec with Matchers {
  private val configuration = Configuration(ConfigFactory.load("test.conf"))

  behavior of "ConfigLoader for strings"

  "ConfigLoader" should "find a loader for strings" in {
    simpleString.bind[ConfigLoader]
  }
  it should "load string without any change" in {
    val stringLoader = simpleString.bind[ConfigLoader]

    val found = configuration.get[String]("stringTest")(stringLoader)
    val expect = "test"
    found shouldBe expect
  }

  it should "load the same string than the default string ConfigLoader" in {
    val derived = simpleString.bind[ConfigLoader]
    val default = ConfigLoader.stringLoader

    val found = configuration.get[String]("stringTest")(derived)
    val expect = configuration.get[String]("stringTest")(default)

    found shouldBe expect
  }

  behavior of "ConfigLoader for decimal"

  "ConfigLoader" should "find a loader for decimals" in {
    simple[Double].bind[ConfigLoader]
  }
  it should "load decimal" in {
    val decimalLoader = simple[Double].bind[ConfigLoader]

    val found = configuration.get[Double]("decimalTest")(decimalLoader)
    val expect = 9999999.99999
    found shouldBe expect
  }

  it should "load the same decimal than the default decimal ConfigLoader" in {
    val derived = simple[Double].bind[ConfigLoader]
    val default = ConfigLoader.doubleLoader

    val found = configuration.get[Double]("decimalTest")(derived)
    val expect = configuration.get[Double]("decimalTest")(default)

    found shouldBe expect
  }

  behavior of "ConfigLoader for integer"

  "ConfigLoader" should "find a loader for integers" in {
    simple[Int].bind[ConfigLoader]
  }
  it should "load integer" in {
    val integerLoader = simple[Int].bind[ConfigLoader]

    val found = configuration.get[Int]("integerTest")(integerLoader)
    val expect = 9999
    found shouldBe expect
  }

  it should "load the same integer than the default integer loader" in {
    val derived = simple[Int].bind[ConfigLoader]
    val default = ConfigLoader.intLoader

    val found = configuration.get[Int]("integerTest")(derived)
    val expect = configuration.get[Int]("integerTest")(default)

    found shouldBe expect
  }

  behavior of "ConfigLoader for boolean"

  "ConfigLoader" should "find a loader for booleans" in {
    simple[Boolean].bind[ConfigLoader]
  }
  it should "load boolean" in {
    val booleanLoader = simple[Boolean].bind[ConfigLoader]

    val found = configuration.get[Boolean]("booleanTest")(booleanLoader)
    val expect = true
    found shouldBe expect
  }

  it should "load the same boolean than the default boolean loader" in {
    val derived = simple[Boolean].bind[ConfigLoader]
    val default = ConfigLoader.booleanLoader

    val found = configuration.get[Boolean]("booleanTest")(derived)
    val expect = configuration.get[Boolean]("booleanTest")(default)

    found shouldBe expect
  }

  behavior of "ConfigLoader for char"

  "configLoader" should "find a loader for chars" in {
    simple[Char].bind[ConfigLoader]
  }
  it should "load char" in {
    val charLoader = simple[Char].bind[ConfigLoader]

    val found = configuration.get[Char]("charTest")(charLoader)
    val expect = 'a'
    found shouldBe expect
  }

  behavior of "ConfigLoader for byte"

  "ConfigLoader" should "find a loader for bytes" in {
    simple[Byte].bind[ConfigLoader]
  }
  it should "load byte" in {
    val byteLoader = simple[Byte].bind[ConfigLoader]

    val found = configuration.get[Byte]("byteTest")(byteLoader)
    val expect = 'a'.toByte
    found shouldBe expect
  }

  behavior of "ConfigLoader for short"

  "ConfigLoader" should "find a loader for shorts" in {
    simple[Short].bind[ConfigLoader]
  }
  it should "load short" in {
    val shortWriter = simple[Short].bind[ConfigLoader]

    val found = configuration.get[Short]("shortTest")(shortWriter)
    val expect = 'a'.toShort
    found shouldBe expect
  }

  behavior of "ConfigLoader for float"

  "ConfigLoader" should "find a loader for floats" in {
    simple[Float].bind[ConfigLoader]
  }
  it should "load float" in {
    val floatWriter = simple[Float].bind[ConfigLoader]

    val found = configuration.get[Float]("floatTest")(floatWriter)
    val expect = 12.2f
    found shouldBe expect
  }

  behavior of "ConfigLoader for BigDecimal"

  "ConfigLoader" should "find a loader for bigDecimal" in {
    simpleBigDecimal.bind[ConfigLoader]
  }
  it should "load bigDecimal" in {
    val bigDecimalLoader = simpleBigDecimal.bind[ConfigLoader]

    val found = configuration.get[BigDecimal]("bigDecimalTest")(bigDecimalLoader)
    val expect = BigDecimal("199999999999999.000000000001")
    found shouldBe expect
  }

  behavior of "ConfigLoader for Long"

  "ConfigLoader" should "find a loader for long" in {
    simple[Long].bind[ConfigLoader]
  }
  it should "load long" in {
    val longLoader = simple[Long].bind[ConfigLoader]

    val found = configuration.get[Long]("longTest")(longLoader)
    val expect = 88888888888888L
    found shouldBe expect
  }

  it should "load the same long than the default long loader" in {
    val derived = simple[Long].bind[ConfigLoader]
    val default = ConfigLoader.longLoader

    val found = configuration.get[Long]("longTest")(derived)
    val expect = configuration.get[Long]("longTest")(default)

    found shouldBe expect
  }

  behavior of "ConfigLoader for bigInt"

  "ConfigLoader" should "find a loader for bigInt" in {
    simpleBigInt.bind[ConfigLoader]
  }
  it should "load bigInt" in {
    val bigIntLoader = simpleBigInt.bind[ConfigLoader]

    val found = configuration.get[BigInt]("bigIntTest")(bigIntLoader)
    val expect = BigInt("888888888888889999999999999999999")
    found shouldBe expect
  }

  behavior of "ConfigLoader for dateTime"

  "ConfigLoader" should "find a loader for dateTime" in {
    simpleZDT.bind[ConfigLoader]
  }
  it should "load dateTime" in {
    val dateTimeLoader = simpleZDT.bind[ConfigLoader]

    val found = configuration.get[ZonedDateTime]("DateTimeTest")(dateTimeLoader)
    val expect = ZonedDateTime.parse("2018-12-19T21:03:41.050953+01:00[Europe/Paris]")
    found shouldBe expect
  }

  behavior of "ConfigLoader for time"

  "ConfigLoader" should "find a loader for time" in {
    simpleT.bind[ConfigLoader]
  }
  it should "load time" in {
    val timeLoader = simpleT.bind[ConfigLoader]

    val found = configuration.get[LocalTime]("TimeTest")(timeLoader)
    val expect = LocalTime.parse("21:03:41.050953")
    found shouldBe expect
  }

  behavior of "ConfigLoader for date"

  "ConfigLoader" should "find a loader for date" in {
    simpleD.bind[ConfigLoader]
  }
  it should "load date" in {
    val dateLoader = simpleD.bind[ConfigLoader]

    val found = configuration.get[LocalDate]("DateTest")(dateLoader)
    val expect = LocalDate.parse("2018-12-19")
    found shouldBe expect
  }

  behavior of "ConfigLoader for mutliple elements"

  "ConfigLoader" should "find a loader for multiple strings" in {
    many[String, List]().bind[ConfigLoader]
  }
  it should "load strings without any change" in {
    val stringLoader = many[String, List]().bind[ConfigLoader]

    val found = configuration.get[List[String]]("stringListTest")(stringLoader)
    val expect = List("test1", "test2")
    found shouldBe expect
  }
  it should "load the same strings than the default string list ConfigLoader" in {
    val derived = many[String, List]().bind[ConfigLoader].asInstanceOf[ConfigLoader[Seq[String]]]
    val default = ConfigLoader.seqStringLoader

    val found = configuration.get[Seq[String]]("stringListTest")(derived)
    val expect = configuration.get[Seq[String]]("stringListTest")(default)

    found shouldBe expect
  }

  "ConfigLoader" should "find a loader for multiple booleans" in {
    many[Boolean, List]().bind[ConfigLoader]
  }
  it should "load booleans" in {
    val booleanLoader = many[Boolean, List]().bind[ConfigLoader]

    val found = configuration.get[List[Boolean]]("booleanListTest")(booleanLoader)
    val expect = List(true, true)
    found shouldBe expect
  }
  it should "load the same booleans than the default boolean list ConfigLoader" in {
    val derived = many[Boolean, List]().bind[ConfigLoader].asInstanceOf[ConfigLoader[Seq[Boolean]]]
    val default = ConfigLoader.seqBooleanLoader

    val found = configuration.get[Seq[Boolean]]("booleanListTest")(derived)
    val expect = configuration.get[Seq[Boolean]]("booleanListTest")(default)

    found shouldBe expect
  }

  behavior of "ConfigLoader for required fields"

  "ConfigLoader" should "find a loader for required fields" in {
    ConfigLoaderSpecs.requiredStringSchema.bind[ConfigLoader]
  }

  it should "load object without any change" in {
    val requiredLoader = ConfigLoaderSpecs.requiredStringSchema.bind[ConfigLoader]

    val found = configuration.get[RequiredString]("requiredStringTest")(requiredLoader)
    val expect = RequiredString("a string")
    found shouldBe expect
  }

  it should "load object containing array using the index in path (first place)" in {
    val requiredLoader = ConfigLoaderSpecs.requiredStringWithIndexFirstSchema.bind[ConfigLoader]

    val found = configuration.get[RequiredString]("requiredStringWithIndexFirstTest")(requiredLoader)
    val expect = RequiredString("a string")
    found shouldBe expect
  }

  it should "load object containing array using the index in path" in {
    val requiredLoader = ConfigLoaderSpecs.requiredStringWithIndexSchema.bind[ConfigLoader]

    val found = configuration.get[RequiredString]("requiredStringWithIndexTest")(requiredLoader)
    val expect = RequiredString("a string")
    found shouldBe expect
  }

  behavior of "ConfigLoader for optional fields"

  "ConfigLoader" should "find a loader for optional fields" in {
    ConfigLoaderSpecs.optionalStringSchema.bind[ConfigLoader]
  }

  it should "load object if present" in {
    val optionalLoader = ConfigLoaderSpecs.optionalStringSchema.bind[ConfigLoader]

    val found = configuration.get[OptionalString]("optionalStringTest")(optionalLoader)
    val expect = OptionalString(Some("a string"))
    found shouldBe expect
  }

  it should "load none if absent" in {
    val optionalLoader = ConfigLoaderSpecs.optionalStringSchema.bind[ConfigLoader]

    val found = configuration.get[OptionalString]("absentStringTest")(optionalLoader)
    val expect = OptionalString(None)
    found shouldBe expect
  }
}

object ConfigLoaderSpecs {
  //TODO this should'nt be mandatory, a schema should be sufficient for himself
  final case class RequiredString(test: String)
  val requiredStringSchema = (Path \ "test" \ "test").as[String]().inmap(RequiredString.apply)(RequiredString.unapply _ andThen (_.get))
  val requiredStringWithIndexFirstSchema = (Path \ 0 \ "test").as[String]().inmap(RequiredString.apply)(RequiredString.unapply _ andThen (_.get))
  val requiredStringWithIndexSchema = (Path \ "test" \ 0).as[String]().inmap(RequiredString.apply)(RequiredString.unapply _ andThen (_.get))

  final case class OptionalString(test: Option[String])
  val optionalStringSchema = (Path \ "test").asOption[String]().inmap(OptionalString.apply)(OptionalString.unapply _ andThen (_.get))
}
