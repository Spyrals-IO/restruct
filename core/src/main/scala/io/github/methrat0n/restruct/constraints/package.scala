package io.github.methrat0n.restruct

import io.github.methrat0n.restruct.core.data.constraints.Constraint

package object constraints {
  def minSize[A](min: Int): Constraint[A] = new Constraint[A] {
    override def args: Seq[Any] = List()

    override def validate(value: A): Boolean = min == min && value == value //placeholder
  }
}
