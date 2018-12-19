package io.github.methrat0n.restruct.schema

import io.github.methrat0n.restruct.core.data.schema.{ FieldAlgebra, Path }

import language.higherKinds

sealed trait Field[Type] extends Schema[Type] {
  def path: Path
  def default: Option[Type]
  def defaultTo(default: Type): Field[Type]
}

final case class OptionalField[Type](path: Path, part: Schema[Type], default: Option[Option[Type]]) extends Field[Option[Type]] {
  override def defaultTo(default: Option[Type]): Field[Option[Type]] = copy(default = Some(default))
  override def bind[FORMAT[_]](algebra: FieldAlgebra[FORMAT]): FORMAT[Option[Type]] =
    algebra.optional[Type](path, part.bind(algebra), default)
}
final case class RequiredField[Type](path: Path, part: Schema[Type], default: Option[Type]) extends Field[Type] {
  override def defaultTo(default: Type): Field[Type] = copy(default = Some(default))
  override def bind[FORMAT[_]](algebra: FieldAlgebra[FORMAT]): FORMAT[Type] =
    algebra.required(path, part.bind(algebra), default)
}
