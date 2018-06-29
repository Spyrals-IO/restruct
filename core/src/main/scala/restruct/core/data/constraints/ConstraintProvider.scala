package restruct.core.data.constraints

trait ConstraintProvider[T] {
  def apply(): List[Constraint[T]]
}

object ConstraintProvider {
  //TODO add json schema constraint
}
