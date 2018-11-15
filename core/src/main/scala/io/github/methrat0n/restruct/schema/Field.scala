package io.github.methrat0n.restruct.schema

import io.github.methrat0n.restruct.core.Program
import io.github.methrat0n.restruct.core.data.schema.{ FieldAlgebra, Path }

import language.higherKinds

sealed trait Field[Type] {
  def path: Path
  def default: Option[Type]
  def defaultTo(default: Type): Field[Type]
  def program: Program[FieldAlgebra, Type]
}

final case class OptionalField[Type](path: Path, schema: Schema[Type], default: Option[Option[Type]]) extends Field[Option[Type]] {
  override val program: Program[FieldAlgebra, Option[Type]] = new Program[FieldAlgebra, Option[Type]] {
    override def run[F[_]](implicit algebra: FieldAlgebra[F]): F[Option[Type]] =
      algebra.optional(path, schema.bind(algebra), default)
  }

  override def defaultTo(default: Option[Type]): Field[Option[Type]] = copy(default = Some(default))
}
final case class RequiredField[Type](path: Path, schema: Schema[Type], default: Option[Type]) extends Field[Type] {
  override def program: Program[FieldAlgebra, Type] = new Program[FieldAlgebra, Type] {
    override def run[F[_]](implicit algebra: FieldAlgebra[F]): F[Type] =
      algebra.required(path, schema.bind(algebra), default)
  }

  override def defaultTo(default: Type): Field[Type] = copy(default = Some(default))
}
