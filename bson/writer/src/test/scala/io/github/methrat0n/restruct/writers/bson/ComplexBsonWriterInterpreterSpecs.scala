package io.github.methrat0n.restruct.writers.bson

import org.scalatest.{ FlatSpec, Matchers }
import reactivemongo.bson._

class ComplexBsonWriterInterpreterSpecs extends FlatSpec with Matchers {

  private val emptyList = List.empty
  private val stringList = List("string", "string")
  private val intList = List(0, 1)

  behavior of "BsonWriter for empty list"

  it should "write an empty list to an empty BSONArray" in {
    val emptyStringListWriter = list(string).bind(bsonWriter)

    val found = emptyStringListWriter.write(emptyList)
    val expect = BSONArray.empty
    found shouldBe expect
  }
  it should "write a string list to a BSONArray of BSONString" in {
    val stringListWriter = list(string).bind(bsonWriter)

    val found = stringListWriter.write(stringList)
    val expect = BSONArray(List(BSONString("string"), BSONString("string")))
    found shouldBe expect
  }
  it should "write an int list  to a BSONArray of BSONInteger" in {
    val intListWriter = list(integer).bind(bsonWriter)

    val found = intListWriter.write(intList)
    val expect = BSONArray(List(BSONInteger(0), BSONInteger(1)))
    found shouldBe expect
  }
}
