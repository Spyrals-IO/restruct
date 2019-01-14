package io.github.methrat0n.restruct.readers.bson

import io.github.methrat0n.restruct.core.data.constraints.Constraint
import io.github.methrat0n.restruct.core.data.schema.{ FieldAlgebra, Path }
import reactivemongo.bson.{ BSONReader, BSONValue }

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

  override def either[A, B](fa: BsonReader[A], fb: BsonReader[B]): BsonReader[Either[A, B]] =
    BSONReader[BSONValue, Either[A, B]](bsonValue => fa.asInstanceOf[BSONReader[BSONValue, A]].readOpt(bsonValue) match {
      case Some(a) => Left(a)
      case None    => Right(fb.asInstanceOf[BSONReader[BSONValue, B]].read(bsonValue))
    })

  override def product[A, B](fa: BsonReader[A], fb: BsonReader[B]): BsonReader[(A, B)] =
    BSONReader[BSONValue, (A, B)](value => (
      fa.asInstanceOf[BSONReader[BSONValue, A]].read(value),
      fb.asInstanceOf[BSONReader[BSONValue, B]].read(value)
    ))
}
