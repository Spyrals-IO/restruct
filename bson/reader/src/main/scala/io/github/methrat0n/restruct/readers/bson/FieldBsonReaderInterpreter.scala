package io.github.methrat0n.restruct.readers.bson

import io.github.methrat0n.restruct.constraints.Constraint
import io.github.methrat0n.restruct.schema.{ NoMatchException, Path }
import reactivemongo.bson.{ BSONReader, BSONValue }

import scala.util.{ Failure, Success }

trait FieldBsonReaderInterpreter extends FieldAlgebra[BsonReader] {

  override def required[T](path: Path, schema: BsonReader[T], default: Option[T]): BsonReader[T] =
    readDocumentWithDefault(path, schema, default).afterRead(_.get)

  override def optional[T](path: Path, schema: BsonReader[T], default: Option[Option[T]]): BsonReader[Option[T]] =
    readDocumentWithDefault[T](path, schema, default.flatten)

  override def verifying[T](schema: BsonReader[T], constraint: Constraint[T]): BsonReader[T] =
    schema.afterRead { parsed =>
      if (constraint.validate(parsed)) parsed
      else throw new RuntimeException(s"Constraint ${constraint.name} check failed for $parsed")
    }

  override def imap[A, B](fa: BsonReader[A])(f: A => B)(g: B => A): BsonReader[B] =
    fa.afterRead[B](f)

  override def or[A, B](fa: BsonReader[A], fb: BsonReader[B]): BsonReader[Either[A, B]] =
    BSONReader[BSONValue, Either[A, B]](bsonValue => fa.asInstanceOf[BSONReader[BSONValue, A]].readTry(bsonValue) match {
      case Success(a) => Left(a)
      case Failure(aThrowable) => fb.asInstanceOf[BSONReader[BSONValue, B]].readTry(bsonValue) match {
        case Success(b)          => Right(b)
        case Failure(bThrowable) => throw NoMatchException.product(aThrowable, bThrowable)
      }
    })

  override def product[A, B](fa: BsonReader[A], fb: BsonReader[B]): BsonReader[(A, B)] =
    BSONReader[BSONValue, (A, B)](value => (
      fa.asInstanceOf[BSONReader[BSONValue, A]].read(value),
      fb.asInstanceOf[BSONReader[BSONValue, B]].read(value)
    ))
}
