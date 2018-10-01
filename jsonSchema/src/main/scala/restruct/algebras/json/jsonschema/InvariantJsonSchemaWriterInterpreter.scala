package restruct.algebras.json.jsonschema

import cats.data.Const
import restruct.algebras.json.playjson.writes.InvariantJsonWriterInterpreter
import restruct.core.data.schema.InvariantAlgebra

trait InvariantJsonSchemaWriterInterpreter extends InvariantAlgebra[JsonSchemaWriter] {

  private object JsonWriter extends InvariantJsonWriterInterpreter

  override def imap[A, B](fa: JsonSchemaWriter[A])(f: A => B)(g: B => A): JsonSchemaWriter[B] =
    (Const(fa._1.getConst), JsonWriter.imap(fa._2)(f)(g))
}
