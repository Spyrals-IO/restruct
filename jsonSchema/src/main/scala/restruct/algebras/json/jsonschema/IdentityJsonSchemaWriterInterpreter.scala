package restruct.algebras.json.jsonschema

import cats.data.Const
import play.api.libs.json.Json
import restruct.algebras.json.playjson.writes.IdentityJsonWriterInterpreter
import restruct.core.data.schema.IdentityAlgebra

trait IdentityJsonSchemaWriterInterpreter extends IdentityAlgebra[JsonSchemaWriter] {

  private object JsonWriter extends IdentityJsonWriterInterpreter

  override def pure[T](a: T): JsonSchemaWriter[T] =
    (Const(Json.obj()), JsonWriter.pure(a))
}
