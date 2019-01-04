package io.github.methrat0n.restruct.writers.json

import io.github.methrat0n.restruct.schema.Schema
import org.scalatest.{ FlatSpec, Matchers }
import io.github.methrat0n.restruct.schema.Syntax._
import play.api.libs.json._

class FieldJsonWriterInterpreterSpecs extends FlatSpec with Matchers {

  private val requiredString = "string".as[String]
  private val complexRequiredString = ("level one" \ "level two").as[String]
  //private val complexRequiredStringWithIndex = ("string" \ 0).as[String]
  private val optionalString = "string".asOption[String]

  behavior of "PlayJson Writes fields"

  it should "write an object to contains a required string" in {
    val requiredStringWriter = requiredString.bind(jsonWrites)

    val found = requiredStringWriter.writes("a string")
    val expect = Json.obj(
      "string" -> "a string"
    )
    found shouldBe expect
  }
  it should "write a second level of object if it's describe in the path" in {
    val complexRequiredStringWriter = complexRequiredString.bind(jsonWrites)

    val found = complexRequiredStringWriter.writes("a string")
    val expect = Json.obj(
      "level one" -> Json.obj(
        "level two" -> "a string"
      )
    )
    found shouldBe expect
  }
  //TODO an issue will be open in play-json
  /*it should "write an array if it's describe in the path" in {
    val complexRequiredStringWithIndexWriter = complexRequiredStringWithIndex.bind(jsonWrites)

    val found = complexRequiredStringWithIndexWriter.writes("a string")
    val expect = Json.obj(
      "string" -> Json.arr(Json.obj(
        "test" -> "a string"
      ))
    )
    found shouldBe expect
  }*/

  it should "write optional string if present" in {
    val optionalStringWriter = optionalString.bind(jsonWrites)

    val found = optionalStringWriter.writes(Some("string"))
    val expect = Json.obj(
      "string" -> "string"
    )
    found shouldBe expect
  }
  it should "write optional string if abscent" in {
    val optionalStringWriter = optionalString.bind(jsonWrites)

    val found = optionalStringWriter.writes(None)
    val expect = Json.obj()
    found shouldBe expect
  }

  it should "write case class instances" in {
    val caseClassWriter = RequiredStringAndInt.schema.bind(jsonWrites)

    val found = caseClassWriter.writes(RequiredStringAndInt("string", 0))
    val expect = Json.obj(
      "string" -> "string",
      "int" -> 0
    )
    found shouldBe expect
  }
}

final case class RequiredStringAndInt(string: String, int: Int)
object RequiredStringAndInt {
  val schema = Schema.of[RequiredStringAndInt]
}
