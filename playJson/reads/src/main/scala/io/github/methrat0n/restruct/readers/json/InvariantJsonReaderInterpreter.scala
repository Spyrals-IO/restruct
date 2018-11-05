package io.github.methrat0n.restruct.readers.json

import play.api.libs.json.Reads
import io.github.methrat0n.restruct.core.data.schema.InvariantAlgebra

trait InvariantJsonReaderInterpreter extends InvariantAlgebra[Reads] {
  override def imap[A, B](fa: Reads[A])(f: A => B)(g: B => A): Reads[B] =
    fa.map(f)
}
