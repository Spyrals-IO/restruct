package io.github.methrat0n.restruct.core.data.schema

import cats.data.NonEmptyList
import io.github.methrat0n.restruct.schema.{ OptionalField, RequiredField, Schema }

final case class Path(steps: NonEmptyList[Step]) {

  def \(step: String): Path =
    Path(NonEmptyList(steps.head, steps.tail :+ StringStep(step)))
  def \(step: Int): Path =
    Path(NonEmptyList(steps.head, steps.tail :+ IntStep(step)))
  def as[A](implicit schema: Schema[A]): RequiredField[A] =
    RequiredField(this, schema, None)
  def asOption[A](implicit schema: Schema[A]): OptionalField[A] =
    OptionalField(this, schema, None)
}

sealed trait Step

final case class StringStep(step: String) extends Step
final case class IntStep(step: Int) extends Step

