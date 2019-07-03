package io.github.methrat0n.restruct.schema

import io.github.methrat0n.restruct.schema.Interpreter.{ OptionalInterpreter, RequiredInterpreter }

final case class OptionalField[P <: Path, Type, TypeInterpreter[Format[_]] <: Interpreter[Format, Type], Scheme <: Schema[Type, TypeInterpreter]](path: P, part: Scheme, default: Option[Option[Type]]) extends Schema[Option[Type], λ[Format[_] => OptionalInterpreter[Format, P, Type, TypeInterpreter[Format]]]] {
  //def defaultTo(default: Option[Type]): Field[P, Option[Type]] = copy(default = Some(default))
  def bind[Format[_]](implicit algebra: OptionalInterpreter[Format, P, Type, TypeInterpreter[Format]]): Format[Option[Type]] =
    algebra.optional(path, part.bind[Format](algebra.originalInterpreter), default)
}

final case class RequiredField[P <: Path, Type, TypeInterpreter[Format[_]] <: Interpreter[Format, Type]](path: P, part: Schema[Type, TypeInterpreter], default: Option[Type]) extends Schema[Type, λ[Format[_] => RequiredInterpreter[Format, P, Type, TypeInterpreter[Format]]]] {
  //override def defaultTo(default: Type): Field[P, Type] = copy(default = Some(default))
  override def bind[Format[_]](implicit algebra: RequiredInterpreter[Format, P, Type, TypeInterpreter[Format]]): Format[Type] =
    algebra.required(path, part.bind[Format](algebra.originalInterpreter), default)
}
