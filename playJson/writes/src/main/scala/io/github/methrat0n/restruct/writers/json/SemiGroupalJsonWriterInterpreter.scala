package io.github.methrat0n.restruct.writers.json

import play.api.libs.json.{ JsArray, JsObject, Writes }
import io.github.methrat0n.restruct.core.data.schema.SemiGroupalAlgebra

trait SemiGroupalJsonWriterInterpreter extends SemiGroupalAlgebra[Writes] {
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
}
