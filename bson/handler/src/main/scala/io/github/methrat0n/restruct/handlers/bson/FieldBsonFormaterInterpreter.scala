package io.github.methrat0n.restruct.handlers.bson

import io.github.methrat0n.restruct.core.data.constraints.Constraint
import io.github.methrat0n.restruct.core.data.schema.{ FieldAlgebra, Path }
import io.github.methrat0n.restruct.readers.bson.bsonReader
import io.github.methrat0n.restruct.writers.bson.bsonWriter

trait FieldBsonFormaterInterpreter extends FieldAlgebra[BsonHandler] {

  private[this] val reader = bsonReader
  private[this] val writer = bsonWriter

  override def optional[T](path: Path, schema: BsonHandler[T], default: Option[Option[T]]): BsonHandler[Option[T]] =
    BsonHandler(
      reader.optional[T](path, schema, default).read,
      writer.optional[T](path, schema, default).write
    )

  override def required[T](path: Path, schema: BsonHandler[T], default: Option[T]): BsonHandler[T] =
    BsonHandler(
      reader.required[T](path, schema, default).read,
      writer.required[T](path, schema, default).write
    )

  override def verifying[T](schema: BsonHandler[T], constraint: Constraint[T]): BsonHandler[T] =
    BsonHandler(
      reader.verifying[T](schema, constraint).read,
      writer.verifying[T](schema, constraint).write
    )

  override def imap[A, B](fa: BsonHandler[A])(f: A => B)(g: B => A): BsonHandler[B] =
    BsonHandler(
      reader.imap(fa)(f)(g).read,
      writer.imap(fa)(f)(g).write
    )

  override def pure[T](t: T): BsonHandler[T] =
    BsonHandler(
      reader.pure(t).read,
      writer.pure(t).write
    )

  override def either[A, B](a: BsonHandler[A], b: BsonHandler[B]): BsonHandler[Either[A, B]] =
    BsonHandler(
      reader.either(a, b).read,
      writer.either(a, b).write
    )

  override def product[A, B](fa: BsonHandler[A], fb: BsonHandler[B]): BsonHandler[(A, B)] =
    BsonHandler(
      reader.product(fa, fb).read,
      writer.product(fa, fb).write
    )
}
