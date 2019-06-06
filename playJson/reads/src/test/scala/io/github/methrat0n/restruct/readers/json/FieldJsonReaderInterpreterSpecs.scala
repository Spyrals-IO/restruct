package io.github.methrat0n.restruct.readers.json

import io.github.methrat0n.restruct.schema.Schema
import org.scalatest.{ FlatSpec, Matchers }
import play.api.libs.json._

class FieldJsonReaderInterpreterSpecs extends FlatSpec with Matchers {

  private val requiredString = "string".as[String]
  private val complexRequiredString = ("level one" \ "level two").as[String]
  private val complexRequiredStringWithIndex = ("string" \ 0).as[String]
  private val optionalString = "string".asOption[String]

  private val stringTest = Json.obj(
    "string" -> "a string"
  )
  private val deepStringTest = Json.obj(
    "level one" -> Json.obj(
      "level two" -> "a string"
    )
  )
  private val deepIndexedStringTest = Json.obj(
    "string" -> Json.arr(
      "a string"
    )
  )
  private val instanceTest = Json.obj(
    "string" -> "string",
    "int" -> 0
  )

  behavior of "PlayJson Reads fields"

  it should "read an object which contains a required string" in {
    val requiredStringReader = requiredString.bind(jsonReads)

    val found = requiredStringReader.reads(stringTest)
    val expect = JsSuccess("a string", JsPath \ "string")
    found shouldBe expect
  }
  it should "read a second level of object if it's describe in the path" in {
    val complexRequiredStringReader = complexRequiredString.bind(jsonReads)

    val found = complexRequiredStringReader.reads(deepStringTest)
    val expect = JsSuccess("a string", JsPath \ "level one" \ "level two")
    found shouldBe expect
  }
  it should "read an array if it's describe in the path" in {
    val complexRequiredStringWithIndexReader = complexRequiredStringWithIndex.bind(jsonReads)

    val found = complexRequiredStringWithIndexReader.reads(deepIndexedStringTest)
    val expect = JsSuccess("a string", JsPath \ "string" \ 0)
    found shouldBe expect
  }
  it should "read optional string if present" in {
    val optionalStringReader = optionalString.bind(jsonReads)

    val found = optionalStringReader.reads(stringTest)
    val expect = JsSuccess(Some("a string"), JsPath \ "string")
    found shouldBe expect
  }
  it should "read optional string if abscent" in {
    val optionalStringReader = optionalString.bind(jsonReads)

    val found = optionalStringReader.reads(JsObject.empty)
    val expect = JsSuccess(None)
    found shouldBe expect
  }
  it should "read case class instances" in {
    val caseClassReader = RequiredStringAndInt.schema.bind(jsonReads)

    val found = caseClassReader.reads(instanceTest)
    val expect = JsSuccess(RequiredStringAndInt("string", 0))
    found shouldBe expect
  }
}

final case class RequiredStringAndInt(string: String, int: Int)
object RequiredStringAndInt {
  val schema = Schema.of[RequiredStringAndInt]
}
