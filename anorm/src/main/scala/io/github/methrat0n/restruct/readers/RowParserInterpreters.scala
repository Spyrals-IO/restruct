package io.github.methrat0n.restruct.readers

import java.time.{ LocalDate, LocalTime, ZonedDateTime }

import anorm.{ RowParser, SqlParser }
import io.github.methrat0n.restruct.schema.Interpreter.SimpleInterpreter

trait RowParserInterpreters {
  implicit val rowParserCharInterpreter: SimpleInterpreter[RowParser, Char] = new SimpleInterpreter[RowParser, Char] {
    override def schema: RowParser[Char] = SqlParser.scalar[Char]
  }
  implicit val rowParserByteInterpreter: SimpleInterpreter[RowParser, Byte] = new SimpleInterpreter[RowParser, Byte] {
    override def schema: RowParser[Byte] = SqlParser.scalar[Byte]
  }
  implicit val rowParserShortInterpreter: SimpleInterpreter[RowParser, Short] = new SimpleInterpreter[RowParser, Short] {
    override def schema: RowParser[Short] = SqlParser.scalar[Short]
  }
  implicit val rowParserFloatInterpreter: SimpleInterpreter[RowParser, Float] = new SimpleInterpreter[RowParser, Float] {
    override def schema: RowParser[Float] = SqlParser.scalar[Float]
  }
  implicit val rowParserDecimalInterpreter: SimpleInterpreter[RowParser, Double] = new SimpleInterpreter[RowParser, Double] {
    override def schema: RowParser[Double] = SqlParser.scalar[Double]
  }
  implicit val rowParserBigDecimalReadInterpreter: SimpleInterpreter[RowParser, BigDecimal] = new SimpleInterpreter[RowParser, BigDecimal] {
    override def schema: RowParser[BigDecimal] = SqlParser.scalar[BigDecimal]
  }
  implicit val rowParserIntegerReadInterpreter: SimpleInterpreter[RowParser, Int] = new SimpleInterpreter[RowParser, Int] {
    override def schema: RowParser[Int] = SqlParser.scalar[Int]
  }
  implicit val rowParserLongReadInterpreter: SimpleInterpreter[RowParser, Long] = new SimpleInterpreter[RowParser, Long] {
    override def schema: RowParser[Long] = SqlParser.scalar[Long]
  }
  implicit val rowParserBigIntReadInterpreter: SimpleInterpreter[RowParser, BigInt] = new SimpleInterpreter[RowParser, BigInt] {
    override def schema: RowParser[BigInt] = SqlParser.scalar[BigInt]
  }
  implicit val rowParserBooleanReadInterpreter: SimpleInterpreter[RowParser, Boolean] = new SimpleInterpreter[RowParser, Boolean] {
    override def schema: RowParser[Boolean] = SqlParser.scalar[Boolean]
  }
  implicit val rowParserStringReadInterpreter: SimpleInterpreter[RowParser, String] = new SimpleInterpreter[RowParser, String] {
    override def schema: RowParser[String] = SqlParser.scalar[String]
  }
  implicit val rowParserDateTimeReadInterpreter: SimpleInterpreter[RowParser, ZonedDateTime] = new SimpleInterpreter[RowParser, ZonedDateTime] {
    override def schema: RowParser[ZonedDateTime] = SqlParser.scalar[ZonedDateTime]
  }
  implicit val rowParserTimeReadInterpreter: SimpleInterpreter[RowParser, LocalTime] = new SimpleInterpreter[RowParser, LocalTime] {
    override def schema: RowParser[LocalTime] = rowParserDateTimeReadInterpreter.schema.map(_.toLocalTime)
  }
  implicit val rowParserDateReadInterpreter: SimpleInterpreter[RowParser, LocalDate] = new SimpleInterpreter[RowParser, LocalDate] {
    override def schema: RowParser[LocalDate] = SqlParser.scalar[LocalDate]
  }
}
