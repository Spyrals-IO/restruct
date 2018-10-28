package restruct

import restruct.core.data.constraints.Constraint

package object constraints {
  def minSize[A](min: Int): Constraint[A] = new Constraint[A] {
    override def args: Seq[Any] = List()

    override def validate(value: A): Boolean = min == min && value == value //placeholder
  }
}
