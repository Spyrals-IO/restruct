package restruct.algebras.json.playjson.format

import play.api.libs.json.Format
import restruct.algebras.json.playjson.reads.IdentityJsonReaderInterpreter
import restruct.algebras.json.playjson.writes.IdentityJsonWriterInterpreter
import restruct.core.data.schema.IdentityAlgebra

private[playjson] trait IdentityJsonFormaterInterpreter extends IdentityAlgebra[Format] {

  private[this] object IdentityReader extends IdentityJsonReaderInterpreter
  private[this] object IdentityWriter extends IdentityJsonWriterInterpreter

  override def pure[T](t: T): Format[T] =
    Format(
      IdentityReader.pure(t),
      IdentityWriter.pure(t)
    )
}
