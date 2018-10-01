package restruct.algebras.json.playjson.format

import play.api.libs.json.Format
import restruct.algebras.json.playjson.reads.SemiGroupalJsonReaderInterpreter
import restruct.algebras.json.playjson.writes.SemiGroupalJsonWriterInterpreter
import restruct.core.data.schema.SemiGroupalAlgebra

private[playjson] trait SemiGroupalJsonFormaterInterpreter extends SemiGroupalAlgebra[Format] {

  private[this] object SemiGroupalReader extends SemiGroupalJsonReaderInterpreter
  private[this] object SemiGroupalWriter extends SemiGroupalJsonWriterInterpreter

  override def either[A, B](a: Format[A], b: Format[B]): Format[Either[A, B]] =
    Format(
      SemiGroupalReader.either(a, b),
      SemiGroupalWriter.either(a, b)
    )

  override def product[A, B](fa: Format[A], fb: Format[B]): Format[(A, B)] =
    Format(
      SemiGroupalReader.product(fa, fb),
      SemiGroupalWriter.product(fa, fb)
    )
}
