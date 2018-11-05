package io.github.methrat0n.restruct.writers.jsonschema

import cats.data.Const
import io.github.methrat0n.restruct.core.data.schema.InvariantAlgebra
import io.github.methrat0n.restruct.writers.json.InvariantJsonWriterInterpreter

trait InvariantJsonSchemaWriterInterpreter extends InvariantAlgebra[JsonSchemaWriter] {

  private object JsonWriter extends InvariantJsonWriterInterpreter

  override def imap[A, B](fa: JsonSchemaWriter[A])(f: A => B)(g: B => A): JsonSchemaWriter[B] =
    (Const(fa._1.getConst), JsonWriter.imap(fa._2)(f)(g))
}
