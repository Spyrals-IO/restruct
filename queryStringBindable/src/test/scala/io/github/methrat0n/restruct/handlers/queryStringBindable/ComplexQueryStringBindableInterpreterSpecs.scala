package io.github.methrat0n.restruct.handlers.queryStringBindable

import org.scalatest.{ FlatSpec, Matchers }

import io.github.methrat0n.restruct.schema.Syntax._

class ComplexQueryStringBindableInterpreterSpecs extends FlatSpec with Matchers {
  private val query = Map(
    "emptyList" -> Seq(),
    "stringList" -> Seq("string", "string"),
    "intList" -> Seq("1", "0")
  )

  behavior of "QueryStringBindable for empty list"

  it should "read an empty List when key is not present" in {
    val emptyStringListBindable = list(string).bind(queryStringBindable)

    val found = emptyStringListBindable.bind("empty", query)
    val expect = Some(Right(List()))
    found shouldBe expect
  }
  it should "read an empty List" in {
    val emptyStringListBindable = list(string).bind(queryStringBindable)

    val found = emptyStringListBindable.bind("emptyList", query)
    val expect = Some(Right(List()))
    found shouldBe expect
  }
  it should "read a string List" in {
    val stringListBindable = list(string).bind(queryStringBindable)

    val found = stringListBindable.bind("stringList", query)
    val expect = Some(Right(List("string", "string")))
    found shouldBe expect
  }
  it should "read an int List" in {
    val intListBindable = list(integer).bind(queryStringBindable)

    val found = intListBindable.bind("intList", query)
    val expect = Some(Right(List(1, 0)))
    found shouldBe expect
  }
}
