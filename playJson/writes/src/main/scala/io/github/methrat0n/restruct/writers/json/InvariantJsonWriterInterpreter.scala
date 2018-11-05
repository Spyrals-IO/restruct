package io.github.methrat0n.restruct.writers.json

import play.api.libs.json.Writes
import io.github.methrat0n.restruct.core.data.schema.InvariantAlgebra

import play.api.libs.functional.syntax._

trait InvariantJsonWriterInterpreter extends InvariantAlgebra[Writes] {
  override def imap[A, B](fa: Writes[A])(f: A => B)(g: B => A): Writes[B] =
    fa.contramap(g)
}
