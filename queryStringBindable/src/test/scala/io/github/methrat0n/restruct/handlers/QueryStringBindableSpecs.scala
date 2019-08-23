package io.github.methrat0n.restruct.handlers

import java.net.URLEncoder
import java.time.{LocalDate, LocalTime, ZonedDateTime}

import io.github.methrat0n.restruct.schema.Schema._
import io.github.methrat0n.restruct.handlers.queryStringBindable._
import io.github.methrat0n.restruct.schema.Interpreter.SimpleInterpreter
import io.github.methrat0n.restruct.schema.{Interpreter, Path, Schema}
import org.scalatest.{FlatSpec, Matchers, Outcome, Succeeded}
import play.api.mvc.QueryStringBindable

import scala.collection.mutable.ListBuffer
import scala.util.{Failure, Success, Try}

class QueryStringBindableSpecs extends FlatSpec with Matchers {
  private val outOfDouble = "1267888889999999999999999999998888867699767611111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111"
  private val query = Map(
    "string" -> Seq("methrat0n"),
    "double" -> Seq("12678888899999999999999999999988888676997676.2"),
    "int" -> Seq("33333"),
    "boolean" -> Seq("true"),
    "char" -> Seq("a"),
    "byte" -> Seq("123"),
    "short" -> Seq("23456"),
    "float" -> Seq("12.2"),
    "bigDecimal" -> Seq(s"$outOfDouble.1"),
    "long" -> Seq("1267888889999999999"),
    "bigInt" -> Seq(outOfDouble),
    "dateTime" -> Seq("2018-12-26T00:08:16.025415+01:00[Europe/Paris]"),
    "time" -> Seq("00:08:16.025415"),
    "date" -> Seq("2018-12-26")
  )

  val allTypes = ListBuffer[String]()
  allTypes ++= query.keys

  private val manyQuery = Map(
    "emptyList" -> Seq(),
    "stringList" -> Seq("string", "string"),
    "intList" -> Seq("1", "0")
  )

  private val complexQuery = Map(
    "string" -> Seq("string"),
    "int" -> Seq("0"),
  )

  private val requiredString = (Path \ "string").as[String]()
  private val requiredStr = (Path \ "str").as[String]()
  private val optionalString = (Path \ "string").asOption[String]()
  private val optionalStr = (Path \ "str").asOption[String]()

  private def shouldBeAnError[T](found: Try[Option[Either[String, T]]]) = found match {
    case Failure(_) | Success(Some(Left(_))) => Succeeded
    case _             => throw new RuntimeException(s"this test should have return an error but instead return $found")
  }

  import language.higherKinds
  private def shouldFail[T, Form[_]: Inter, Err, Value, Inter[Fomat[_]] <: Interpreter[Fomat, T]](
    localSchema: Schema[T, Inter],
    formatName: String,
    localFormat: String,
    failures: Seq[Value],
    test: (Form[T], Value) => Err,
    errorSwitch: Try[Err] => Outcome
  ) = {
    failures.foreach(failure => {
      it should s"not read $failure with $localFormat $formatName" in {
        errorSwitch(Try(test(localSchema.bind[Form], failure)))
      }
    })
  }

  behavior of "QueryStringBindable errors"

  it should "not read absent value" in {
    val decimalBindable = simple[Double].bind[QueryStringBindable]

    val found = decimalBindable.bind("str", query)
    val expect = None
    found shouldBe expect
  }

  behavior of "QueryStringBindable for strings"

  "QueryStringBindable" should "find a parser for strings" in {
    simpleString.bind[QueryStringBindable]
  }
  it should "read string without any change" in {
    val stringBindable = simpleString.bind[QueryStringBindable]

    val found = stringBindable.bind("string", query)
    val expect = Some(Right("methrat0n"))
    found shouldBe expect
  }
  it should "write string without any change" in {
    val stringBindable = simpleString.bind[QueryStringBindable]

    val value = "methrat0n"
    val found = stringBindable.unbind("string", value)
    val expect = "string=methrat0n"
    found shouldBe expect
  }
  it should "read the same string than the default string QueryStringBindable" in {
    val derived = simpleString.bind[QueryStringBindable]
    val default = QueryStringBindable.bindableString

    val found = derived.bind("string", query)
    val expect = default.bind("string", query)

    found shouldBe expect
  }
  it should "write the same string than the default string QueryStringBindable" in {
    val derived = simpleString.bind[QueryStringBindable]
    val default = QueryStringBindable.bindableString

    val value = "methrat0n"
    val found = derived.unbind("string", value)
    val expect = default.unbind("string", value)

    found shouldBe expect
  }

