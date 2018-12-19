package io.github.methrat0n.restruct.schema

import enumeratum.{ Enum, EnumEntry }
import io.github.methrat0n.restruct.constraints.EnumeratumConstraints.EnumConstraint
import io.github.methrat0n.restruct.core.data.schema.FieldAlgebra

import scala.language.higherKinds

object EnumeratumSchema {
  def enum[E <: EnumEntry](enum: Enum[E]): Schema[E] = new Schema[E] {
    override def bind[FORMAT[_]](algebra: FieldAlgebra[FORMAT]): FORMAT[E] =
      algebra.imap(algebra.verifying(algebra.stringSchema, EnumConstraint(enum)))(enum.withName)(_.entryName)
  }
}
