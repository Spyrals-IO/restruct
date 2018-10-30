package restruct.examples

import play.api.libs.json.{ Json, Reads }
import restruct.algebras.json.playjson.reads.JsonReaderInterpreter
import restruct.examples.SyntaxExample.BankAccount.reads
import restruct.reader.Reader

object SyntaxExample extends App {

  final case class Person(
    lastName: String,
    firstName: String,
    bankAccount: BankAccount,
    work: Option[Work]
  )

  object Person {
    import restruct.constraints._
    import restruct.reader.Syntax._
    val work = Work.schema
    val bankAccount = BankAccount.schema
    implicit lazy val schema: Reader[Person] = Reader is (
      "lastName".as(string.constrainted(minSize(0))) and
      "firstName".as(string) and
      "bankAccount".as(bankAccount) and
      "work".as(option.of(work))
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
  )

  object BankAccount {
    import restruct.reader.Syntax._
    implicit lazy val schema: Reader[BankAccount] = Reader.is(
      "amount".as(bigInt)
    )

    val json = JsonReaderInterpreter
    lazy val reads: Reads[BankAccount] = schema.read(json)
  }

  final case class Work(
    jobTitle: String,
    salary: BigDecimal,
    subordinates: List[Person]
  )

  object Work {
    import restruct.reader.Syntax._

    val person = Person.schema
    implicit lazy val schema: Reader[Work] = Reader is (
      "jobTitle".as(string) and
      "salary".as(bigDecimal) and
      "subordinates".as(list.of(person))
    )
  }

  val goodJson =
    """
      |{
      |  "amount": 111
      |}
    """.stripMargin

  val badJson =
    """
      |{
      |  "amount": { "amount": 11.1 }
      |}
    """.stripMargin

  println(BankAccount.schema)

  reads.reads(Json.parse(goodJson)) match {
    case play.api.libs.json.JsSuccess(value, _) => println(value)
    case play.api.libs.json.JsError(errors)     => println(errors)
  }

  restruct.reader.Syntax.bigInt.read(JsonReaderInterpreter).reads(Json.parse("111")) match {
    case play.api.libs.json.JsSuccess(value, _) => println(value)
    case play.api.libs.json.JsError(errors)     => println(errors)
  }
}
