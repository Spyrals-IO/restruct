package lib.refined.data.constraints

import lib.core.data.constraints.{ Constraint, ConstraintProvider }
import shapeless.Witness
import eu.timepit.refined.api.{ Refined, Validate }
import eu.timepit.refined.collection.MinSize
import eu.timepit.refined.numeric.{ Greater, GreaterEqual, Interval }
import eu.timepit.refined.string.MatchesRegex

object RefinedConstraintProvider {
  //TODO add all Refined type base constraint
  implicit def greaterConstraintProvider[T, N](implicit witness: Witness.Aux[N], validator: Validate[T, Greater[N]]): ConstraintProvider[Refined[T, Greater[N]]] = () => List(
    new Constraint[Refined[T, Greater[N]]] {
      override def name: String = "exclusiveMinimum"

      override def args: Seq[Any] = Seq(witness.value)

      override def validate(value: Refined[T, Greater[N]]): Boolean = validator.isValid(value.value)
    }
  )

  implicit def greaterEqualConstraintProvider[T, N](implicit witness: Witness.Aux[N], validator: Validate[T, Greater[N]]): ConstraintProvider[Refined[T, GreaterEqual[N]]] = () => List(
    new Constraint[Refined[T, GreaterEqual[N]]] {
      override def name: String = "minimum"

      override def args: Seq[Any] = List(witness.value)

      override def validate(value: Refined[T, GreaterEqual[N]]): Boolean = validator.isValid(value.value)
    }
  )

  implicit def minSizeConstaintProvider[T, N](implicit witness: Witness.Aux[N], validator: Validate[T, MinSize[N]]): ConstraintProvider[Refined[T, MinSize[N]]] = () => List(
    new Constraint[Refined[T, MinSize[N]]] {
      override def name: String = "minItems"

      override def args: Seq[Any] = List(witness.value)

      override def validate(value: Refined[T, MinSize[N]]): Boolean = validator.isValid(value.value)
    }
  )

  implicit def matchRegexConstaintProvider[T, REGEX](implicit witness: Witness.Aux[REGEX], validator: Validate[T, MatchesRegex[REGEX]]): ConstraintProvider[Refined[T, MatchesRegex[REGEX]]] = () => List(
    new Constraint[Refined[T, MatchesRegex[REGEX]]] {
      override def name: String = "pattern"

      override def args: Seq[Any] = List(witness.value)

      override def validate(value: Refined[T, MatchesRegex[REGEX]]): Boolean = validator.isValid(value.value)
    }
  )

  implicit def intervalClosedConstaintProvider[T, MIN, MAX](implicit witnessMin: Witness.Aux[MIN], witnessMax: Witness.Aux[MAX], validator: Validate[T, Interval.Closed[MIN, MAX]]): ConstraintProvider[Refined[T, Interval.Closed[MIN, MAX]]] = () => List(
    new Constraint[Refined[T, Interval.Closed[MIN, MAX]]] {
      override def name: String = "minimum"

      override def args: Seq[Any] = List(witnessMin.value)

      override def validate(value: Refined[T, Interval.Closed[MIN, MAX]]): Boolean = validator.isValid(value.value)
    },
    new Constraint[Refined[T, Interval.Closed[MIN, MAX]]] {
      override def name: String = "maximum"

      override def args: Seq[Any] = List(witnessMax.value)

      override def validate(value: Refined[T, Interval.Closed[MIN, MAX]]): Boolean = validator.isValid(value.value)
    }
  )
}
