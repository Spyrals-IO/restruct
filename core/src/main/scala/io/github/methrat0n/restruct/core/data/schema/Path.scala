package io.github.methrat0n.restruct.core.data.schema

import io.github.methrat0n.restruct.schema.{ OptionalField, RequiredField, Schema }

final case class Path(steps: StepList) {

  def \(step: String): Path =
    Path(StepList(steps.head, steps.tail :+ StringStep(step)))
  def \(step: Int): Path =
    Path(StepList(steps.head, steps.tail :+ IntStep(step)))
  def as[A](implicit schema: Schema[A]): RequiredField[A] =
    RequiredField(this, schema, None)
  def asOption[A](implicit schema: Schema[A]): OptionalField[A] =
    OptionalField(this, schema, None)
}

object Path {
  def \(step: String): Path =
    Path(StepList(StringStep(step), List.empty))
  def \(step: Int): Path =
    Path(StepList(IntStep(step), List.empty))
}

sealed trait Step

final case class StringStep(step: String) extends Step
final case class IntStep(step: Int) extends Step

final case class StepList(head: Step, tail: List[Step]) {
  val toList: List[Step] =
    List(head) ++ tail
}
