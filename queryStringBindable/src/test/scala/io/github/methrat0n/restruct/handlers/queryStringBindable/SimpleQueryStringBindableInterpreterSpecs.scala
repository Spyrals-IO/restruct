package io.github.methrat0n.restruct.handlers.queryStringBindable

import java.net.URLEncoder
import java.time.{ LocalDate, LocalTime, ZonedDateTime }

import io.github.methrat0n.restruct.core.data.schema.FieldAlgebra
import io.github.methrat0n.restruct.schema.Schema
import org.scalatest.{ time => _, _ }
import play.api.mvc.QueryStringBindable

import scala.collection.mutable.ListBuffer

class SimpleQueryStringBindableInterpreterSpecs extends FlatSpec with Matchers {

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
  query.keys.copyToBuffer(allTypes)

  private def shouldBeAnError[T](found: Option[Either[String, T]]) = found match {
    case Some(Left(_)) => Succeeded
    case _             => throw new RuntimeException(s"this test should have return an error but instead return $found")
  }

  import language.higherKinds
  private def shouldFail[T, Format[_], Err](
    interpreter: FieldAlgebra[Format],
    localSchema: Schema[T],
    formatName: String,
    localFormat: String,
    failures: Seq[String],
    test: (Format[T], String) => Err,
    errorSwitch: Err => Outcome
  ) = {
    failures.foreach(failure => {
      it should s"not read $failure with $localFormat $formatName" in {
        errorSwitch(test(localSchema.bind(interpreter), failure))
      }
    })
  }

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
    val expect = Some(Right(12678888899999999999999999999988888676997676.2))
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
  it should "read float with decimal bindable" in {
    val decimalBindable = decimal.bind(queryStringBindable)

    val found = decimalBindable.bind("float", query)
    val expect = Some(Right(12.2))
    found shouldBe expect
  }
  it should "read int with decimal bindable" in {
    val decimalBindable = decimal.bind(queryStringBindable)

    val found = decimalBindable.bind("int", query)
    val expect = Some(Right(33333))
    found shouldBe expect
  }
  it should "read short with decimal bindable" in {
    val decimalBindable = decimal.bind(queryStringBindable)

    val found = decimalBindable.bind("short", query)
    val expect = Some(Right(23456))
    found shouldBe expect
  }
  it should "read byte with decimal bindable" in {
    val decimalBindable = decimal.bind(queryStringBindable)

    val found = decimalBindable.bind("byte", query)
    val expect = Some(Right(123))
    found shouldBe expect
  }
  it should "read long with decimal bindable" in {
    val decimalBindable = decimal.bind(queryStringBindable)

    val found = decimalBindable.bind("long", query)
    val expect = Some(Right(1267888889999999999l.toDouble))
    found shouldBe expect
  }
  it should "read bigInt with decimal bindable" in {
    val decimalBindable = decimal.bind(queryStringBindable)

    val found = decimalBindable.bind("bigInt", query)
    val expect = Some(Right(Double.PositiveInfinity))
    found shouldBe expect
  }
  it should "read bigDecimal with decimal bindable" in {
    val decimalBindable = decimal.bind(queryStringBindable)

    val found = decimalBindable.bind("bigDecimal", query)
    val expect = Some(Right(Double.PositiveInfinity))
    found shouldBe expect
  }
  shouldFail[Double, QueryStringBindable, Option[Either[String, Double]]](
    queryStringBindable,
    decimal,
    "QueryStringBindable",
    "double",
    allTypes -- List("float", "int", "long", "bigInt", "bigDecimal", "short", "byte", "double"),
    (bindable, failure) => bindable.bind(failure, query),
    shouldBeAnError _
  )

  behavior of "QueryStringBindable for integer"

