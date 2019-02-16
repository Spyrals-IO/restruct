package io.github.methrat0n.restruct.writers.bson

import bsonWriter
import io.github.methrat0n.restruct.schema.Schema
import io.github.methrat0n.restruct.schema.Syntax._
import org.scalatest.{FlatSpec, Matchers}
import reactivemongo.bson.BSONDocument

class FieldBsonWriterInterpreterSpecs extends FlatSpec with Matchers {

  private val requiredString = "string".as[String]
  private val complexRequiredString = ("level one" \ "level two").as[String]
  //private val complexRequiredStringWithIndex = ("string" \ 0).as[String]
  private val optionalString = "string".asOption[String]

  behavior of "BsonWriter Writes fields"

  it should "write an object to contains a required string" in {
    val requiredStringWriter = requiredString.bind(bsonWriter)

    val found = requiredStringWriter.write("a string")
    val expect = BSONDocument(
      "string" -> "a string"
    )
    found shouldBe expect
  }
  it should "write a second level of object if it's describe in the path" in {
    val complexRequiredStringWriter = complexRequiredString.bind(bsonWriter)

    val found = complexRequiredStringWriter.write("a string")
    val expect = BSONDocument(
      "level one" -> BSONDocument(
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
    val optionalStringWriter = optionalString.bind(bsonWriter)

    val found = optionalStringWriter.write(Some("string"))
    val expect = BSONDocument(
      "string" -> "string"
    )
    found shouldBe expect
  }
  it should "write optional string if abscent" in {
    val optionalStringWriter = optionalString.bind(bsonWriter)

    val found = optionalStringWriter.write(None)
    val expect = BSONDocument.empty
    found shouldBe expect
  }

  it should "write case class instances" in {
    val caseClassWriter = RequiredStringAndInt.schema.bind(bsonWriter)

    val found = caseClassWriter.write(RequiredStringAndInt("string", 0))
    val expect = BSONDocument(
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
