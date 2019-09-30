package io.github.methrat0n.restruct.schema

import enumeratum.{ Enum, EnumEntry }
import io.github.methrat0n.restruct.constraints.EnumeratumConstraints.EnumConstraint
import io.github.methrat0n.restruct.schema.Interpreter.{ ConstrainedInterpreter, InvariantInterpreter, SimpleInterpreter }

import scala.language.higherKinds

/**
 * In the companion object of your enumeration you can use this helper to define the Schema of the Enumeration.
 *
 * {{{
 *   import io.github.methrat0n.restruct.schema.EnumeratumSchema
 *
 *   sealed trait Cats extends EnumEntry
 *
 *   object Cats extends Enum[Cats] {
 *
 *     val values = findValues
 *
 *     case object StrayCats extends Cats
 *     case object WhiteCats extends Cats
 *     case object TortoiseShellCats extends Cats
 *
 *     implicit schema = EnumSchema(Cats) //pass the companion object itself
 *   }
 * }}}
 *
 * Notice : It will only work for Format that are Invariant and Constrainable.
 */
final case class EnumeratumSchema[E <: EnumEntry, InternalInterpreter[Format[_]] <: SimpleInterpreter[Format, String]](
  enum: Enum[E]
) extends Schema[E, λ[Format[_] => InvariantInterpreter[Format, String, E, ConstrainedInterpreter[Format, String, InternalInterpreter[Format]]]]] {
  override def bind[Format[_]](implicit interpreter: InvariantInterpreter[Format, String, E, ConstrainedInterpreter[Format, String, InternalInterpreter[Format]]]): Format[E] =
    interpreter.imap(
      interpreter.underlyingInterpreter.verifying(
        interpreter.underlyingInterpreter.originalInterpreter.schema,
        EnumConstraint(enum)
      )
    )(enum.withName)(_.entryName)
}
