package restruct.algebras.json.playjson.reads

import play.api.libs.json.Reads
import restruct.core.data.schema.IdentityAlgebra

private[playjson] trait IdentityJsonReaderInterpreter extends IdentityAlgebra[Reads] {
  override def pure[T](a: T): Reads[T] = Reads.pure(a)
}
