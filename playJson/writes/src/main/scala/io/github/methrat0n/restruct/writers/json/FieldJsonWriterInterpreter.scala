package io.github.methrat0n.restruct.writers.json

import io.github.methrat0n.restruct.core.data.constraints.Constraint
import io.github.methrat0n.restruct.core.data.schema.FieldAlgebra
import play.api.libs.json._

trait FieldJsonWriterInterpreter extends FieldAlgebra[Writes] {
  override def required[T](name: String, schema: Writes[T], default: Option[T]): Writes[T] =
    (JsPath \ name).write(schema)

  override def optional[T](name: String, schema: Writes[T], default: Option[Option[T]]): Writes[Option[T]] =
    (JsPath \ name).writeNullable(schema)

  override def verifying[T](schema: Writes[T], constraint: Constraint[T]): Writes[T] =
    schema

  override def either[A, B](a: Writes[A], b: Writes[B]): Writes[Either[A, B]] =
    Writes {
      case Left(input)  => a.writes(input)
      case Right(input) => b.writes(input)
    }

  override def product[A, B](fa: Writes[A], fb: Writes[B]): Writes[(A, B)] =
    (o: (A, B)) => (fa.writes(o._1), fb.writes(o._2)) match {
      case (a @ JsObject(_), b @ JsObject(_)) => a ++ b
      case (a @ JsArray(_), b @ JsArray(_))   => a ++ b
      case (selected, _)                      => selected
    }

  override def pure[T](t: T): Writes[T] =
    Writes(_ => Json.obj())

  import play.api.libs.functional.syntax._
  override def imap[A, B](fa: Writes[A])(f: A => B)(g: B => A): Writes[B] =
    fa.contramap(g)
}
