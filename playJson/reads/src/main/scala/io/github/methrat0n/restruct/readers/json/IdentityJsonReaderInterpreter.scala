package io.github.methrat0n.restruct.readers.json

import play.api.libs.json.Reads
import io.github.methrat0n.restruct.core.data.schema.IdentityAlgebra

trait IdentityJsonReaderInterpreter extends IdentityAlgebra[Reads] {
  override def pure[T](a: T): Reads[T] = Reads.pure(a)
}
