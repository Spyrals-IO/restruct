package io.github.methrat0n.restruct.handlers.queryStringBindable

import io.github.methrat0n.restruct.schema.Schema
import org.scalatest.{FlatSpec, Matchers}
import io.github.methrat0n.restruct.schema.Syntax._

class FieldQueryStringBindableInterpreterSpecs extends FlatSpec with Matchers {
  private val query = Map(
    "string" -> Seq("string"),
    "int" -> Seq("0"),
  )

  private val requiredString = "string".as[String]
  private val requiredStr = "str".as[String]
  private val complexRequiredString = ("string" \ 0).as[String]
  private val optionalString = "string".asOption[String]
  private val optionalStr = "str".asOption[String]

  behavior of "QueryStringBindable fields"

  it should "read required string without any change" in {
    val requiredStringBindable = requiredString.bind(queryStringBindable)

    val found = requiredStringBindable.bind("", query)
    val expect = Some(Right("string"))
    found shouldBe expect
  }
  it should "write required string without any change" in {
    val requiredStringBindable = requiredString.bind(queryStringBindable)

    val found = requiredStringBindable.unbind("", "string")
    val expect = "string=string"
    found shouldBe expect
  }
  it should "complain when required string is missing" in {
    val requiredStringBindable = requiredStr.bind(queryStringBindable)

    val found = requiredStringBindable.bind("", query)
    val expect = None
    found shouldBe expect
  }
  it should "complain when path is more than just a string" in {
    val complexRequiredStringBindable = complexRequiredString.bind(queryStringBindable)

    a[RuntimeException] shouldBe thrownBy(complexRequiredStringBindable.bind("", query))
  }
  it should "read optional string" in {
    val optionalStringBindable = optionalString.bind(queryStringBindable)

    val found = optionalStringBindable.bind("", query)
    val expect = Some(Right(Some("string")))
    found shouldBe expect
  }
  it should "read None when optional value is missing" in {
    val optionalStringBindable = optionalStr.bind(queryStringBindable)

    val found = optionalStringBindable.bind("", query)
    val expect = Some(Right(None))
    found shouldBe expect
  }
  it should "read object from query string" in {
    val requiredObjectBindable = RequiredStringAndInt.schema.bind(queryStringBindable)

    val found = requiredObjectBindable.bind("", query)
    val expect = Some(Right(RequiredStringAndInt("string", 0)))
    found shouldBe expect
  }
  it should "read object with optional fields from query string" in {
    val requiredObjectBindable = StringAndMaybeInt.schema.bind(queryStringBindable)

    val found = requiredObjectBindable.bind("", query)
    val expect = Some(Right(StringAndMaybeInt("string", Some(0))))
    found shouldBe expect
  }
  it should "read None for missing optional fields from query string" in {
    val requiredObjectBindable = StringAndMaybeIn.schema.bind(queryStringBindable)

    val found = requiredObjectBindable.bind("", query)
    val expect = Some(Right(StringAndMaybeIn("string", None)))
    found shouldBe expect
  }
}

final case class RequiredStringAndInt(string: String, int: Int)
object RequiredStringAndInt {
  val schema = Schema.of[RequiredStringAndInt]
}

final case class StringAndMaybeInt(string: String, int: Option[Int])
object StringAndMaybeInt {
  val schema = Schema.of[StringAndMaybeInt]
}

final case class StringAndMaybeIn(string: String, in: Option[Int])
object StringAndMaybeIn {
  val schema = Schema.of[StringAndMaybeIn]
}
