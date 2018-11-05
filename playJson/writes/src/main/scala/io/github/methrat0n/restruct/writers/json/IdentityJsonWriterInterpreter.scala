package io.github.methrat0n.restruct.writers.json

import play.api.libs.json.{ Json, Writes }
import io.github.methrat0n.restruct.core.data.schema.IdentityAlgebra

trait IdentityJsonWriterInterpreter extends IdentityAlgebra[Writes] {
  override def pure[T](t: T): Writes[T] =
    Writes(_ => Json.obj())
}
