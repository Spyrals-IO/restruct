package io.github.methrat0n.restruct.handlers.json

import play.api.libs.json.Format
import io.github.methrat0n.restruct.core.data.schema.IdentityAlgebra
import io.github.methrat0n.restruct.readers.json.IdentityJsonReaderInterpreter
import io.github.methrat0n.restruct.writers.json.IdentityJsonWriterInterpreter

trait IdentityJsonFormaterInterpreter extends IdentityAlgebra[Format] {

  private object IdentityReader extends IdentityJsonReaderInterpreter
  private object IdentityWriter extends IdentityJsonWriterInterpreter

  override def pure[T](t: T): Format[T] =
    Format(
      IdentityReader.pure(t),
      IdentityWriter.pure(t)
    )
}