  "QueryStringBindable" should "find a bindable for integers" in {
    integer.bind(queryStringBindable)
  }
  it should "read integer" in {
    val integerBindable = integer.bind(queryStringBindable)

    val found = integerBindable.bind("int", query)
    val expect = Some(Right(33333))
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
  it should "read byte with int bindable" in {
    val integerBindable = integer.bind(queryStringBindable)

    val found = integerBindable.bind("byte", query)
    val expect = Some(Right(123))
    found shouldBe expect
  }
  it should "read short with int bindable" in {
    val integerBindable = integer.bind(queryStringBindable)

    val found = integerBindable.bind("short", query)
    val expect = Some(Right(23456))
    found shouldBe expect
  }
  shouldFail[Int, QueryStringBindable, Option[Either[String, Int]]](
    queryStringBindable,
    integer,
    "QueryStringBindable",
    "int",
    allTypes -- List("short", "byte", "int"),
    (bindable, failure) => bindable.bind(failure, query),
    shouldBeAnError _
  )

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
  shouldFail[Boolean, QueryStringBindable, Option[Either[String, Boolean]]](
    queryStringBindable,
    boolean,
    "QueryStringBindable",
    "boolean",
    allTypes -- List("boolean"),
    (bindable, failure) => bindable.bind(failure, query),
    shouldBeAnError _
  )

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
  shouldFail[Char, QueryStringBindable, Option[Either[String, Char]]](
    queryStringBindable,
    char,
    "QueryStringBindable",
    "char",
    allTypes -- List("char"),
    (bindable, failure) => bindable.bind(failure, query),
    shouldBeAnError _
  )

  behavior of "QueryStringBindable for byte"

  "QueryStringBindable" should "find a bindable for bytes" in {
    byte.bind(queryStringBindable)
  }
  it should "read byte" in {
    val byteBindable = byte.bind(queryStringBindable)

    val found = byteBindable.bind("byte", query)
    val expect = Some(Right(123))
    found shouldBe expect
  }
  it should "write byte" in {
    val byteBindable = byte.bind(queryStringBindable)

    val found = byteBindable.unbind("byte", 'a'.toByte)
    val expect = "byte=a"
    found shouldBe expect
  }
  it should "read char with byte bindable" in {
    val byteBindable = byte.bind(queryStringBindable)

    val found = byteBindable.bind("char", query)
    val expect = Some(Right('a'.toByte))
    found shouldBe expect
  }
  shouldFail[Byte, QueryStringBindable, Option[Either[String, Byte]]](
    queryStringBindable,
    byte,
    "QueryStringBindable",
    "byte",
    allTypes -- List("char", "byte"),
    (bindable, failure) => bindable.bind(failure, query),
    shouldBeAnError _
  )

  behavior of "QueryStringBindable for short"

  "QueryStringBindable" should "find a bindable for shorts" in {
    short.bind(queryStringBindable)
  }
  it should "read short" in {
    val shortBindable = short.bind(queryStringBindable)

    val found = shortBindable.bind("short", query)
    val expect = Some(Right(23456))
    found shouldBe expect
  }
  it should "write short" in {
    val shortBindable = short.bind(queryStringBindable)

    val found = shortBindable.unbind("short", 'a'.toShort)
    val expect = "short=a"
    found shouldBe expect
  }
  it should "read char with short bindable" in {
    val shortBindable = short.bind(queryStringBindable)

    val found = shortBindable.bind("char", query)
    val expect = Some(Right('a'.toShort))
    found shouldBe expect
  }
  it should "read byte with short bindable" in {
    val shortBindable = short.bind(queryStringBindable)

    val found = shortBindable.bind("byte", query)
    val expect = Some(Right(123))
    found shouldBe expect
  }
  shouldFail[Short, QueryStringBindable, Option[Either[String, Short]]](
    queryStringBindable,
    short,
    "QueryStringBindable",
    "short",
    allTypes -- List("char", "byte", "short"),
    (bindable, failure) => bindable.bind(failure, query),
    shouldBeAnError _
  )

  behavior of "QueryStringBindable for float"

  "QueryStringBindable" should "find a bindable for floats" in {
    float.bind(queryStringBindable)
  }
  it should "read float" in {
    val floatBindable = float.bind(queryStringBindable)

    val found = floatBindable.bind("float", query)
    val expect = Some(Right(12.2f))
    found shouldBe expect
  }
  it should "write float" in {
    val floatBindable = float.bind(queryStringBindable)

    val found = floatBindable.unbind("float", 12.2f)
    val expect = "float=12.2"
    found shouldBe expect
  }
  it should "read the same float than the default float bindable" in {
    val derived = float.bind(queryStringBindable)
    val default = QueryStringBindable.bindableFloat

    val found = derived.bind("float", query)
    val expect = default.bind("float", query)

    found shouldBe expect
  }
  it should "write the same float than the default float bindable" in {
    val derived = float.bind(queryStringBindable)
    val default = QueryStringBindable.bindableFloat

    val found = derived.unbind("float", 12.2f)
    val expect = default.unbind("float", 12.2f)

    found shouldBe expect
  }
  it should "read integer with float bindable" in {
    val floatBindable = float.bind(queryStringBindable)

    val found = floatBindable.bind("int", query)
    val expect = Some(Right(33333))
    found shouldBe expect
  }
  it should "read short with float bindable" in {
    val floatBindable = float.bind(queryStringBindable)

    val found = floatBindable.bind("short", query)
    val expect = Some(Right(23456))
    found shouldBe expect
  }
  it should "read byte with float bindable" in {
    val floatBindable = float.bind(queryStringBindable)

    val found = floatBindable.bind("byte", query)
    val expect = Some(Right(123))
    found shouldBe expect
  }
  it should "read long with float bindable" in {
    val floatBindable = float.bind(queryStringBindable)

    val found = floatBindable.bind("long", query)
    val expect = Some(Right(1267888889999999999l.toFloat))
    found shouldBe expect
  }
  it should "read infinty for large decimal with float bindable" in {
    val floatBindable = float.bind(queryStringBindable)

    val found = floatBindable.bind("double", query)
    val expect = Some(Right(Float.PositiveInfinity))
    found shouldBe expect
  }
  it should "read infinty for bigInt with float bindable" in {
    val floatBindable = float.bind(queryStringBindable)

    val found = floatBindable.bind("bigInt", query)
    val expect = Some(Right(Float.PositiveInfinity))
    found shouldBe expect
  }
  it should "read infinty for bigDecimal with float bindable" in {
    val floatBindable = float.bind(queryStringBindable)

    val found = floatBindable.bind("bigDecimal", query)
    val expect = Some(Right(Float.PositiveInfinity))
    found shouldBe expect
  }
  shouldFail[Float, QueryStringBindable, Option[Either[String, Float]]](
    queryStringBindable,
    float,
    "QueryStringBindable",
    "float",
    allTypes -- List("int", "long", "float", "double", "bigInt", "bigDecimal", "short", "byte"),
    (bindable, failure) => bindable.bind(failure, query),
    shouldBeAnError _
  )

  behavior of "QueryStringBindable for BigDecimal"

  "QueryStringBindable" should "find a bindable for bigDecimal" in {
    bigDecimal.bind(queryStringBindable)
  }
  it should "read bigDecimal" in {
    val bigDecimalBindable = bigDecimal.bind(queryStringBindable)

    val found = bigDecimalBindable.bind("bigDecimal", query)
    val expect = Some(Right(BigDecimal(s"$outOfDouble.1")))
    found shouldBe expect
  }
  it should "write bigDecimal" in {
    val bigDecimalBindable = bigDecimal.bind(queryStringBindable)

    val found = bigDecimalBindable.unbind("bigDecimal", BigDecimal("12678888899999999999999999999988888676997676"))
    val expect = "bigDecimal=12678888899999999999999999999988888676997676"
    found shouldBe expect
  }
  it should "read integer with bigDecimal bindable" in {
    val bigDecimalBindable = bigDecimal.bind(queryStringBindable)

    val found = bigDecimalBindable.bind("int", query)
    val expect = Some(Right(BigDecimal(33333)))
    found shouldBe expect
  }
  it should "read long with bigDecimal bindable" in {
    val bigDecimalBindable = bigDecimal.bind(queryStringBindable)

    val found = bigDecimalBindable.bind("long", query)
    val expect = Some(Right(BigDecimal("1267888889999999999")))
    found shouldBe expect
  }
  it should "read decimal with bigDecimal bindable" in {
    val bigDecimalBindable = bigDecimal.bind(queryStringBindable)

    val found = bigDecimalBindable.bind("double", query)
    val expect = Some(Right(BigDecimal("12678888899999999999999999999988888676997676.2")))
    found shouldBe expect
  }
  it should "read float with bigDecimal bindable" in {
    val bigDecimalBindable = bigDecimal.bind(queryStringBindable)

    val found = bigDecimalBindable.bind("float", query)
    val expect = Some(Right(BigDecimal(12.2)))
    found shouldBe expect
  }
  it should "read bigInt with bigDecimal bindable" in {
    val bigDecimalBindable = bigDecimal.bind(queryStringBindable)

    val found = bigDecimalBindable.bind("bigInt", query)
    val expect = Some(Right(BigDecimal(outOfDouble)))
    found shouldBe expect
  }
  it should "read short with bigDecimal bindable" in {
    val bigDecimalBindable = bigDecimal.bind(queryStringBindable)

    val found = bigDecimalBindable.bind("short", query)
    val expect = Some(Right(BigDecimal("23456")))
    found shouldBe expect
  }
  it should "read byte with bigDecimal bindable" in {
    val bigDecimalBindable = bigDecimal.bind(queryStringBindable)

    val found = bigDecimalBindable.bind("byte", query)
    val expect = Some(Right(BigDecimal("123")))
    found shouldBe expect
  }
  shouldFail[BigDecimal, QueryStringBindable, Option[Either[String, BigDecimal]]](
    queryStringBindable,
    bigDecimal,
    "QueryStringBindable",
    "bigDecimal",
    allTypes -- List("int", "double", "float", "bigInt", "long", "bigDecimal", "short", "byte"),
    (bindable, failure) => bindable.bind(failure, query),
    shouldBeAnError _
  )

  behavior of "QueryStringBindable for Long"

  "QueryStringBindable" should "find a bindable for long" in {
    long.bind(queryStringBindable)
  }
  it should "read long" in {
    val longBindable = long.bind(queryStringBindable)

    val found = longBindable.bind("long", query)
    val expect = Some(Right(1267888889999999999l))
    found shouldBe expect
  }
  it should "write long" in {
    val longBindable = long.bind(queryStringBindable)

    val found = longBindable.unbind("long", 1267888889999999999l)
    val expect = "long=1267888889999999999"
    found shouldBe expect
  }
  it should "read the same long than the default long bindable" in {
    val derived = long.bind(queryStringBindable)
    val default = QueryStringBindable.bindableFloat

    val found = derived.bind("long", query)
    val expect = default.bind("long", query)

    found shouldBe expect
  }
  it should "write the same long than the default long bindable" in {
    val derived = long.bind(queryStringBindable)
    val default = QueryStringBindable.bindableLong

    val found = derived.unbind("long", 1267888889999999999l)
    val expect = default.unbind("long", 1267888889999999999l)

    found shouldBe expect
  }
  it should "read integer with long bindable" in {
    val longBindable = long.bind(queryStringBindable)

    val found = longBindable.bind("int", query)
    val expect = Some(Right(33333))
    found shouldBe expect
  }
  it should "read short with long bindable" in {
    val longBindable = long.bind(queryStringBindable)

    val found = longBindable.bind("short", query)
    val expect = Some(Right(23456))
    found shouldBe expect
  }
  it should "read byte with long bindable" in {
    val longBindable = long.bind(queryStringBindable)

    val found = longBindable.bind("byte", query)
    val expect = Some(Right(123))
    found shouldBe expect
  }
  shouldFail[Long, QueryStringBindable, Option[Either[String, Long]]](
    queryStringBindable,
    long,
    "QueryStringBindable",
    "long",
    allTypes -- List("int", "long", "short", "byte"),
    (bindable, failure) => bindable.bind(failure, query),
    shouldBeAnError _
  )

  behavior of "QueryStringBindable for bigInt"

  "QueryStringBindable" should "find a bindable for bigInt" in {
    bigInt.bind(queryStringBindable)
  }
  it should "read bigInt" in {
    val bigIntBindable = bigInt.bind(queryStringBindable)

    val found = bigIntBindable.bind("bigInt", query)
    val expect = Some(Right(BigInt(outOfDouble)))
    found shouldBe expect
  }
  it should "write bigInt" in {
    val bigIntBindable = bigInt.bind(queryStringBindable)

    val found = bigIntBindable.unbind("bigInt", BigInt("12678888899999999999999999999988888676997676"))
    val expect = "bigInt=12678888899999999999999999999988888676997676"
    found shouldBe expect
  }
  it should "read integer with bigInt bindable" in {
    val bigIntBindable = bigInt.bind(queryStringBindable)

    val found = bigIntBindable.bind("int", query)
    val expect = Some(Right(BigInt(33333)))
    found shouldBe expect
  }
  it should "read short with bigInt bindable" in {
    val bigIntBindable = bigInt.bind(queryStringBindable)

    val found = bigIntBindable.bind("short", query)
    val expect = Some(Right(BigInt(23456)))
    found shouldBe expect
  }
  it should "read long with bigInt bindable" in {
    val bigIntBindable = bigInt.bind(queryStringBindable)

    val found = bigIntBindable.bind("long", query)
    val expect = Some(Right(BigInt("1267888889999999999")))
    found shouldBe expect
  }
  it should "read byte with bigInt bindable" in {
    val bigIntBindable = bigInt.bind(queryStringBindable)

    val found = bigIntBindable.bind("byte", query)
    val expect = Some(Right(BigInt("123")))
    found shouldBe expect
  }
  shouldFail[BigInt, QueryStringBindable, Option[Either[String, BigInt]]](
    queryStringBindable,
    bigInt,
    "QueryStringBindable",
    "bigInt",
    allTypes -- List("int", "long", "bigInt", "short", "byte"),
    (bindable, failure) => bindable.bind(failure, query),
    shouldBeAnError _
  )

  behavior of "QueryStringBindable for dateTime"

  "QueryStringBindable" should "find a bindable for dateTime" in {
    dateTime.bind(queryStringBindable)
  }
  it should "read dateTime" in {
    val dateTimeBindable = dateTime.bind(queryStringBindable)

    val found = dateTimeBindable.bind("dateTime", query)
    val expect = Some(Right(ZonedDateTime.parse("2018-12-26T00:08:16.025415+01:00[Europe/Paris]")))
    found shouldBe expect
  }
  it should "write dateTime" in {
    val dateTimeBindable = dateTime.bind(queryStringBindable)

    val found = dateTimeBindable.unbind("dateTime", ZonedDateTime.parse("2018-12-26T00:08:16.025415+01:00[Europe/Paris]"))
    val expect = "dateTime=" + URLEncoder.encode("2018-12-26T00:08:16.025415+01:00[Europe/Paris]", "UTF-8")
    found shouldBe expect
  }
  shouldFail[ZonedDateTime, QueryStringBindable, Option[Either[String, ZonedDateTime]]](
    queryStringBindable,
    dateTime,
    "QueryStringBindable",
    "dateTime",
    allTypes -- List("dateTime"),
    (bindable, failure) => bindable.bind(failure, query),
    shouldBeAnError _
  )

  behavior of "QueryStringBindable for time"

  "QueryStringBindable" should "find a bindable for time" in {
    time.bind(queryStringBindable)
  }
  it should "read time" in {
    val timeBindable = time.bind(queryStringBindable)

    val found = timeBindable.bind("time", query)
    val expect = Some(Right(LocalTime.parse("00:08:16.025415")))
    found shouldBe expect
  }
  it should "write time" in {
    val timeBindable = time.bind(queryStringBindable)

    val found = timeBindable.unbind("time", LocalTime.parse("00:08:16.025415"))
    val expect = "time=" + URLEncoder.encode("00:08:16.025415", "UTF-8")
    found shouldBe expect
  }
  shouldFail[LocalTime, QueryStringBindable, Option[Either[String, LocalTime]]](
    queryStringBindable,
    time,
    "QueryStringBindable",
    "time",
    allTypes -- List("time"),
    (bindable, failure) => bindable.bind(failure, query),
    shouldBeAnError _
  )

  behavior of "QueryStringBindable for date"

  it should "read date" in {
    val dateBindable = date.bind(queryStringBindable)

    val found = dateBindable.bind("date", query)
    val expect = Some(Right(LocalDate.parse("2018-12-26")))
    found shouldBe expect
  }
  it should "write date" in {
    val dateBindable = date.bind(queryStringBindable)

    val found = dateBindable.unbind("date", LocalDate.parse("2018-12-26"))
    val expect = "date=2018-12-26"
    found shouldBe expect
  }
  shouldFail[LocalDate, QueryStringBindable, Option[Either[String, LocalDate]]](
    queryStringBindable,
    date,
    "QueryStringBindable",
    "date",
    allTypes -- List("date"),
    (bindable, failure) => bindable.bind(failure, query),
    shouldBeAnError _
  )
}
