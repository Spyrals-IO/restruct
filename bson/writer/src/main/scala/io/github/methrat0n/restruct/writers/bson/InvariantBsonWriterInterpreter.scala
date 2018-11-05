package io.github.methrat0n.restruct.writers.bson

import io.github.methrat0n.restruct.core.data.schema.InvariantAlgebra

trait InvariantBsonWriterInterpreter extends InvariantAlgebra[BsonWriter] {
  override def imap[A, B](fa: BsonWriter[A])(f: A => B)(g: B => A): BsonWriter[B] =
    fa.beforeWrite[B](g)
}
