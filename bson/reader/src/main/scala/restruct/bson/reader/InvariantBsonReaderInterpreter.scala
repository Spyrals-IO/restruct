package restruct.bson.reader

import restruct.core.data.schema.InvariantAlgebra

trait InvariantBsonReaderInterpreter extends InvariantAlgebra[BsonReader] {
  override def imap[A, B](fa: BsonReader[A])(f: A => B)(g: B => A): BsonReader[B] =
    fa.afterRead[B](f)
}
