package io.github.methrat0n.restruct.handlers.queryStringBindable

import java.time.{ LocalDate, LocalTime, ZonedDateTime }

import io.github.methrat0n.restruct.core.data.schema.SimpleSchemaAlgebra
import play.api.mvc.QueryStringBindable

trait SimpleQueryStringBindableInterpreter extends SimpleSchemaAlgebra[QueryStringBindable] {
  override def charSchema: QueryStringBindable[Char] =
    QueryStringBindable.bindableChar

  override def byteSchema: QueryStringBindable[Byte] = new QueryStringBindable[Byte] {
    override def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, Byte]] =
      QueryStringBindable.bindableChar.bind(key, params).map(_.map(_.toByte))

    override def unbind(key: String, value: Byte): String =
      QueryStringBindable.bindableChar.unbind(key, value.toChar)
  }

  override def shortSchema: QueryStringBindable[Short] = new QueryStringBindable[Short] {
    override def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, Short]] =
      QueryStringBindable.bindableChar.bind(key, params).map(_.map(_.toShort))

    override def unbind(key: String, value: Short): String =
      QueryStringBindable.bindableChar.unbind(key, value.toChar)
  }

  override def floatSchema: QueryStringBindable[Float] =
    QueryStringBindable.bindableFloat

  override def decimalSchema: QueryStringBindable[Double] =
    QueryStringBindable.bindableDouble

  override def bigDecimalSchema: QueryStringBindable[BigDecimal] = new QueryStringBindable[BigDecimal] {
    override def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, BigDecimal]] =
      QueryStringBindable.bindableString.bind(key, params).map(_.map(BigDecimal.apply))

    override def unbind(key: String, value: BigDecimal): String =
      QueryStringBindable.bindableString.unbind(key, value.toString)
  }

  override def integerSchema: QueryStringBindable[Int] =
    QueryStringBindable.bindableInt

  override def longSchema: QueryStringBindable[Long] =
    QueryStringBindable.bindableLong

  override def bigIntSchema: QueryStringBindable[BigInt] = new QueryStringBindable[BigInt] {
    override def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, BigInt]] =
      QueryStringBindable.bindableString.bind(key, params).map(_.map(BigInt.apply))

    override def unbind(key: String, value: BigInt): String =
      QueryStringBindable.bindableString.unbind(key, value.toString)
  }

  override def booleanSchema: QueryStringBindable[Boolean] =
    QueryStringBindable.bindableBoolean

  override def stringSchema: QueryStringBindable[String] =
    QueryStringBindable.bindableString

  override def dateTimeSchema: QueryStringBindable[ZonedDateTime] = new QueryStringBindable[ZonedDateTime] {
    override def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, ZonedDateTime]] =
      QueryStringBindable.bindableString.bind(key, params).map(_.map(ZonedDateTime.parse))

    override def unbind(key: String, value: ZonedDateTime): String =
      QueryStringBindable.bindableString.unbind(key, value.toString)
  }

  override def dateSchema: QueryStringBindable[LocalDate] = new QueryStringBindable[LocalDate] {
    override def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, LocalDate]] =
      QueryStringBindable.bindableString.bind(key, params).map(_.map(LocalDate.parse))

    override def unbind(key: String, value: LocalDate): String =
      QueryStringBindable.bindableString.unbind(key, value.toString)
  }

  override def timeSchema: QueryStringBindable[LocalTime] = new QueryStringBindable[LocalTime] {
    override def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, LocalTime]] =
      QueryStringBindable.bindableString.bind(key, params).map(_.map(LocalTime.parse))

    override def unbind(key: String, value: LocalTime): String =
      QueryStringBindable.bindableString.unbind(key, value.toString)
  }
}
