package restruct.examples

import play.api.libs.json.{ Json, Reads }
import io.github.methrat0n.restruct.schema.Schema
import io.github.methrat0n.restruct.readers.json.JsonReaderInterpreter

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

}
