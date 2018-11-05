package restruct.examples

import play.api.libs.json.{Json, Reads}
import restruct.algebras.json.playjson.reads.JsonReaderInterpreter
import restruct.examples.SyntaxExample.Examples.BankAccount
import restruct.reader.Schema

object SyntaxExample extends App {

  sealed trait Examples

  object Examples {
    final case class Person(
      lastName: String,
      firstName: String
    ) extends Examples

    object Person {
      import restruct.constraints._
      import restruct.reader.Syntax._
      implicit lazy val schema: Schema[Person] = Schema is (
        "lastName".as(string.constrainted(minSize(0))) and
        "firstName".as(string)
      )
      val json = JsonReaderInterpreter
      lazy val reads: Reads[Person] = schema.read(json)

      val personJson =
        """
          |{
          |  lastName: 1,
          |  firstName:
          |}
        """.stripMargin
    }

    final case class BankAccount(
      amount: BigInt
    ) extends Examples

    object BankAccount {
      import restruct.reader.Syntax._
      implicit lazy val schema: Schema[BankAccount] = Schema.is(
        "amount".as(bigInt)
      )

      val json = JsonReaderInterpreter
      lazy val reads: Reads[BankAccount] = schema.read(json)
    }

    final case class Work(
      jobTitle: String,
      salary: BigDecimal,
    ) extends Examples

    object Work {
      import restruct.reader.Syntax._

      val person = Person.schema
      implicit lazy val schema: Schema[Work] = Schema is (
        "jobTitle".as(string) and
        "salary".as(bigDecimal)
      )
    }

    implicit val schema: Schema[Examples] = Schema is (
      Person.schema, BankAccount.schema, Work.schema
    )
    val json = JsonReaderInterpreter
    val reads: Reads[Examples] = schema.read(json)
  }

  val goodJson =
    """
      |{
      |  "jobTitle": "title",
      |  "salary": 12
      |}
    """.stripMargin

  val badJson =
    """
      |{
      |  "amount": { "amount": 11.1 }
      |}
    """.stripMargin

  println(BankAccount.schema)

  Examples.reads.reads(Json.parse(goodJson)) match {
    case play.api.libs.json.JsSuccess(value, _) => println(value)
    case play.api.libs.json.JsError(errors)     => println(errors)
  }

  restruct.reader.Syntax.bigInt.read(JsonReaderInterpreter).reads(Json.parse("111")) match {
    case play.api.libs.json.JsSuccess(value, _) => println(value)
    case play.api.libs.json.JsError(errors)     => println(errors)
  }

}
