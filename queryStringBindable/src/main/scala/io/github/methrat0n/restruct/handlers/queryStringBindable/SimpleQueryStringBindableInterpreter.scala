package io.github.methrat0n.restruct.handlers.queryStringBindable

import java.time.{ LocalDate, LocalTime, ZonedDateTime }
import play.api.mvc.QueryStringBindable

import scala.util.Try

trait SimpleQueryStringBindableInterpreter extends SimpleSchemaAlgebra[QueryStringBindable] {
  override def charSchema: QueryStringBindable[Char] =
    QueryStringBindable.bindableChar

  override def byteSchema: QueryStringBindable[Byte] = new QueryStringBindable[Byte] {
    override def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, Byte]] =
      QueryStringBindable.bindableChar.bind(key, params)
        .flatMap(either =>
          if (either.isLeft)
            integerSchema.bind(key, params).map(_.flatMap(int =>
            if (int > Byte.MaxValue || int < Byte.MinValue)
              Left(s"Cannot parse parameter $key with value '$int' as Byte: $key is out of the byte bounds")
            else
              Right(int.toByte)))
          else Some(either.map(_.toByte)))

    override def unbind(key: String, value: Byte): String =
      QueryStringBindable.bindableChar.unbind(key, value.toChar)
  }

  override def shortSchema: QueryStringBindable[Short] = new QueryStringBindable[Short] {
    override def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, Short]] =
      QueryStringBindable.bindableChar.bind(key, params).flatMap(either =>
        if (either.isLeft)
          integerSchema.bind(key, params).map(_.flatMap(int =>
          if (int > Short.MaxValue || int < Short.MinValue)
            Left(s"Cannot parse parameter $key with value '$int' as Short: $key is out of the short bounds")
          else
            Right(int.toShort)))
        else Some(either.map(_.toShort)))

    override def unbind(key: String, value: Short): String =
      QueryStringBindable.bindableChar.unbind(key, value.toChar)
  }

  override def floatSchema: QueryStringBindable[Float] =
    QueryStringBindable.bindableFloat

  override def decimalSchema: QueryStringBindable[Double] =
    QueryStringBindable.bindableDouble

  override def bigDecimalSchema: QueryStringBindable[BigDecimal] = new QueryStringBindable[BigDecimal] {
    override def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, BigDecimal]] =
      QueryStringBindable.bindableString.bind(key, params).map(_.flatMap(string =>
        Try { BigDecimal(string) }.toEither.left.map(_ =>
          s"Cannot parse parameter $key with value '$string' as BigDecimal: $key must be a number")))

    override def unbind(key: String, value: BigDecimal): String =
      QueryStringBindable.bindableString.unbind(key, value.toString)
  }

  override def integerSchema: QueryStringBindable[Int] =
    QueryStringBindable.bindableInt

  override def longSchema: QueryStringBindable[Long] =
    QueryStringBindable.bindableLong

  override def bigIntSchema: QueryStringBindable[BigInt] = new QueryStringBindable[BigInt] {
    override def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, BigInt]] =
      QueryStringBindable.bindableString.bind(key, params).map(_.flatMap(string =>
        Try { BigInt(string) }.toEither.left.map(_ =>
          s"Cannot parse parameter $key with value '$string' as BigDecimal: $key must be a natural number")))

    override def unbind(key: String, value: BigInt): String =
      QueryStringBindable.bindableString.unbind(key, value.toString)
  }

  override def booleanSchema: QueryStringBindable[Boolean] =
    QueryStringBindable.bindableBoolean

  override def stringSchema: QueryStringBindable[String] =
    QueryStringBindable.bindableString

  override def dateTimeSchema: QueryStringBindable[ZonedDateTime] = new QueryStringBindable[ZonedDateTime] {
    override def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, ZonedDateTime]] =
      QueryStringBindable.bindableString.bind(key, params).map(_.flatMap(string =>
        Try { ZonedDateTime.parse(string) }.toEither.left.map(_ =>
          s"Cannot parse parameter $key with value '$string' as ZonedDateTime: $key must contains a valid date and time")))

    override def unbind(key: String, value: ZonedDateTime): String =
      QueryStringBindable.bindableString.unbind(key, value.toString)
  }

  override def dateSchema: QueryStringBindable[LocalDate] = new QueryStringBindable[LocalDate] {
    override def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, LocalDate]] =
      QueryStringBindable.bindableString.bind(key, params).map(_.flatMap(string =>
        Try { LocalDate.parse(string) }.toEither.left.map(_ =>
          s"Cannot parse parameter $key with value '$string' as LocalDate: $key must contains a valid date")))

    override def unbind(key: String, value: LocalDate): String =
      QueryStringBindable.bindableString.unbind(key, value.toString)
  }

  override def timeSchema: QueryStringBindable[LocalTime] = new QueryStringBindable[LocalTime] {
    override def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, LocalTime]] =
      QueryStringBindable.bindableString.bind(key, params).map(_.flatMap(string =>
        Try { LocalTime.parse(string) }.toEither.left.map(_ =>
          s"Cannot parse parameter $key with value '$string' as LocalTime: $key must contains a valid time")))

    override def unbind(key: String, value: LocalTime): String =
      QueryStringBindable.bindableString.unbind(key, value.toString)
  }
}
