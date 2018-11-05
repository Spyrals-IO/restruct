package io.github.methrat0n.restruct.writers.jsonschema

import cats.data.Const
import play.api.libs.json.Json
import io.github.methrat0n.restruct.core.data.schema.IdentityAlgebra
import io.github.methrat0n.restruct.writers.json.IdentityJsonWriterInterpreter

trait IdentityJsonSchemaWriterInterpreter extends IdentityAlgebra[JsonSchemaWriter] {

  private object JsonWriter extends IdentityJsonWriterInterpreter

  override def pure[T](a: T): JsonSchemaWriter[T] =
    (Const(Json.obj()), JsonWriter.pure(a))
}
