package io.github.methrat0n.restruct.readers.bson

import io.github.methrat0n.restruct.schema.Schema
import io.github.methrat0n.restruct.schema.Syntax._
import org.scalatest.{ FlatSpec, Matchers }
import reactivemongo.bson.{ BSONArray, BSONDocument }

class FieldJsonReaderInterpreterSpecs extends FlatSpec with Matchers {

  private val requiredString = "string".as[String]
  private val complexRequiredString = ("level one" \ "level two").as[String]
  private val complexRequiredStringWithIndex = ("string" \ 0).as[String]
  private val optionalString = "string".asOption[String]

  private val stringTest = BSONDocument(
    "string" -> "a string"
  )
  private val deepStringTest = BSONDocument(
    "level one" -> BSONDocument(
      "level two" -> "a string"
    )
  )
  private val deepIndexedStringTest = BSONDocument(
    "string" -> BSONArray(
      "a string"
    )
  )
  private val instanceTest = BSONDocument(
    "string" -> "string",
    "int" -> 0
  )

  behavior of "BSONReaderInterpreter for fields"

  it should "read an object which contains a required string" in {
    val requiredStringReader = requiredString.bind(bsonReader)

    val found = requiredStringReader.read(stringTest)
    val expect = "a string"
    found shouldBe expect
  }
  it should "read a second level of object if it's describe in the path" in {
    val complexRequiredStringReader = complexRequiredString.bind(bsonReader)

    val found = complexRequiredStringReader.read(deepStringTest)
    val expect = "a string"
    found shouldBe expect
  }
  it should "read an array if it's describe in the path" in {
    val complexRequiredStringWithIndexReader = complexRequiredStringWithIndex.bind(bsonReader)

    val found = complexRequiredStringWithIndexReader.read(deepIndexedStringTest)
    val expect = "a string"
    found shouldBe expect
  }
  it should "read optional string if present" in {
    val optionalStringReader = optionalString.bind(bsonReader)

    val found = optionalStringReader.read(stringTest)
    val expect = Some("a string")
    found shouldBe expect
  }
  it should "read optional string if abscent" in {
    val optionalStringReader = optionalString.bind(bsonReader)

    val found = optionalStringReader.read(BSONDocument.empty)
    val expect = None
    found shouldBe expect
  }
  it should "read case class instances" in {
    val caseClassReader = RequiredStringAndInt.schema.bind(bsonReader)

    val found = caseClassReader.read(instanceTest)
    val expect = RequiredStringAndInt("string", 0)
    found shouldBe expect
  }
}

final case class RequiredStringAndInt(string: String, int: Int)

object RequiredStringAndInt {
  val schema = Schema.of[RequiredStringAndInt]
}
