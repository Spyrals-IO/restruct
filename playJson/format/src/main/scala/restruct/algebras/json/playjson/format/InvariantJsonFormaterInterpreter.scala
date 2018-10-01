package restruct.algebras.json.playjson.format

import play.api.libs.json.Format
import restruct.algebras.json.playjson.reads.InvariantJsonReaderInterpreter
import restruct.algebras.json.playjson.writes.InvariantJsonWriterInterpreter
import restruct.core.data.schema.InvariantAlgebra

private[playjson] trait InvariantJsonFormaterInterpreter extends InvariantAlgebra[Format] {

  private[this] object InvariantReader extends InvariantJsonReaderInterpreter
  private[this] object InvariantWriter extends InvariantJsonWriterInterpreter

  override def imap[A, B](fa: Format[A])(f: A => B)(g: B => A): Format[B] =
    Format(
      InvariantReader.imap(fa)(f)(g),
      InvariantWriter.imap(fa)(f)(g)
    )
}
