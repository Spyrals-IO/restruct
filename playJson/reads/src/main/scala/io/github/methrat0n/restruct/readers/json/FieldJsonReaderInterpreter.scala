package io.github.methrat0n.restruct.readers.json

import io.github.methrat0n.restruct.core.data.constraints.Constraint
import io.github.methrat0n.restruct.core.data.schema.{ FieldAlgebra, IntStep, Path, StringStep }
import play.api.libs.json._

trait FieldJsonReaderInterpreter extends FieldAlgebra[Reads] {
  override def required[T](path: Path, schema: Reads[T], default: Option[T]): Reads[T] =
    default
      .map(default => path2JsPath(path).readWithDefault(default)(schema))
      .getOrElse(path2JsPath(path).read(schema))

  override def optional[T](path: Path, schema: Reads[T], default: Option[Option[T]]): Reads[Option[T]] =
    path2JsPath(path).readNullableWithDefault(default.flatten)(schema)

  override def verifying[T](schema: Reads[T], constraint: Constraint[T]): Reads[T] =
    schema.filter(JsonValidationError(s"error.constraints.${constraint.name}", constraint.args: _*))(constraint.validate)

  override def pure[T](a: T): Reads[T] = Reads.pure(a)

  override def imap[A, B](fa: Reads[A])(f: A => B)(g: B => A): Reads[B] =
    fa.map(f)

  override def either[A, B](a: Reads[A], b: Reads[B]): Reads[Either[A, B]] =
    a.map[Either[A, B]](Left.apply).orElse(b.map[Either[A, B]](Right.apply))

  import play.api.libs.functional.syntax._
  override def product[A, B](fa: Reads[A], fb: Reads[B]): Reads[(A, B)] =
    (fa and fb).tupled

  private[this] def path2JsPath(path: Path): JsPath =
    JsPath(path.steps.toList.map {
      case StringStep(step) => KeyPathNode(step)
      case IntStep(step)    => IdxPathNode(step)
    })
}
