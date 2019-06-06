package io.github.methrat0n.restruct.schema

import io.github.methrat0n.restruct.schema.Path.\

sealed trait Path

final case class PathCon[PreviousSteps <: Path, Step](previousSteps: PreviousSteps, step: Step) extends Path { self =>
  def \(step: String): PreviousSteps \ Step \ String =
    PathCon(self, step)
  def \(step: Int): PreviousSteps \ Step \ Int =
    PathCon(self, step)
  def as[T](implicit schema: Schema[T]): RequiredField[PreviousSteps \ Step, T] =
    RequiredField(self, schema, None)
  def asOption[T](implicit schema: Schema[T]): OptionalField[PreviousSteps \ Step, T] =
    OptionalField(self, schema, None)
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