  behavior of "QueryStringBindable for decimal"

  "QueryStringBindable" should "find a bindable for decimals" in {
    simple[Double].bind[QueryStringBindable]
  }
  it should "read decimal" in {
    val decimalBindable = simple[Double].bind[QueryStringBindable]

    val found = decimalBindable.bind("double", query)
    val expect = Some(Right(12678888899999999999999999999988888676997676.2))
    found shouldBe expect
  }
  it should "write decimal" in {
    val decimalBindable = simple[Double].bind[QueryStringBindable]

    val found = decimalBindable.unbind("double", 12.2)
    val expect = "double=12.2"
    found shouldBe expect
  }
  it should "read the same decimal than the default decimal QueryStringBindable" in {
    val derived = simple[Double].bind[QueryStringBindable]
    val default = QueryStringBindable.bindableDouble

    val found = derived.bind("double", query)
    val expect = default.bind("double", query)

    found shouldBe expect
  }
  it should "write the same decimal than the default decimal QueryStringBindable" in {
    val derived = simple[Double].bind[QueryStringBindable]
    val default = QueryStringBindable.bindableDouble

    val found = derived.unbind("double", 12.2)
    val expect = default.unbind("double", 12.2)

    found shouldBe expect
  }
  it should "read float with decimal bindable" in {
    val decimalBindable = simple[Double].bind[QueryStringBindable]

    val found = decimalBindable.bind("float", query)
    val expect = Some(Right(12.2))
    found shouldBe expect
  }
  it should "read int with decimal bindable" in {
    val decimalBindable = simple[Double].bind[QueryStringBindable]

    val found = decimalBindable.bind("int", query)
    val expect = Some(Right(33333))
    found shouldBe expect
  }
  it should "read short with decimal bindable" in {
    val decimalBindable = simple[Double].bind[QueryStringBindable]

    val found = decimalBindable.bind("short", query)
    val expect = Some(Right(23456))
    found shouldBe expect
  }
  it should "read byte with decimal bindable" in {
    val decimalBindable = simple[Double].bind[QueryStringBindable]

    val found = decimalBindable.bind("byte", query)
    val expect = Some(Right(123))
    found shouldBe expect
  }
  it should "read long with decimal bindable" in {
    val decimalBindable = simple[Double].bind[QueryStringBindable]

    val found = decimalBindable.bind("long", query)
    val expect = Some(Right(1267888889999999999L.toDouble))
    found shouldBe expect
  }
  it should "read bigInt with decimal bindable" in {
    val decimalBindable = simple[Double].bind[QueryStringBindable]

    val found = decimalBindable.bind("bigInt", query)
    val expect = Some(Right(Double.PositiveInfinity))
    found shouldBe expect
  }
  it should "read bigDecimal with decimal bindable" in {
    val decimalBindable = simple[Double].bind[QueryStringBindable]

    val found = decimalBindable.bind("bigDecimal", query)
    val expect = Some(Right(Double.PositiveInfinity))
    found shouldBe expect
  }
  shouldFail[Double, QueryStringBindable, Option[Either[String, Double]], String, SimpleInterpreter[?[_], Double]](
    simple[Double],
    "QueryStringBindable",
    "double",
    allTypes.toSeq.diff(List("float", "int", "long", "bigInt", "bigDecimal", "short", "byte", "double")),
    (bindable, value) => bindable.bind(value, query),
    shouldBeAnError _
  )

  behavior of "QueryStringBindable for integer"

