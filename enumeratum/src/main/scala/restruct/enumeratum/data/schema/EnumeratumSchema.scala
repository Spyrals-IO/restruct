package restruct.enumeratum.data.schema

import enumeratum.{Enum, EnumEntry}
import cats.implicits._
import lib.core.Program
import restruct.core.Program
import restruct.core.data.schema.SchemaAlgebra
import restruct.enumeratum.data.constraints.EnumeratumConstraints.EnumConstraint

import language.higherKinds

object EnumeratumSchema {
  def enum[E <: EnumEntry](enum: Enum[E]): Program[SchemaAlgebra, E] = new Program[SchemaAlgebra, E] {
    override def run[F[_]](implicit algebra: SchemaAlgebra[F]): F[E] =
      algebra.verifying(algebra.stringSchema, EnumConstraint(enum)).imap(enum.withName)(_.entryName)
  }
}
