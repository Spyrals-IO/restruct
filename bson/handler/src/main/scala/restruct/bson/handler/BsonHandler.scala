package restruct.bson.handler

import java.time.{ZoneOffset, ZonedDateTime}
import java.util.Date

import reactivemongo.bson.{BSONHandler, BSONValue, DefaultBSONHandlers}
import restruct.bson.reader.BsonReaderHandler
import restruct.bson.writer.BsonWriterHandler
import restruct.core.Program
import restruct.core.data.constraints.Constraint
import restruct.core.data.schema.SchemaAlgebra


object BsonHandler {

  def run[T](program: Program[SchemaAlgebra, T]): BSONHandler[BSONValue, T] = program.run(Handler)

  private val readerHandler = BsonReaderHandler.Handler
  private val writerHandler = BsonWriterHandler.Handler

  private object Handler extends SchemaAlgebra[BSONHandler[BSONValue, ?]] {
    override def stringSchema: BSONHandler[BSONValue, String] =
      DefaultBSONHandlers.BSONStringHandler.asInstanceOf[BSONHandler[BSONValue, String]]

    override def decimalSchema: BSONHandler[BSONValue, Double] =
      DefaultBSONHandlers.BSONDoubleHandler.asInstanceOf[BSONHandler[BSONValue, Double]]

    override def integerSchema: BSONHandler[BSONValue, Int] =
      DefaultBSONHandlers.BSONIntegerHandler.asInstanceOf[BSONHandler[BSONValue, Int]]

    override def longSchema: BSONHandler[BSONValue, Long] =
      DefaultBSONHandlers.BSONLongHandler.asInstanceOf[BSONHandler[BSONValue, Long]]

    override def booleanSchema: BSONHandler[BSONValue, Boolean] =
      DefaultBSONHandlers.BSONBooleanHandler.asInstanceOf[BSONHandler[BSONValue, Boolean]]

    override def bigDecimalSchema: BSONHandler[BSONValue, BigDecimal] =
      DefaultBSONHandlers.BSONDecimalHandler.asInstanceOf[BSONHandler[BSONValue, BigDecimal]]

    override def dateTimeSchema: BSONHandler[BSONValue, ZonedDateTime] =
      DefaultBSONHandlers.BSONDateTimeHandler.as[ZonedDateTime](
        date => ZonedDateTime.ofInstant(date.toInstant, ZoneOffset.UTC),
        zonedDateTime => Date.from(zonedDateTime.toInstant)
      ).asInstanceOf[BSONHandler[BSONValue, ZonedDateTime]]

    override def many[T](name: String, schema: BSONHandler[BSONValue, T], default: Option[List[T]]): BSONHandler[BSONValue, List[T]] = BSONHandler(
      readerHandler.many(name, schema, default).read,
      writerHandler.many(name, schema, default).write
    )

    override def keyValue[T](name: String, schema: BSONHandler[BSONValue, T], default: Option[Map[String, T]]): BSONHandler[BSONValue, Map[String, T]] = BSONHandler(
      readerHandler.keyValue(name, schema, default).read,
      writerHandler.keyValue(name, schema, default).write
    )

    override def optional[T](name: String, schema: BSONHandler[BSONValue, T], default: Option[Option[T]]): BSONHandler[BSONValue, Option[T]] = BSONHandler(
      readerHandler.optional(name, schema, default).read,
      writerHandler.optional(name, schema, default).write
    )

    override def required[T](name: String, schema: BSONHandler[BSONValue, T], default: Option[T]): BSONHandler[BSONValue, T] = BSONHandler(
      readerHandler.required(name, schema, default).read,
      writerHandler.required(name, schema, default).write
    )

    override def verifying[T](schema: BSONHandler[BSONValue, T], constraint: Constraint[T]): BSONHandler[BSONValue, T] = BSONHandler(
      readerHandler.verifying(schema, constraint).read,
      writerHandler.verifying(schema, constraint).write
    )

    override def either[A, B](fa: BSONHandler[BSONValue, A], fb: BSONHandler[BSONValue, B]): BSONHandler[BSONValue, Either[A, B]] = BSONHandler(
      readerHandler.either(fa, fb).read,
      writerHandler.either(fa, fb).write
    )

    override def pure[T](a: T): BSONHandler[BSONValue, T] = BSONHandler(
      readerHandler.pure(a).read,
      writerHandler.pure(a).write
    )

    override def imap[A, B](fa: BSONHandler[BSONValue, A])(f: A => B)(g: B => A): BSONHandler[BSONValue, B] = BSONHandler(
      readerHandler.imap(fa)(f)(g).read,
      writerHandler.imap(fa)(f)(g).write
    )

    override def product[A, B](fa: BSONHandler[BSONValue, A], fb: BSONHandler[BSONValue, B]): BSONHandler[BSONValue, (A, B)] = BSONHandler(
      readerHandler.product(fa, fb).read,
      writerHandler.product(fa, fb).write
    )
  }
}
