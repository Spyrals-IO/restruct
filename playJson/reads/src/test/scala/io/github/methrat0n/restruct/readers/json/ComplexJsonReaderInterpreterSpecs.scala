package io.github.methrat0n.restruct.readers.json

import io.github.methrat0n.restruct.schema.Syntax._
import org.scalatest.{ FlatSpec, Matchers }
import play.api.libs.json._

class ComplexJsonReaderInterpreterSpecs extends FlatSpec with Matchers {
  private val emptyList = JsArray.empty
  private val stringList = Json.arr("string", "string")
  private val intList = Json.arr(0, 1)

  behavior of "JsonReaderInterpreter for empty list"

  it should "read an empty list from an empty JsArray" in {
    val emptyStringListReader = list(string).bind(jsonReads)

    val found = emptyStringListReader.reads(emptyList)
    val expect = JsSuccess(List.empty)
    found shouldBe expect
  }
  it should "read a string list from a JsArray of JsString" in {
    val stringListReader = list(string).bind(jsonReads)

    val found = stringListReader.reads(stringList)
    val expect = JsSuccess(List("string", "string"))
    found shouldBe expect
  }
  it should "read an int list  from a JsArray of JsNumber" in {
    val intListReader = list(integer).bind(jsonReads)

    val found = intListReader.reads(intList)
    val expect = JsSuccess(List(0, 1))
    found shouldBe expect
  }
}