  "QueryStringBindable" should "find a bindable for integers" in {
    simple[Int].bind[QueryStringBindable]
  }
  it should "read integer" in {
    val integerBindable = simple[Int].bind[QueryStringBindable]

    val found = integerBindable.bind("int", query)
    val expect = Some(Right(33333))
    found shouldBe expect
  }
  it should "write integer" in {
    val integerBindable = simple[Int].bind[QueryStringBindable]

    val found = integerBindable.unbind("int", 1234)
    val expect = "int=1234"
    found shouldBe expect
  }
  it should "read the same integer than the default integer QueryStringBindable" in {
    val derived = simple[Int].bind[QueryStringBindable]
    val default = QueryStringBindable.bindableInt

    val found = derived.bind("int", query)
    val expect = default.bind("int", query)

    found shouldBe expect
  }
  it should "write the same integer than the default integer QueryStringBindable" in {
    val derived = simple[Int].bind[QueryStringBindable]
    val default = QueryStringBindable.bindableInt

    val found = derived.unbind("int", 1234)
    val expect = default.unbind("int", 1234)

    found shouldBe expect
  }
  it should "read byte with int bindable" in {
    val integerBindable = simple[Int].bind[QueryStringBindable]

    val found = integerBindable.bind("byte", query)
    val expect = Some(Right(123))
    found shouldBe expect
  }
  it should "read short with int bindable" in {
    val integerBindable = simple[Int].bind[QueryStringBindable]

    val found = integerBindable.bind("short", query)
    val expect = Some(Right(23456))
    found shouldBe expect
  }
  shouldFail[Int, QueryStringBindable, Option[Either[String, Int]], String, SimpleInterpreter[?[_], Int]](
    simple[Int],
    "QueryStringBindable",
    "int",
    allTypes.toSeq.diff(List("short", "byte", "int")),
    (bindable, failure) => bindable.bind(failure, query),
    shouldBeAnError _
  )

  behavior of "QueryStringBindable for boolean"

  "QueryStringBindable" should "find a bindable for booleans" in {
    simple[Boolean].bind[QueryStringBindable]
  }
  it should "read boolean" in {
    val booleanBindable = simple[Boolean].bind[QueryStringBindable]

    val found = booleanBindable.bind("boolean", query)
    val expect = Some(Right(true))
    found shouldBe expect
  }
  it should "write boolean" in {
    val booleanBindable = simple[Boolean].bind[QueryStringBindable]

    val found = booleanBindable.unbind("boolean", true)
    val expect = "boolean=true"
    found shouldBe expect
  }
  it should "read the same boolean than the default boolean bindable" in {
    val derived = simple[Boolean].bind[QueryStringBindable]
    val default = QueryStringBindable.bindableBoolean

    val found = derived.bind("boolean", query)
    val expect = default.bind("boolean", query)

    found shouldBe expect
  }
  it should "write the same boolean than the default boolean bindable" in {
    val derived = simple[Boolean].bind[QueryStringBindable]
    val default = QueryStringBindable.bindableBoolean

    val found = derived.unbind("boolean", true)
    val expect = default.unbind("boolean", true)

    found shouldBe expect
  }
  shouldFail[Boolean, QueryStringBindable, Option[Either[String, Boolean]], String, SimpleInterpreter[?[_], Boolean]](
    simple[Boolean],
    "QueryStringBindable",
    "boolean",
    allTypes.toSeq.diff(List("boolean")),
    (bindable, failure) => bindable.bind(failure, query),
    shouldBeAnError _
  )

  behavior of "QueryStringBindable for char"

  "QueryStringBindable" should "find a bindable for chars" in {
    simple[Char].bind[QueryStringBindable]
  }
  it should "read char" in {
    val charBindable = simple[Char].bind[QueryStringBindable]

    val found = charBindable.bind("char", query)
    val expect = Some(Right('a'))
    found shouldBe expect
  }
  it should "write char" in {
    val charBindable = simple[Char].bind[QueryStringBindable]

    val found = charBindable.unbind("char", 'a')
    val expect = "char=a"
    found shouldBe expect
  }
  it should "read the same char than the default char bindable" in {
    val derived = simple[Char].bind[QueryStringBindable]
    val default = QueryStringBindable.bindableChar

    val found = derived.bind("char", query)
    val expect = default.bind("char", query)

    found shouldBe expect
  }
  it should "write the same char than the default char bindable" in {
    val derived = simple[Char].bind[QueryStringBindable]
    val default = QueryStringBindable.bindableChar

    val found = derived.unbind("char", 'a')
    val expect = default.unbind("char", 'a')

    found shouldBe expect
  }
  shouldFail[Char, QueryStringBindable, Option[Either[String, Char]], String, SimpleInterpreter[?[_], Char]](
    simple[Char],
    "QueryStringBindable",
    "char",
    allTypes.toSeq.diff(List("char")),
    (bindable, failure) => bindable.bind(failure, query),
    shouldBeAnError _
  )

