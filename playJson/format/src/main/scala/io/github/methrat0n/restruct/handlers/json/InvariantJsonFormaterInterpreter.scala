package io.github.methrat0n.restruct.handlers.json

import play.api.libs.json.Format
import io.github.methrat0n.restruct.core.data.schema.InvariantAlgebra
import io.github.methrat0n.restruct.readers.json.InvariantJsonReaderInterpreter
import io.github.methrat0n.restruct.writers.json.InvariantJsonWriterInterpreter

trait InvariantJsonFormaterInterpreter extends InvariantAlgebra[Format] {

  private[this] object InvariantReader extends InvariantJsonReaderInterpreter
  private[this] object InvariantWriter extends InvariantJsonWriterInterpreter

  override def imap[A, B](fa: Format[A])(f: A => B)(g: B => A): Format[B] =
    Format(
      InvariantReader.imap(fa)(f)(g),
      InvariantWriter.imap(fa)(f)(g)
    )
}
