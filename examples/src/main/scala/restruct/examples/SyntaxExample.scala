package restruct.examples

import io.github.methrat0n.restruct.schema.{ Path, Schema }
import play.api.libs.json.{ Format, JsSuccess, Json, Reads, Writes }

object SyntaxExample extends App {

  //    val nameSchema = (Path \ "name").as[String]()
  //    val nameWrites: Writes[String] = nameSchema.bind[Writes]

  val name = "Methrat0n"
  //  nameWrites.writes(name)

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
      |  "name": "meth",
      |  "age": 24,
      |
      |  "__type": "BadUser"
      |}
    """.stripMargin

  import io.github.methrat0n.restruct.handlers.json._
  GoodUser.schema.bind[Format].reads(Json.parse(goodUserJson)) match {
    case play.api.libs.json.JsSuccess(value, _) => println(value)
    case play.api.libs.json.JsError(errors)     => println(errors)
  }

  import io.github.methrat0n.restruct.writers.json._
  val user = GoodUser("charlaine", 32)
  println(Json.stringify(GoodUser.schema.bind[Writes].writes(user)))

  import io.github.methrat0n.restruct.readers.json._
  println(Json.parse(s"""{"user": [$goodUserJson, $goodUserJson, $goodUserJson], "text":"qqzdd"}""").as[WrappedUser])
  BadUser.schema.bind[Reads].reads(Json.parse(goodUserJson)) match {
    case JsSuccess(value, _)                => println(value)
    case play.api.libs.json.JsError(errors) => println(errors)
  }

  User.schema.bind[Format].reads(Json.parse(goodUserJson)) match {
    case JsSuccess(value, _)                => println(value)
    case play.api.libs.json.JsError(errors) => println(errors)
  }
}

sealed trait User extends Product with Serializable

final case class GoodUser(name: String, age: Int) extends User

object GoodUser {
  implicit val schema = Schema.of[GoodUser]
  //    Schema[GoodUser](
  //    (Path \ "name").as[String]().defaultTo("meth") and
  //    (Path \ "age").as[Int]()
  //  )

  implicit val reads: Reads[GoodUser] = {
    import io.github.methrat0n.restruct.readers.json._
    schema.bind[Reads]
  }
  //
  //  implicit val queryStringBindable: QueryStringBindable[GoodUser] = {
  //    import io.github.methrat0n.restruct.handlers.queryStringBindable._
  //    GoodUser.schema.bind[QueryStringBindable]
  //  }
}

final case class BadUser(name: String, age: Int) extends User

object BadUser {
  implicit val schema = Schema[BadUser](
    (Path \ "name").as[String]() and
      (Path \ "age").as[Int]()
  )

  implicit val reads: Reads[BadUser] = {
    import io.github.methrat0n.restruct.handlers.json._
    schema.bind[Format]
  }
}

final case class WrappedUser(users: List[GoodUser], texts: String)

object WrappedUser {
  implicit val schema = Schema[WrappedUser](
    (Path \ "user").many[GoodUser, List]() and
      (Path \ "text").as[String]()
  )
  implicit val reads: Reads[WrappedUser] = {
    import io.github.methrat0n.restruct.readers.json._
    schema.bind[Reads]
  }
}

object User {
  //implicit val schema = Schema[User](GoodUser.schema or BadUser.schema)
  implicit val schema = Schema.Strict.of[User]
}
