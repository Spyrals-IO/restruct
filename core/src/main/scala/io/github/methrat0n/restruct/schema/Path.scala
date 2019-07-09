package io.github.methrat0n.restruct.schema

import io.github.methrat0n.restruct.schema.Interpreter.ManyInterpreter
import io.github.methrat0n.restruct.schema.Path.\
import io.github.methrat0n.restruct.schema.Schemas.ManySchema

sealed trait Path

final case class PathCon[PreviousSteps <: Path, Step](previousSteps: PreviousSteps, step: Step) extends Path { self =>
  def \(step: String): PreviousSteps \ Step \ String =
    PathCon(self, step)
  def \(step: Int): PreviousSteps \ Step \ Int =
    PathCon(self, step)
  def as[Type]: SchemeInferer[Type] =
    new SchemeInferer[Type]

  //TODO remove and use Schema.many implicit in as
  def many[Type, Collection[A] <: Iterable[A]]: ManySchemeInferer[Type, Collection] =
    new ManySchemeInferer[Type, Collection]

  final class ManySchemeInferer[Type, Collection[A] <: Iterable[A]] {
    def apply[TypeInterpreter[Format[_]] <: Interpreter[Format, Type]]()(
      implicit
      schema: Schema[Type, TypeInterpreter]
    ): RequiredField[PreviousSteps \ Step, Collection[Type], λ[Format[_] => ManyInterpreter[Format, Type, Collection, TypeInterpreter[Format]]]] =
      RequiredField(self, new ManySchema[Collection, Type, TypeInterpreter](schema), None)
  }

  def asOption[Type]: OptionalSchemeInfere[Type] =
    new OptionalSchemeInfere[Type]

  final class SchemeInferer[Type] {
    def apply[TypeInterpreter[Format[_]] <: Interpreter[Format, Type]]()(
      implicit
      schema: Schema[Type, TypeInterpreter]
    ): RequiredField[PreviousSteps \ Step, Type, TypeInterpreter] =
      RequiredField(self, schema, None)
  }

  final class OptionalSchemeInfere[Type] {
    def apply[TypeInterpreter[Format[_]] <: Interpreter[Format, Type]]()(
      implicit
      schema: Schema[Type, TypeInterpreter]
    ): OptionalField[PreviousSteps \ Step, Type, TypeInterpreter] =
      OptionalField[PreviousSteps \ Step, Type, TypeInterpreter](self, schema, None)
  }
}

sealed trait PathNil extends Path
case object PathNil extends PathNil

object Path {
  def \(step: String): PathCon[PathNil, String] = PathCon(PathNil, step)
  def \(step: Int): PathCon[PathNil, Int] = PathCon(PathNil, step)

  type \[PreviousSteps <: Path, Step] = PathCon[PreviousSteps, Step]
  object \ {
    def apply[PreviousSteps <: Path, Step](previousSteps: PreviousSteps, step: Step): PreviousSteps \ Step = PathCon(previousSteps, step)
    def apply[Step](step: Step): PathNil \ Step = PathCon(PathNil, step)
  }
}
