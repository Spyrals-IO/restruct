package restruct.urlencode

import java.net.URLEncoder
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

import restruct.core.Program
import restruct.core.data.constraints.Constraint
import restruct.core.data.schema.SchemaAlgebra
import shapeless.HNil


object UrlEncode {

  /**
   * A Show type to transform T to stripe format
   */
  type StripeShow[T] = T => String
  def run[T](program: Program[SchemaAlgebra, T]): StripeShow[T] = program.run(Handler)(maybe => maybe.getOrElse(""))

  /**
   * Type class wich allow to pass the parent context down to it's child
   */
  private trait ParentHandler {
    def handleParent(maybeChild: Option[String]): String
  }

  private def parenting(name: String, parentHandler: ParentHandler): ParentHandler = {
    case Some(child) => parentHandler.handleParent(Some(s"$name[$child]"))
    case None        => parentHandler.handleParent(Some(name))
  }

  private type UrlEncode[T] = ParentHandler => StripeShow[T]

  /**
   * Algebra describing the stripe format and creating an urlEncode, which will be used to get a StripeShow
   *
   * The stripe format is the following urlEncode format:
   * name=value&otherName=otherValue&lastName=lastValue
   *
   * examples:
   * case class User(name: String, age: Int)
   *
   * User("john", 23) => name=john&age=23
   *
   * case class Room(inhabitant: User, size: Int)
   *
   * Room(User("john", 23), 34) => user[name]=john&user[age]=23&size=34
   */
  private object Handler extends SchemaAlgebra[UrlEncode] {
    override def stringSchema: UrlEncode[String] = (parentHandler) => (string) =>
      s"${parentHandler.handleParent(None)}=${URLEncoder.encode(string, "UTF-8")}"

    override def decimalSchema: UrlEncode[Double] = (parentHandler) => (double) =>
      stringSchema(parentHandler)(double.toString)

    override def integerSchema: UrlEncode[Int] = (parentHandler) => (int) =>
      stringSchema(parentHandler)(int.toString)

    override def longSchema: UrlEncode[Long] = (parentHandler) => (long) =>
      stringSchema(parentHandler)(long.toString)

    override def booleanSchema: UrlEncode[Boolean] = (parentHandler) => (boolean) =>
      stringSchema(parentHandler)(boolean.toString)

    override def bigDecimalSchema: UrlEncode[BigDecimal] = (parentHandler) => (bigDecimal) =>
      stringSchema(parentHandler)(bigDecimal.toString)

    override def dateTimeSchema: UrlEncode[ZonedDateTime] = (parentHandler) => (zonedDateTime) =>
      stringSchema(parentHandler)(zonedDateTime.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))

    override def many[T](name: String, schema: UrlEncode[T], default: Option[List[T]]): UrlEncode[List[T]] = (parentHandler) => (list) =>
      list.map(element => schema(parenting(name, parentHandler))(element)).mkString("&")

    override def keyValue[T](name: String, schema: UrlEncode[T], default: Option[Map[String, T]]): UrlEncode[Map[String, T]] = (parentHandler) => (map) =>
      map.toList.map {
        case (key, value) => schema(parenting(s"$name[$key]", parentHandler))(value)
      }.mkString("&")

    override def optional[T](name: String, schema: UrlEncode[T], default: Option[Option[T]]): UrlEncode[Option[T]] = (parentHandler) => {
      case Some(value) => schema(parenting(name, parentHandler))(value)
      case None        => ""
    }

    override def required[T](name: String, schema: UrlEncode[T], default: Option[T]): UrlEncode[T] = (parentHandler) => (obj) =>
      schema(parenting(name, parentHandler))(obj)

    override def verifying[T](schema: UrlEncode[T], constraint: Constraint[T]): UrlEncode[T] =
      schema

    override def either[A, B](left: UrlEncode[A], right: UrlEncode[B]): UrlEncode[Either[A, B]] = (parentHandler) => {
      case Right(value) => right(parentHandler)(value)
      case Left(err)    => left(parentHandler)(err)
    }

    override def pure[T](a: T): UrlEncode[T] = (parentHandler) => {
      case HNil  => ""
      case other => stringSchema(parentHandler)(other.toString)
    }

    override def imap[A, B](fa: UrlEncode[A])(f: A => B)(g: B => A): UrlEncode[B] = (parentHandler) => (b) =>
      fa(parentHandler)(g(b))

    override def product[A, B](fa: UrlEncode[A], fb: UrlEncode[B]): UrlEncode[(A, B)] = (parentHandler) => {
      case (a, b) => (fa(parentHandler)(a), fb(parentHandler)(b)) match {
        case ("", "")     => ""
        case ("", string) => string
        case (string, "") => string
        case (s1, s2)     => s"$s1&$s2"
      }
    }

  }

}
