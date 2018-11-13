package io.github.methrat0n.restruct.readers.bson

import io.github.methrat0n.restruct.core.data.constraints.Constraint
import io.github.methrat0n.restruct.core.data.schema.FieldAlgebra
import reactivemongo.bson.{ BSONReader, BSONValue }

trait FieldBsonReaderInterpreter extends FieldAlgebra[BsonReader] {

  override def required[T](name: String, schema: BsonReader[T], default: Option[T]): BsonReader[T] =
    readDocumentWithDefault(name, schema, default).afterRead(_.get)

  override def optional[T](name: String, schema: BsonReader[T], default: Option[Option[T]]): BsonReader[Option[T]] =
    readDocumentWithDefault[T](name, schema, default.flatten)

  override def verifying[T](schema: BsonReader[T], constraint: Constraint[T]): BsonReader[T] =
    schema.afterRead { parsed =>
      if (constraint.validate(parsed)) parsed
      else throw new RuntimeException(s"Constraint ${constraint.name} check failed for $parsed")
    }

  override def imap[A, B](fa: BsonReader[A])(f: A => B)(g: B => A): BsonReader[B] =
    fa.afterRead[B](f)

  override def pure[T](t: T): BsonReader[T] =
    BsonReader[T](_ => t)

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
