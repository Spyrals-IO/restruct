package io.github.methrat0n.restruct.handlers.json

import io.github.methrat0n.restruct.core.data.constraints.Constraint
import io.github.methrat0n.restruct.core.data.schema.FieldAlgebra
import io.github.methrat0n.restruct.readers.json.JsonReaderInterpreter
import io.github.methrat0n.restruct.writers.json.JsonWriterInterpreter
import play.api.libs.json.Format

trait FieldJsonFormaterInterpreter extends FieldAlgebra[Format] {

  private[this] val writer = JsonWriterInterpreter
  private[this] val reader = JsonReaderInterpreter

  override def required[T](name: String, schema: Format[T], default: Option[T]): Format[T] =
    Format(
      reader.required(name, schema, default),
      writer.required(name, schema, default)
    )

  override def verifying[T](schema: Format[T], constraint: Constraint[T]): Format[T] =
    Format(
      reader.verifying(schema, constraint),
      writer.verifying(schema, constraint)
    )

  override def pure[T](t: T): Format[T] =
    Format(
      reader.pure(t),
      writer.pure(t)
    )

  override def imap[A, B](fa: Format[A])(f: A => B)(g: B => A): Format[B] =
    Format(
      reader.imap(fa)(f)(g),
      writer.imap(fa)(f)(g)
    )

  override def either[A, B](a: Format[A], b: Format[B]): Format[Either[A, B]] =
    Format(
      reader.either(a, b),
      writer.either(a, b)
    )

  override def product[A, B](fa: Format[A], fb: Format[B]): Format[(A, B)] =
    Format(
      reader.product(fa, fb),
      writer.product(fa, fb)
    )

  override def optional[T](name: String, schema: Format[T], default: Option[Option[T]]): Format[Option[T]] =
    Format(
      reader.optional(name, schema, default),
      writer.optional(name, schema, default)
    )
}
