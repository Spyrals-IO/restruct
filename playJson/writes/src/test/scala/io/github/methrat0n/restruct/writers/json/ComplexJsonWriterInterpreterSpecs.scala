package io.github.methrat0n.restruct.writers.json

import org.scalatest.{ FlatSpec, Matchers }
import play.api.libs.json._

class ComplexJsonWriterInterpreterSpecs extends FlatSpec with Matchers {
  private val emptyList = List.empty
  private val stringList = List("string", "string")
  private val intList = List(0, 1)

  behavior of "JsonWriterInterpreter for empty list"

  it should "write an empty list to an empty JsArray" in {
    val emptyStringListWriter = list(string).bind(jsonWrites)

    val found = emptyStringListWriter.writes(emptyList)
    val expect = JsArray()
    found shouldBe expect
  }
  it should "write a string list to a JsArray of JsString" in {
    val stringListWriter = list(string).bind(jsonWrites)

    val found = stringListWriter.writes(stringList)
    val expect = JsArray(List(JsString("string"), JsString("string")))
    found shouldBe expect
  }
  it should "write an int list  to a JsArray of JsNumber" in {
    val intListWriter = list(integer).bind(jsonWrites)

    val found = intListWriter.writes(intList)
    val expect = JsArray(List(JsNumber(0), JsNumber(1)))
    found shouldBe expect
  }
}
