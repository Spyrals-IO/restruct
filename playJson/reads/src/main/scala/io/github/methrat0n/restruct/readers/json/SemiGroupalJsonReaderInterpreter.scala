package io.github.methrat0n.restruct.readers.json

import play.api.libs.json.Reads
import play.api.libs.functional.syntax._
import io.github.methrat0n.restruct.core.data.schema.SemiGroupalAlgebra

trait SemiGroupalJsonReaderInterpreter extends SemiGroupalAlgebra[Reads] {
  override def either[A, B](a: Reads[A], b: Reads[B]): Reads[Either[A, B]] =
    a.map[Either[A, B]](Left.apply).orElse(b.map[Either[A, B]](Right.apply))

  override def product[A, B](fa: Reads[A], fb: Reads[B]): Reads[(A, B)] =
    (fa and fb).tupled
}
