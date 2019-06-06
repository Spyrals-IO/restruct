package io.github.methrat0n.restruct.schema

import scala.language.higherKinds

sealed trait Field[P <: Path, Type] extends Schema[Type] {
  def path: Path
  def default: Option[Type]
  def defaultTo(default: Type): Field[P, Type]
}

final case class OptionalField[P <: Path, Type](path: P, part: Schema[Type], default: Option[Option[Type]]) extends Field[P, Option[Type]] {
  override def defaultTo(default: Option[Type]): Field[P, Option[Type]] = copy(default = Some(default))
  override type OwnInterpreter[Format[_]] = OptionalInterpreter[Format, P, Type]
  override def bind[Format[_]](implicit algebra: OptionalInterpreter[Format, P, Type]): Format[Option[Type]] =
    algebra.optional(path, part.secretBind[Format](algebra.originalInterpreter), default)
}

final case class RequiredField[P <: Path, Type](path: P, part: Schema[Type], default: Option[Type]) extends Field[P, Type] {
  override def defaultTo(default: Type): Field[P, Type] = copy(default = Some(default))
  override type OwnInterpreter[Format[_]] = RequiredInterpreter[Format, P, Type]
  override def bind[Format[_]](implicit algebra: RequiredInterpreter[Format, P, Type]): Format[Type] =
    algebra.required(path, part.secretBind[Format](algebra), default)
}
