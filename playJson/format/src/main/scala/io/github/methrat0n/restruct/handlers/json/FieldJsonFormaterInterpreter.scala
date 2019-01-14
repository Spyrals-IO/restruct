package io.github.methrat0n.restruct.handlers.json

import io.github.methrat0n.restruct.core.data.constraints.Constraint
import io.github.methrat0n.restruct.core.data.schema.{ FieldAlgebra, Path }
import io.github.methrat0n.restruct.readers.json.jsonReads
import io.github.methrat0n.restruct.writers.json.jsonWrites
import play.api.libs.json.Format

trait FieldJsonFormaterInterpreter extends FieldAlgebra[Format] {

  private[this] val writer = jsonWrites
  private[this] val reader = jsonReads

  override def required[T](path: Path, schema: Format[T], default: Option[T]): Format[T] =
    Format(
      reader.required(path, schema, default),
      writer.required(path, schema, default)
    )

  override def verifying[T](schema: Format[T], constraint: Constraint[T]): Format[T] =
    Format(
      reader.verifying(schema, constraint),
      writer.verifying(schema, constraint)
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

  override def optional[T](path: Path, schema: Format[T], default: Option[Option[T]]): Format[Option[T]] =
    Format(
      reader.optional(path, schema, default),
      writer.optional(path, schema, default)
    )
}