  behavior of "QueryStringBindable for byte"

  "QueryStringBindable" should "find a bindable for bytes" in {
    simple[Byte].bind[QueryStringBindable]
  }
  it should "read byte" in {
    val byteBindable = simple[Byte].bind[QueryStringBindable]

    val found = byteBindable.bind("byte", query)
    val expect = Some(Right(123))
    found shouldBe expect
  }
  it should "write byte" in {
    val byteBindable = simple[Byte].bind[QueryStringBindable]

    val found = byteBindable.unbind("byte", 'a'.toByte)
    val expect = "byte=a"
    found shouldBe expect
  }
  it should "read char with byte bindable" in {
    val byteBindable = simple[Byte].bind[QueryStringBindable]

    val found = byteBindable.bind("char", query)
    val expect = Some(Right('a'.toByte))
    found shouldBe expect
  }
  shouldFail[Byte, QueryStringBindable, Option[Either[String, Byte]], String, SimpleInterpreter[?[_], Byte]](
    simple[Byte],
    "QueryStringBindable",
    "byte",
    allTypes.toSeq.diff(List("char", "byte")),
    (bindable, failure) => bindable.bind(failure, query),
    shouldBeAnError _
  )

  behavior of "QueryStringBindable for short"

  "QueryStringBindable" should "find a bindable for shorts" in {
    simple[Short].bind[QueryStringBindable]
  }
  it should "read short" in {
    val shortBindable = simple[Short].bind[QueryStringBindable]

    val found = shortBindable.bind("short", query)
    val expect = Some(Right(23456))
    found shouldBe expect
  }
  it should "write short" in {
    val shortBindable = simple[Short].bind[QueryStringBindable]

    val found = shortBindable.unbind("short", 'a'.toShort)
    val expect = "short=a"
    found shouldBe expect
  }
  it should "read char with short bindable" in {
    val shortBindable = simple[Short].bind[QueryStringBindable]

    val found = shortBindable.bind("char", query)
    val expect = Some(Right('a'.toShort))
    found shouldBe expect
  }
  it should "read byte with short bindable" in {
    val shortBindable = simple[Short].bind[QueryStringBindable]

    val found = shortBindable.bind("byte", query)
    val expect = Some(Right(123))
    found shouldBe expect
  }
  shouldFail[Short, QueryStringBindable, Option[Either[String, Short]], String, SimpleInterpreter[?[_], Short]](
    simple[Short],
    "QueryStringBindable",
    "short",
    allTypes.toSeq.diff(List("char", "byte", "short")),
    (bindable, failure) => bindable.bind(failure, query),
    shouldBeAnError _
  )

  behavior of "QueryStringBindable for float"

