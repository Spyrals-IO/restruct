package io.github.methrat0n.restruct.readers.config

import java.time.{ LocalDate, LocalTime, ZonedDateTime }
import play.api.ConfigLoader

trait SimpleConfigInterpreter extends SimpleSchemaAlgebra[ConfigLoader] {
  override def charSchema: ConfigLoader[Char] =
    ConfigLoader.stringLoader.map(_.charAt(0))

  override def byteSchema: ConfigLoader[Byte] =
    charSchema.map(_.asInstanceOf[Byte])

  override def shortSchema: ConfigLoader[Short] =
    charSchema.map(_.asInstanceOf[Short])

  override def floatSchema: ConfigLoader[Float] =
    ConfigLoader.doubleLoader.map(_.toFloat)

  override def decimalSchema: ConfigLoader[Double] =
    ConfigLoader.doubleLoader

  override def bigDecimalSchema: ConfigLoader[BigDecimal] =
    ConfigLoader.stringLoader.map(BigDecimal.apply)

  override def integerSchema: ConfigLoader[Int] =
    ConfigLoader.intLoader

  override def longSchema: ConfigLoader[Long] =
    ConfigLoader.longLoader

  override def bigIntSchema: ConfigLoader[BigInt] =
    ConfigLoader.stringLoader.map(BigInt.apply)

  override def booleanSchema: ConfigLoader[Boolean] =
    ConfigLoader.booleanLoader

  override def stringSchema: ConfigLoader[String] =
    ConfigLoader.stringLoader

  override def dateTimeSchema: ConfigLoader[ZonedDateTime] =
    ConfigLoader.stringLoader.map(ZonedDateTime.parse)

  override def dateSchema: ConfigLoader[LocalDate] =
    ConfigLoader.stringLoader.map(LocalDate.parse)

  override def timeSchema: ConfigLoader[LocalTime] =
    ConfigLoader.stringLoader.map(LocalTime.parse)
}
