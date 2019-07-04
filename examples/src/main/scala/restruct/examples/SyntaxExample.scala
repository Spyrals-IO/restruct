package restruct.examples

import io.github.methrat0n.restruct.schema.Path
import play.api.libs.json.{ Json, Reads, Writes }

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
  GoodUser.schema.bind[Reads].reads(Json.parse(goodUserJson)) match {
    case play.api.libs.json.JsSuccess(value, _) => println(value)
    case play.api.libs.json.JsError(errors)     => println(errors)
  }

  import io.github.methrat0n.restruct.writers.json._
  val user = GoodUser("charlaine", 32)
  println(Json.stringify(GoodUser.schema.bind[Writes].writes(user)))

  /*(Path \ 0 \ "dt").as[BigInt].bind[Reads, SimpleInterpreter[?[_], BigInt]].reads(Json.parse("111")) match {
    case play.api.libs.json.JsSuccess(value, _) => println(value)
    case play.api.libs.json.JsError(errors)     => println(errors)
  }*/

  //import io.github.methrat0n.restruct.readers.json._

  //WrappedUser.schema.bind[Reads]
  //GoodUser.schema.bind[Reads, SemiGroupalInterpreter[?[_], String, Int]]

  //User.schema.bind[Reads, OneOfInterpreter[?[_], (String, Int), (String, Int)]]
  /*val bsonGoodUser = BSONDocument(
    "name" -> "merlin",
    "age" -> 24,
    "__type" -> "BadUser"
  )

  User.autoSchema.bind[BsonReader].read(bsonGoodUser)*/
}

sealed trait User extends Product with Serializable

final case class GoodUser(name: String, age: Int) extends User

import scala.language.postfixOps

object GoodUser {

  implicit val schema = (
    (Path \ "name").as[String]() and
    (Path \ "age").as[Int]()
  ).inmap(GoodUser.apply _ tupled)(GoodUser.unapply _ andThen (_.get))
}

final case class BadUser(name: String, age: Int) extends User

object BadUser {
  implicit val schema =
    ((Path \ "name").as[String]() and
      (Path \ "age").as[Int]()).inmap(BadUser.apply _ tupled)(BadUser.unapply _ andThen (_.get))
}

final case class WrappedUser(user: GoodUser, text: String)

object WrappedUser {
  implicit val schema = (
    (Path \ "user").as[GoodUser]() and
    (Path \ "text").as[String]()
  ).inmap[WrappedUser](WrappedUser.apply _ tupled)(WrappedUser.unapply _ andThen (_.get))
}

object User {

  val schema = (GoodUser.schema or BadUser.schema).inmap {
    case Right(badUser) => badUser
    case Left(goodUser) => goodUser
  } {
    case badUser: BadUser   => Right(badUser)
    case goodUser: GoodUser => Left(goodUser)
  }

  //val goodUserAutoSchema = Schema.of[GoodUser]

  //val autoSchema = StrictSchema.of[User]
}
