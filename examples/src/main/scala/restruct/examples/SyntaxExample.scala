package restruct.examples

import play.api.libs.json.{ Json, Reads }
import io.github.methrat0n.restruct.schema.{ Schema, StrictSchema }
import io.github.methrat0n.restruct.readers.json.JsonReaderInterpreter
import io.github.methrat0n.schema.Schemas

object SyntaxExample extends App {

  final case class Person(
    lastName: String,
    firstName: Option[String]
  )

  object Person {
    import io.github.methrat0n.restruct.schema.Syntax._
    implicit lazy val schema: Schema[Person] = Schema(
      ("names" \ "lastName").as(string).defaultTo("merlin"),
      ("names" \ "firstName").asOption(string)
    )
    implicit lazy val autoSchema: Schema[Person] = Schemas.of[Person]
    val jsonReader = JsonReaderInterpreter
    lazy val reads: Reads[Person] = schema.bind(jsonReader)
  }

  val goodJson =
    """
      |{ "names": {
      |  "lastName": "goulet",
      |  "firstName": "merlin"
      |  }
      |}
    """.stripMargin

  val badJson =
    """
      |{
      |  "amount": { "amount": 11.1 }
      |}
    """.stripMargin

  Person.reads.reads(Json.parse(goodJson)) match {
    case play.api.libs.json.JsSuccess(value, _) => println(value)
    case play.api.libs.json.JsError(errors)     => println(errors)
  }

  import io.github.methrat0n.restruct.schema.Syntax._
  bigInt.bind(JsonReaderInterpreter).reads(Json.parse("111")) match {
    case play.api.libs.json.JsSuccess(value, _) => println(value)
    case play.api.libs.json.JsError(errors)     => println(errors)
  }

  val goodUserJson =
    """
      |{
      |  "name": "merlin",
      |  "age": 24,
      |  "__type": "GoodUser"
      |}
    """.stripMargin

  User.strictSchema.bind(JsonReaderInterpreter).reads(Json.parse(goodUserJson)) match {
    case play.api.libs.json.JsSuccess(value, _) => println(value)
    case play.api.libs.json.JsError(errors)     => println(errors)
  }

}

sealed trait User

final case class GoodUser(name: String, age: Int) extends User
final case class BadUser(name: String, age: Int) extends User

object User {
  import io.github.methrat0n.restruct.schema.Syntax._
  val goodUserSchema: Schema[GoodUser] = Schema(
    "name".as(string),
    "age".as(integer)
  )
  val badUserSchema: Schema[BadUser] = Schema(
    "name".as(string),
    "age".as(integer)
  )
  val schema: Schema[User] = Schema(
    goodUserSchema,
    badUserSchema
  )
  val strictSchema: Schema[User] = StrictSchema(
    goodUserSchema,
    badUserSchema
  )
}
