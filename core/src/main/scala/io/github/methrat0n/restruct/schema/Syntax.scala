package io.github.methrat0n.restruct.schema

import java.time.{ LocalDate, LocalTime, ZonedDateTime }

import io.github.methrat0n.restruct.core.data.schema.{ FieldAlgebra, Path }

import scala.language.higherKinds

object Syntax {

  implicit val string: Schema[String] = new Schema[String] {
    override def bind[FORMAT[_]](algebra: FieldAlgebra[FORMAT]): FORMAT[String] =
      algebra.stringSchema
  }
  implicit val decimal: Schema[Double] = new Schema[Double] {
    override def bind[FORMAT[_]](algebra: FieldAlgebra[FORMAT]): FORMAT[Double] =
      algebra.decimalSchema
  }
  implicit val integer: Schema[Int] = new Schema[Int] {
    override def bind[FORMAT[_]](algebra: FieldAlgebra[FORMAT]): FORMAT[Int] =
      algebra.integerSchema
  }
  implicit val boolean: Schema[Boolean] = new Schema[Boolean] {
    override def bind[FORMAT[_]](algebra: FieldAlgebra[FORMAT]): FORMAT[Boolean] =
      algebra.booleanSchema
  }
  implicit val char: Schema[Char] = new Schema[Char] {
    override def bind[FORMAT[_]](algebra: FieldAlgebra[FORMAT]): FORMAT[Char] =
      algebra.charSchema
  }
  implicit val byte: Schema[Byte] = new Schema[Byte] {
    override def bind[FORMAT[_]](algebra: FieldAlgebra[FORMAT]): FORMAT[Byte] =
      algebra.byteSchema
  }
  implicit val short: Schema[Short] = new Schema[Short] {
    override def bind[FORMAT[_]](algebra: FieldAlgebra[FORMAT]): FORMAT[Short] =
      algebra.shortSchema
  }
  implicit val float: Schema[Float] = new Schema[Float] {
    override def bind[FORMAT[_]](algebra: FieldAlgebra[FORMAT]): FORMAT[Float] =
      algebra.floatSchema
  }
  implicit val bigDecimal: Schema[BigDecimal] = new Schema[BigDecimal] {
    override def bind[FORMAT[_]](algebra: FieldAlgebra[FORMAT]): FORMAT[BigDecimal] =
      algebra.bigDecimalSchema
  }
  implicit val long: Schema[Long] = new Schema[Long] {
    override def bind[FORMAT[_]](algebra: FieldAlgebra[FORMAT]): FORMAT[Long] =
      algebra.longSchema
  }
  implicit val bigInt: Schema[BigInt] = new Schema[BigInt] {
    override def bind[FORMAT[_]](algebra: FieldAlgebra[FORMAT]): FORMAT[BigInt] =
      algebra.bigIntSchema
  }
  implicit val dateTime: Schema[ZonedDateTime] = new Schema[ZonedDateTime] {
    override def bind[FORMAT[_]](algebra: FieldAlgebra[FORMAT]): FORMAT[ZonedDateTime] =
      algebra.dateTimeSchema
  }
  implicit val time: Schema[LocalTime] = new Schema[LocalTime] {
    override def bind[FORMAT[_]](algebra: FieldAlgebra[FORMAT]): FORMAT[LocalTime] =
      algebra.timeSchema
  }
  implicit val date: Schema[LocalDate] = new Schema[LocalDate] {
    override def bind[FORMAT[_]](algebra: FieldAlgebra[FORMAT]): FORMAT[LocalDate] =
      algebra.dateSchema
  }

  def list[T](implicit schema: Schema[T]): Schema[List[T]] = new Schema[List[T]] {
    override def bind[FORMAT[_]](algebra: FieldAlgebra[FORMAT]): FORMAT[List[T]] =
      algebra.many(schema.bind(algebra))
  }

  import language.implicitConversions
  import language.experimental.macros

  implicit def string2Path(step: String): Path = Path \ step
  implicit def int2Path(step: Int): Path = Path \ step
  implicit def compositeSchema2ComplexSchema[Typ, Composition](schema: Schema[Composition]): Schema[Typ] = macro Impl.simple[Typ, Composition]

}
