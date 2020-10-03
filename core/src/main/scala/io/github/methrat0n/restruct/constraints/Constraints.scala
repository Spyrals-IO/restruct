package io.github.methrat0n.restruct.constraints

object Constraints {
  final case class Equal[T](value: T) extends Constraint[T] {
    override def args: Seq[Any] = Seq(value)
    override def validate(other: T): Boolean = value == other
  }
}