  "QueryStringBindable" should "find a bindable for floats" in {
    simple[Float].bind[QueryStringBindable]
  }
  it should "read float" in {
    val floatBindable = simple[Float].bind[QueryStringBindable]

    val found = floatBindable.bind("float", query)
    val expect = Some(Right(12.2f))
    found shouldBe expect
  }
  it should "write float" in {
    val floatBindable = simple[Float].bind[QueryStringBindable]

    val found = floatBindable.unbind("float", 12.2f)
    val expect = "float=12.2"
    found shouldBe expect
  }
  it should "read the same float than the default float bindable" in {
    val derived = simple[Float].bind[QueryStringBindable]
    val default = QueryStringBindable.bindableFloat

    val found = derived.bind("float", query)
    val expect = default.bind("float", query)

    found shouldBe expect
  }
  it should "write the same float than the default float bindable" in {
    val derived = simple[Float].bind[QueryStringBindable]
    val default = QueryStringBindable.bindableFloat

    val found = derived.unbind("float", 12.2f)
    val expect = default.unbind("float", 12.2f)

    found shouldBe expect
  }
  it should "read integer with float bindable" in {
    val floatBindable = simple[Float].bind[QueryStringBindable]

    val found = floatBindable.bind("int", query)
    val expect = Some(Right(33333))
    found shouldBe expect
  }
  it should "read short with float bindable" in {
    val floatBindable = simple[Float].bind[QueryStringBindable]

    val found = floatBindable.bind("short", query)
    val expect = Some(Right(23456))
    found shouldBe expect
  }
  it should "read byte with float bindable" in {
    val floatBindable = simple[Float].bind[QueryStringBindable]

    val found = floatBindable.bind("byte", query)
    val expect = Some(Right(123))
    found shouldBe expect
  }
  it should "read long with float bindable" in {
    val floatBindable = simple[Float].bind[QueryStringBindable]

    val found = floatBindable.bind("long", query)
    val expect = Some(Right(1267888889999999999L.toFloat))
    found shouldBe expect
  }
  it should "read infinty for large decimal with float bindable" in {
    val floatBindable = simple[Float].bind[QueryStringBindable]

    val found = floatBindable.bind("double", query)
    val expect = Some(Right(Float.PositiveInfinity))
    found shouldBe expect
  }
  it should "read infinty for bigInt with float bindable" in {
    val floatBindable = simple[Float].bind[QueryStringBindable]

    val found = floatBindable.bind("bigInt", query)
    val expect = Some(Right(Float.PositiveInfinity))
    found shouldBe expect
  }
  it should "read infinty for bigDecimal with float bindable" in {
    val floatBindable = simple[Float].bind[QueryStringBindable]

    val found = floatBindable.bind("bigDecimal", query)
    val expect = Some(Right(Float.PositiveInfinity))
    found shouldBe expect
  }
  shouldFail[Float, QueryStringBindable, Option[Either[String, Float]], String, SimpleInterpreter[?[_], Float]](
    simple[Float],
    "QueryStringBindable",
    "float",
    allTypes.toSeq.diff(List("int", "long", "float", "double", "bigInt", "bigDecimal", "short", "byte")),
    (bindable, failure) => bindable.bind(failure, query),
    shouldBeAnError _
  )

  behavior of "QueryStringBindable for BigDecimal"

  "QueryStringBindable" should "find a bindable for bigDecimal" in {
    simpleBigDecimal.bind[QueryStringBindable]
  }
  it should "read bigDecimal" in {
    val bigDecimalBindable = simpleBigDecimal.bind[QueryStringBindable]

    val found = bigDecimalBindable.bind("bigDecimal", query)
    val expect = Some(Right(BigDecimal(s"$outOfDouble.1")))
    found shouldBe expect
  }
  it should "write bigDecimal" in {
    val bigDecimalBindable = simpleBigDecimal.bind[QueryStringBindable]

    val found = bigDecimalBindable.unbind("bigDecimal", BigDecimal("12678888899999999999999999999988888676997676"))
    val expect = "bigDecimal=12678888899999999999999999999988888676997676"
    found shouldBe expect
  }
  it should "read integer with bigDecimal bindable" in {
    val bigDecimalBindable = simpleBigDecimal.bind[QueryStringBindable]

    val found = bigDecimalBindable.bind("int", query)
    val expect = Some(Right(BigDecimal(33333)))
    found shouldBe expect
  }
  it should "read long with bigDecimal bindable" in {
    val bigDecimalBindable = simpleBigDecimal.bind[QueryStringBindable]

    val found = bigDecimalBindable.bind("long", query)
    val expect = Some(Right(BigDecimal("1267888889999999999")))
    found shouldBe expect
  }
  it should "read decimal with bigDecimal bindable" in {
    val bigDecimalBindable = simpleBigDecimal.bind[QueryStringBindable]

    val found = bigDecimalBindable.bind("double", query)
    val expect = Some(Right(BigDecimal("12678888899999999999999999999988888676997676.2")))
    found shouldBe expect
  }
  it should "read float with bigDecimal bindable" in {
    val bigDecimalBindable = simpleBigDecimal.bind[QueryStringBindable]

    val found = bigDecimalBindable.bind("float", query)
    val expect = Some(Right(BigDecimal(12.2)))
    found shouldBe expect
  }
  it should "read bigInt with bigDecimal bindable" in {
    val bigDecimalBindable = simpleBigDecimal.bind[QueryStringBindable]

    val found = bigDecimalBindable.bind("bigInt", query)
    val expect = Some(Right(BigDecimal(outOfDouble)))
    found shouldBe expect
  }
  it should "read short with bigDecimal bindable" in {
    val bigDecimalBindable = simpleBigDecimal.bind[QueryStringBindable]

    val found = bigDecimalBindable.bind("short", query)
    val expect = Some(Right(BigDecimal("23456")))
    found shouldBe expect
  }
  it should "read byte with bigDecimal bindable" in {
    val bigDecimalBindable = simpleBigDecimal.bind[QueryStringBindable]

    val found = bigDecimalBindable.bind("byte", query)
    val expect = Some(Right(BigDecimal("123")))
    found shouldBe expect
  }
  shouldFail[BigDecimal, QueryStringBindable, Option[Either[String, BigDecimal]], String, SimpleInterpreter[?[_], BigDecimal]](
    simpleBigDecimal,
    "QueryStringBindable",
    "bigDecimal",
    allTypes.toSeq.diff(List("int", "double", "float", "bigInt", "long", "bigDecimal", "short", "byte")),
    (bindable, failure) => bindable.bind(failure, query),
    shouldBeAnError _
  )

