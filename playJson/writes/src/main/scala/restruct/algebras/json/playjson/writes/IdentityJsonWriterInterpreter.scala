package restruct.algebras.json.playjson.writes

import play.api.libs.json.{ Json, Writes }
import restruct.core.data.schema.IdentityAlgebra

trait IdentityJsonWriterInterpreter extends IdentityAlgebra[Writes] {
  override def pure[T](t: T): Writes[T] =
    Writes(_ => Json.obj())
}
