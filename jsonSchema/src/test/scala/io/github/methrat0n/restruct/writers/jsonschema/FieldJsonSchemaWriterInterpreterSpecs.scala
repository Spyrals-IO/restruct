package io.github.methrat0n.restruct.writers.jsonschema

import io.github.methrat0n.restruct.schema.Schema
import org.scalatest.{FlatSpec, Matchers}
import play.api.libs.json._

class FieldJsonWriterInterpreterSpecs extends FlatSpec with Matchers {

  private val requiredString = "string".as[String]
  private val complexRequiredString = ("level one" \ "level two").as[String]
  private val optionalString = "string".asOption[String]

  behavior of "JsonSchema for fields"

  it should "find a jsonSchema for a required string" in {
    val requiredStringJsonSchema = requiredString.bind(jsonSchema)

    val found = requiredStringJsonSchema.json
    val expect = Json.obj(
      "type" -> "object",
      "properties" -> Json.obj(
        "string" -> Json.obj(
          "type" -> "string"
        )
      ),
      "required" -> Json.arr("string")
    )
    found shouldBe expect
  }
  it should "find a jsonSchema for a second level of object if it's describe in the path" in {
    val complexRequiredStringJsonSchema = complexRequiredString.bind(jsonSchema)

    val found = complexRequiredStringJsonSchema.json
    val expect = Json.obj(
      "type" -> "object",
      "properties" -> Json.obj(
        "level one" -> Json.obj(
          "type" -> "object",
          "properties" -> Json.obj(
            "level two" -> Json.obj(
              "type" -> "string"
            )
          ),
          "required" -> Json.arr("level two")
        )
      ),
      "required" -> Json.arr("level one")
    )
    found shouldBe expect
  }

  it should "find a jsonSchema for optional string" in {
    val optionalStringWriter = optionalString.bind(jsonSchema)

    val found = optionalStringWriter.json
    val expect = Json.obj(
      "type" -> "object",
      "properties" -> Json.obj(
        "string" -> Json.obj(
          "type" -> "string"
        )
      ),
      "required" -> JsArray.empty
    )
    found shouldBe expect
  }

  it should "find a jsonSchema for case class instances" in {
    val caseClassWriter = RequiredStringAndInt.schema.bind(jsonSchema)

    val found = caseClassWriter.json
    val expect = Json.obj(
      "type" -> "object",
      "properties" -> Json.obj(
        "string" -> Json.obj(
          "type" -> "string",
          ),
          "int" -> Json.obj(
            "type" -> "integer"
            )
          ),
      "required" -> Json.arr("string", "int")
    )

    found shouldBe expect
  }
}

final case class RequiredStringAndInt(string: String, int: Int)

object RequiredStringAndInt {
  val schema = Schema.of[RequiredStringAndInt]
}
