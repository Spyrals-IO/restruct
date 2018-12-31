package restruct.examples

import io.github.methrat0n.restruct.readers.bson.bsonReader
import io.github.methrat0n.restruct.readers.json.jsonReads
import io.github.methrat0n.restruct.schema.{ Schema, StrictSchema }
import play.api.libs.json.Json
import reactivemongo.bson.BSONDocument

object SyntaxExample extends App {

  val goodJson =
    """
      |{
      |  "lastName": "goulet",
      |  "firstName": "merlin"
      |}
    """.stripMargin

  val badJson =
    """
      |{
      |  "amount": { "amount": 11.1 }
      |}
    """.stripMargin

  val goodUserJson =
    """
      |{
      |  "name": "merlin",
      |  "age": 24,
      |
      |  "__type": "Badser"
      |}
    """.stripMargin

  User.autoSchema.bind(jsonReads).reads(Json.parse(goodUserJson)) match {
    case play.api.libs.json.JsSuccess(value, _) => println(value)
    case play.api.libs.json.JsError(errors)     => println(errors)
  }

  import io.github.methrat0n.restruct.schema.Syntax._
  bigInt.bind(jsonReads).reads(Json.parse("111")) match {
    case play.api.libs.json.JsSuccess(value, _) => println(value)
    case play.api.libs.json.JsError(errors)     => println(errors)
  }

  val bsonGoodUser = BSONDocument(
    "name" -> "merlin",
    "age" -> 24,
    "__type" -> "BadUser"
  )

  User.autoSchema.bind(bsonReader).read(bsonGoodUser)

}

sealed trait User

final case class GoodUser(name: String, age: Int) extends User
final case class BadUser(name: String, age: Int) extends User

object User {
  import io.github.methrat0n.restruct.schema.Syntax._
  implicit val goodUserSchema: Schema[GoodUser] =
    "name".as[String] and
      "age".as[Int]

  implicit val badUserSchema: Schema[BadUser] =
    "name".as(string) and
      "age".as(integer)

  val schema: Schema[User] = StrictSchema(
    goodUserSchema or
      badUserSchema
  )

  val goodUserAutoSchema = Schema.of[GoodUser]

  val autoSchema = StrictSchema.of[User]
}
