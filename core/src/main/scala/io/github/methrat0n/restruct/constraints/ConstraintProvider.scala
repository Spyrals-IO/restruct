package io.github.methrat0n.restruct.constraints

trait ConstraintProvider[T] {
  def apply(): List[Constraint[T]]
}

object ConstraintProvider {
  //TODO add json schema constraint
}