  behavior of "QueryStringBindable for Long"

  "QueryStringBindable" should "find a bindable for long" in {
    simple[Long].bind[QueryStringBindable]
  }
  it should "read long" in {
    val longBindable = simple[Long].bind[QueryStringBindable]

    val found = longBindable.bind("long", query)
    val expect = Some(Right(1267888889999999999L))
    found shouldBe expect
  }
  it should "write long" in {
    val longBindable = simple[Long].bind[QueryStringBindable]

    val found = longBindable.unbind("long", 1267888889999999999L)
    val expect = "long=1267888889999999999"
    found shouldBe expect
  }
  it should "read the same long than the default long bindable" in {
    val derived = simple[Long].bind[QueryStringBindable]
    val default = QueryStringBindable.bindableFloat

    val found = derived.bind("long", query)
    val expect = default.bind("long", query)

    found shouldBe expect
  }
  it should "write the same long than the default long bindable" in {
    val derived = simple[Long].bind[QueryStringBindable]
    val default = QueryStringBindable.bindableLong

    val found = derived.unbind("long", 1267888889999999999L)
    val expect = default.unbind("long", 1267888889999999999L)

    found shouldBe expect
  }
  it should "read integer with long bindable" in {
    val longBindable = simple[Long].bind[QueryStringBindable]

    val found = longBindable.bind("int", query)
    val expect = Some(Right(33333))
    found shouldBe expect
  }
  it should "read short with long bindable" in {
    val longBindable = simple[Long].bind[QueryStringBindable]

    val found = longBindable.bind("short", query)
    val expect = Some(Right(23456))
    found shouldBe expect
  }
  it should "read byte with long bindable" in {
    val longBindable = simple[Long].bind[QueryStringBindable]

    val found = longBindable.bind("byte", query)
    val expect = Some(Right(123))
    found shouldBe expect
  }
  shouldFail[Long, QueryStringBindable, Option[Either[String, Long]], String, SimpleInterpreter[?[_], Long]](
    simple[Long],
    "QueryStringBindable",
    "long",
    allTypes.toSeq.diff(List("int", "long", "short", "byte")),
    (bindable, failure) => bindable.bind(failure, query),
    shouldBeAnError _
  )

  behavior of "QueryStringBindable for bigInt"

  "QueryStringBindable" should "find a bindable for bigInt" in {
    simpleBigInt.bind[QueryStringBindable]
  }
  it should "read bigInt" in {
    val bigIntBindable = simpleBigInt.bind[QueryStringBindable]

    val found = bigIntBindable.bind("bigInt", query)
    val expect = Some(Right(BigInt(outOfDouble)))
    found shouldBe expect
  }
  it should "write bigInt" in {
    val bigIntBindable = simpleBigInt.bind[QueryStringBindable]

    val found = bigIntBindable.unbind("bigInt", BigInt("12678888899999999999999999999988888676997676"))
    val expect = "bigInt=12678888899999999999999999999988888676997676"
    found shouldBe expect
  }
  it should "read integer with bigInt bindable" in {
    val bigIntBindable = simpleBigInt.bind[QueryStringBindable]

    val found = bigIntBindable.bind("int", query)
    val expect = Some(Right(BigInt(33333)))
    found shouldBe expect
  }
  it should "read short with bigInt bindable" in {
    val bigIntBindable = simpleBigInt.bind[QueryStringBindable]

    val found = bigIntBindable.bind("short", query)
    val expect = Some(Right(BigInt(23456)))
    found shouldBe expect
  }
  it should "read long with bigInt bindable" in {
    val bigIntBindable = simpleBigInt.bind[QueryStringBindable]

    val found = bigIntBindable.bind("long", query)
    val expect = Some(Right(BigInt("1267888889999999999")))
    found shouldBe expect
  }
  it should "read byte with bigInt bindable" in {
    val bigIntBindable = simpleBigInt.bind[QueryStringBindable]

    val found = bigIntBindable.bind("byte", query)
    val expect = Some(Right(BigInt("123")))
    found shouldBe expect
  }
  shouldFail[BigInt, QueryStringBindable, Option[Either[String, BigInt]], String, SimpleInterpreter[?[_], BigInt]](
    simpleBigInt,
    "QueryStringBindable",
    "bigInt",
    allTypes.toSeq.diff(List("int", "long", "bigInt", "short", "byte")),
    (bindable, failure) => bindable.bind(failure, query),
    shouldBeAnError _
  )

