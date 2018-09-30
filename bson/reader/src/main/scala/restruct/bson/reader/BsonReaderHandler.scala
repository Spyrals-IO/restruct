package restruct.bson.reader

import java.time.{ZoneOffset, ZonedDateTime}

import reactivemongo.bson.{BSONDocument, BSONReader, BSONValue, DefaultBSONHandlers}
import restruct.core.Program
import restruct.core.data.constraints.Constraint
import restruct.core.data.schema.SchemaAlgebra


object BsonReaderHandler {

  def run[T](program: Program[SchemaAlgebra, T]): BSONReader[BSONValue, T] = program.run(Handler)

  private[bson] object Handler extends SchemaAlgebra[BSONReader[BSONValue, ?]] {
    override def stringSchema: BSONReader[BSONValue, String] =
      DefaultBSONHandlers.BSONStringHandler.asInstanceOf[BSONReader[BSONValue, String]]

    override def decimalSchema: BSONReader[BSONValue, Double] =
      DefaultBSONHandlers.BSONDoubleHandler.asInstanceOf[BSONReader[BSONValue, Double]]

    override def integerSchema: BSONReader[BSONValue, Int] =
      DefaultBSONHandlers.BSONIntegerHandler.asInstanceOf[BSONReader[BSONValue, Int]]

    override def longSchema: BSONReader[BSONValue, Long] =
      DefaultBSONHandlers.BSONLongHandler.asInstanceOf[BSONReader[BSONValue, Long]]

    override def booleanSchema: BSONReader[BSONValue, Boolean] =
      DefaultBSONHandlers.BSONBooleanHandler.asInstanceOf[BSONReader[BSONValue, Boolean]]

    override def bigDecimalSchema: BSONReader[BSONValue, BigDecimal] =
      DefaultBSONHandlers.BSONDecimalHandler.asInstanceOf[BSONReader[BSONValue, BigDecimal]]

    override def dateTimeSchema: BSONReader[BSONValue, ZonedDateTime] =
      DefaultBSONHandlers.BSONDateTimeHandler
        .afterRead(date => ZonedDateTime.ofInstant(date.toInstant, ZoneOffset.UTC))
        .asInstanceOf[BSONReader[BSONValue, ZonedDateTime]]

    private def readDocumentWithDefault[T](name: String, schema: BSONReader[BSONValue, T], default: Option[T]): BSONReader[BSONValue, Option[T]] = BSONReader {
      case document: BSONDocument => default
        .map(defaultValue => Some(document.getAs(name)(schema).getOrElse(defaultValue)))
        .getOrElse(document.getAs(name)(schema))
      case value => throw new RuntimeException(s"bsonvalue $value should be a BSONDocument")
    }

    override def many[T](name: String, schema: BSONReader[BSONValue, T], default: Option[List[T]]): BSONReader[BSONValue, List[T]] = {
      val listReader = DefaultBSONHandlers.bsonArrayToCollectionReader[List, T](implicitly, schema)
      readDocumentWithDefault(name, listReader.asInstanceOf[BSONReader[BSONValue, List[T]]], default).afterRead(_.getOrElse(List.empty))
    }

    override def keyValue[T](name: String, schema: BSONReader[BSONValue, T], default: Option[Map[String, T]]): BSONReader[BSONValue, Map[String, T]] = {
      val mapReader = BSONReader[BSONDocument, Map[String, T]](
        document => document.elements.foldLeft(Map.empty[String, T])(
          (acc, element) => acc + (element.name -> schema.read(element.value))
        )
      )
      readDocumentWithDefault[Map[String, T]](name, mapReader.asInstanceOf[BSONReader[BSONValue, Map[String, T]]], default).afterRead(_.getOrElse(Map.empty))
    }

    override def optional[T](name: String, schema: BSONReader[BSONValue, T], default: Option[Option[T]]): BSONReader[BSONValue, Option[T]] =
      readDocumentWithDefault[T](name, schema, default.flatten)

    override def required[T](name: String, schema: BSONReader[BSONValue, T], default: Option[T]): BSONReader[BSONValue, T] =
      readDocumentWithDefault(name, schema, default).afterRead(_.get)

    override def verifying[T](schema: BSONReader[BSONValue, T], constraint: Constraint[T]): BSONReader[BSONValue, T] = BSONReader { bson =>
      val parsed = schema.read(bson)
      if (constraint.validate(parsed))
        parsed
      else
        throw new RuntimeException(s"Constraint ${constraint.name} check failed for $parsed")
    }

    override def either[A, B](fa: BSONReader[BSONValue, A], fb: BSONReader[BSONValue, B]): BSONReader[BSONValue, Either[A, B]] =
      BSONReader(bsonValue => fa.readOpt(bsonValue) match {
        case Some(a) => Left(a)
        case None    => Right(fb.read(bsonValue))
      })

    override def pure[T](a: T): BSONReader[BSONValue, T] = BSONReader(
      _ => a
    )

    override def imap[A, B](fa: BSONReader[BSONValue, A])(f: A => B)(g: B => A): BSONReader[BSONValue, B] =
      fa.afterRead[B](f)

    override def product[A, B](fa: BSONReader[BSONValue, A], fb: BSONReader[BSONValue, B]): BSONReader[BSONValue, (A, B)] = BSONReader(
      value => (
        fa.read(value),
        fb.read(value)
      )
    )

  }
}
