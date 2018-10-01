package restruct.algebras.json.playjson.reads

import play.api.libs.json.Reads
import restruct.core.data.schema.InvariantAlgebra

private[playjson] trait InvariantJsonReaderInterpreter extends InvariantAlgebra[Reads] {
  override def imap[A, B](fa: Reads[A])(f: A => B)(g: B => A): Reads[B] =
    fa.map(f)
}