  behavior of "QueryStringBindable for dateTime"

  "QueryStringBindable" should "find a bindable for dateTime" in {
    simpleZDT.bind[QueryStringBindable]
  }
  it should "read dateTime" in {
    val dateTimeBindable = simpleZDT.bind[QueryStringBindable]

    val found = dateTimeBindable.bind("dateTime", query)
    val expect = Some(Right(ZonedDateTime.parse("2018-12-26T00:08:16.025415+01:00[Europe/Paris]")))
    found shouldBe expect
  }
  it should "write dateTime" in {
    val dateTimeBindable = simpleZDT.bind[QueryStringBindable]

    val found = dateTimeBindable.unbind("dateTime", ZonedDateTime.parse("2018-12-26T00:08:16.025415+01:00[Europe/Paris]"))
    val expect = "dateTime=" + URLEncoder.encode("2018-12-26T00:08:16.025415+01:00[Europe/Paris]", "UTF-8")
    found shouldBe expect
  }
  shouldFail[ZonedDateTime, QueryStringBindable, Option[Either[String, ZonedDateTime]], String, SimpleInterpreter[?[_], ZonedDateTime]](
    simpleZDT,
    "QueryStringBindable",
    "dateTime",
    allTypes.toSeq.diff(List("dateTime")),
    (bindable, failure) => bindable.bind(failure, query),
    shouldBeAnError _
  )

  behavior of "QueryStringBindable for time"

  "QueryStringBindable" should "find a bindable for time" in {
    simpleT.bind[QueryStringBindable]
  }
  it should "read time" in {
    val timeBindable = simpleT.bind[QueryStringBindable]

    val found = timeBindable.bind("time", query)
    val expect = Some(Right(LocalTime.parse("00:08:16.025415")))
    found shouldBe expect
  }
  it should "write time" in {
    val timeBindable = simpleT.bind[QueryStringBindable]

    val found = timeBindable.unbind("time", LocalTime.parse("00:08:16.025415"))
    val expect = "time=" + URLEncoder.encode("00:08:16.025415", "UTF-8")
    found shouldBe expect
  }
  shouldFail[LocalTime, QueryStringBindable, Option[Either[String, LocalTime]], String, SimpleInterpreter[?[_], LocalTime]](
    simpleT,
    "QueryStringBindable",
    "time",
    allTypes.toSeq.diff(List("time")),
    (bindable, failure) => bindable.bind(failure, query),
    shouldBeAnError _
  )

  behavior of "QueryStringBindable for date"

  it should "read date" in {
    val dateBindable = simpleD.bind[QueryStringBindable]

    val found = dateBindable.bind("date", query)
    val expect = Some(Right(LocalDate.parse("2018-12-26")))
    found shouldBe expect
  }
  it should "write date" in {
    val dateBindable = simpleD.bind[QueryStringBindable]

    val found = dateBindable.unbind("date", LocalDate.parse("2018-12-26"))
    val expect = "date=2018-12-26"
    found shouldBe expect
  }
  shouldFail[LocalDate, QueryStringBindable, Option[Either[String, LocalDate]], String, SimpleInterpreter[?[_], LocalDate]](
    simpleD,
    "QueryStringBindable",
    "date",
    allTypes.toSeq.diff(List("date")),
    (bindable, failure) => bindable.bind(failure, query),
    shouldBeAnError _
  )

