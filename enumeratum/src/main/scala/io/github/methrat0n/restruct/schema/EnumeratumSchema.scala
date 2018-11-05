package io.github.methrat0n.restruct.schema

import cats.implicits._
import enumeratum.{ Enum, EnumEntry }
import io.github.methrat0n.restruct.core.Program
import io.github.methrat0n.restruct.core.data.schema.ComplexSchemaAlgebra
import io.github.methrat0n.restruct.constraints.EnumeratumConstraints.EnumConstraint

import scala.language.higherKinds

object EnumeratumSchema {
  def enum[E <: EnumEntry](enum: Enum[E]): Program[ComplexSchemaAlgebra, E] = new Program[ComplexSchemaAlgebra, E] {
    override def run[F[_]](implicit algebra: ComplexSchemaAlgebra[F]): F[E] =
      algebra.verifying(algebra.stringSchema, EnumConstraint(enum)).imap(enum.withName)(_.entryName)
  }
}
