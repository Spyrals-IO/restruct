package io.github.methrat0n.restruct.readers.bson

import org.scalatest.{ FlatSpec, Matchers }
import reactivemongo.bson.BSONArray

class ComplexBsonReaderInterpreterSpecs extends FlatSpec with Matchers {
  private val emptyList = BSONArray.empty
  private val stringList = BSONArray("string", "string")
  private val intList = BSONArray(0, 1)

  behavior of "BSONReaderInterpreter for empty list"

  it should "read an empty list from an empty BSONArray" in {
    val emptyStringListReader = list(string).bind(bsonReader)

    val found = emptyStringListReader.read(emptyList)
    val expect = List.empty
    found shouldBe expect
  }
  it should "read a string list from a BSONArray of String" in {
    val stringListReader = list(string).bind(bsonReader)

    val found = stringListReader.read(stringList)
    val expect = List("string", "string")
    found shouldBe expect
  }
  it should "read an int list  from a BSONArray of Int" in {
    val intListReader = list(integer).bind(bsonReader)

    val found = intListReader.read(intList)
    val expect = List(0, 1)
    found shouldBe expect
  }
}