  behavior of "QueryStringBindable for empty list"

  it should "read an empty List when key is not present" in {
    val emptyStringListBindable = many[String, List]().bind[QueryStringBindable]

    val found = emptyStringListBindable.bind("empty", manyQuery)
    val expect = Some(Right(List()))
    found shouldBe expect
  }
  it should "read an empty List" in {
    val emptyStringListBindable = many[String, List]().bind[QueryStringBindable]

    val found = emptyStringListBindable.bind("emptyList", manyQuery)
    val expect = Some(Right(List()))
    found shouldBe expect
  }
  it should "read a string List" in {
    val stringListBindable = many[String, List]().bind[QueryStringBindable]

    val found = stringListBindable.bind("stringList", manyQuery)
    val expect = Some(Right(List("string", "string")))
    found shouldBe expect
  }
  it should "read an int List" in {
    val intListBindable = many[Int, List]().bind[QueryStringBindable]

    val found = intListBindable.bind("intList", manyQuery)
    val expect = Some(Right(List(1, 0)))
    found shouldBe expect
  }

  behavior of "QueryStringBindable fields"

  it should "read required string without any change" in {
    val requiredStringBindable = requiredString.bind[QueryStringBindable]

    val found = requiredStringBindable.bind("", complexQuery)
    val expect = Some(Right("string"))
    found shouldBe expect
  }
  it should "write required string without any change" in {
    val requiredStringBindable = requiredString.bind[QueryStringBindable]

    val found = requiredStringBindable.unbind("", "string")
    val expect = "string=string"
    found shouldBe expect
  }
  it should "complain when required string is missing" in {
    val requiredStringBindable = requiredStr.bind[QueryStringBindable]

    val found = requiredStringBindable.bind("", complexQuery)
    val expect = None
    found shouldBe expect
  }
  it should "read optional string" in {
    val optionalStringBindable = optionalString.bind[QueryStringBindable]

    val found = optionalStringBindable.bind("", complexQuery)
    val expect = Some(Right(Some("string")))
    found shouldBe expect
  }
  it should "read None when optional value is missing" in {
    val optionalStringBindable = optionalStr.bind[QueryStringBindable]

    val found = optionalStringBindable.bind("", complexQuery)
    val expect = Some(Right(None))
    found shouldBe expect
  }
  it should "read object from query string" in {
    val requiredObjectBindable = RequiredStringAndInt.schema.bind[QueryStringBindable]

    val found = requiredObjectBindable.bind("", complexQuery)
    val expect = Some(Right(RequiredStringAndInt("string", 0)))
    found shouldBe expect
  }
  it should "read object with optional fields from query string" in {
    val requiredObjectBindable = StringAndMaybeInt.schema.bind[QueryStringBindable]

    val found = requiredObjectBindable.bind("", complexQuery)
    val expect = Some(Right(StringAndMaybeInt("string", Some(0))))
    found shouldBe expect
  }
  it should "read None for missing optional fields from query string" in {
    val requiredObjectBindable = StringAndMaybeIn.schema.bind[QueryStringBindable]

    val found = requiredObjectBindable.bind("", complexQuery)
    val expect = Some(Right(StringAndMaybeIn("string", None)))
    found shouldBe expect
  }
}

import language.postfixOps
final case class RequiredStringAndInt(string: String, int: Int)
object RequiredStringAndInt {
  val schema = (
    (Path \ "string").as[String]() and
    (Path \ "int").as[Int]()
  ).inmap(RequiredStringAndInt.apply _ tupled)(RequiredStringAndInt.unapply _ andThen(_.get))
}

final case class StringAndMaybeInt(string: String, int: Option[Int])
object StringAndMaybeInt {
  val schema = (
    (Path \ "string").as[String]() and
      (Path \ "int").asOption[Int]()
    ).inmap(StringAndMaybeInt.apply _ tupled)(StringAndMaybeInt.unapply _ andThen(_.get))
}

final case class StringAndMaybeIn(string: String, in: Option[Int])
object StringAndMaybeIn {
  val schema = (
    (Path \ "string").as[String]() and
      (Path \ "in").asOption[Int]()
    ).inmap(StringAndMaybeIn.apply _ tupled)(StringAndMaybeIn.unapply _ andThen(_.get))
}
