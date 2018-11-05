package io.github.methrat0n.restruct.handlers.json

import play.api.libs.json.Format
import io.github.methrat0n.restruct.core.data.schema.SemiGroupalAlgebra
import io.github.methrat0n.restruct.readers.json.SemiGroupalJsonReaderInterpreter
import io.github.methrat0n.restruct.writers.json.SemiGroupalJsonWriterInterpreter

trait SemiGroupalJsonFormaterInterpreter extends SemiGroupalAlgebra[Format] {

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
