package restruct.examples

//import io.github.methrat0n.restruct.readers.bson.BsonReader
import io.github.methrat0n.restruct.schema.Path.\
import io.github.methrat0n.restruct.schema.{ Path, PathNil }
import play.api.libs.json.{ Json, Reads }
//import reactivemongo.bson.BSONDocument

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

  import io.github.methrat0n.restruct.readers.json._

  /*User.autoSchema.bind[Reads].reads(Json.parse(goodUserJson)) match {
    case play.api.libs.json.JsSuccess(value, _) => println(value)
    case play.api.libs.json.JsError(errors)     => println(errors)
  }*/

  implicitly[PathBuilder[PathNil]](PathBuilder.emptyStep2JsPath)
  implicitly[PathBuilder[PathNil \ Int]]
  implicitly[PathBuilder[PathNil \ Int \ String]]
  (Path \ 0 \ "dt").as[BigInt].bind[Reads].reads(Json.parse("111")) match {
    case play.api.libs.json.JsSuccess(value, _) => println(value)
    case play.api.libs.json.JsError(errors)     => println(errors)
  }

  /*val bsonGoodUser = BSONDocument(
    "name" -> "merlin",
    "age" -> 24,
    "__type" -> "BadUser"
  )

  User.autoSchema.bind[BsonReader].read(bsonGoodUser)*/
}

sealed trait User

final case class GoodUser(name: String, age: Int) extends User
final case class BadUser(name: String, age: Int) extends User

object User {
  implicit val goodUserSchema =
    (Path \ "name").as[String] and
      (Path \ "age").as[Int]

  implicit val badUserSchema =
    (Path \ "name").as[String] and
      (Path \ "age").as[Int]

  //val goodUserAutoSchema = Schema.of[GoodUser]

  //val autoSchema = StrictSchema.of[User]
}
