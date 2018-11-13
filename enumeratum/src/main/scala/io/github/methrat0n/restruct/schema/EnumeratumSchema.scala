package io.github.methrat0n.restruct.schema

import cats.implicits._
import enumeratum.{ Enum, EnumEntry }
import io.github.methrat0n.restruct.constraints.EnumeratumConstraints.EnumConstraint
import io.github.methrat0n.restruct.core.Program
import io.github.methrat0n.restruct.core.data.schema.FieldAlgebra

import scala.language.higherKinds

object EnumeratumSchema {
  def enum[E <: EnumEntry](enum: Enum[E]): Program[FieldAlgebra, E] = new Program[FieldAlgebra, E] {
    override def run[F[_]](implicit algebra: FieldAlgebra[F]): F[E] =
      algebra.verifying(algebra.stringSchema, EnumConstraint(enum)).imap(enum.withName)(_.entryName)
  }
}
