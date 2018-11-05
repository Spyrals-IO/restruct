package io.github.methrat0n.restruct.core.data.constraints

object Constraints {
  final case class EqualConstraint[T](value: T) extends Constraint[T] {
    override def name: String = "equal"

    override def args: Seq[Any] = Seq(value)

    override def validate(other: T): Boolean = value == other
  }
}
