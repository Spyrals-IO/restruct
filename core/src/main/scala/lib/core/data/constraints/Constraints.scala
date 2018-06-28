package lib.core.data.constraints

import enumeratum.Enum

object Constraints {
  final case class EnumConstraint[E <: Enum[_]](enum: E) extends Constraint[String] {
    override def name: String = "enum"

    override def args: Seq[Any] = Seq(enum.namesToValuesMap.keys.toList)

    override def validate(value: String): Boolean = enum.withNameOption(value).isDefined
  }

  final case class EqualConstraint[T](value: T) extends Constraint[T] {
    override def name: String = "equal"

    override def args: Seq[Any] = Seq(value)

    override def validate(other: T): Boolean = value == other
  }
}
