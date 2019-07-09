package io.github.methrat0n.restruct.schema

import io.github.methrat0n.restruct.constraints.Constraint
import io.github.methrat0n.restruct.schema.Interpreter._

object Schemas {
  final case class And[A, B, AInterpreter[Format[_]] <: Interpreter[Format, A], BInterpreter[Format[_]] <: Interpreter[Format, B]](schemaA: Schema[A, AInterpreter], schemaB: Schema[B, BInterpreter]) extends Schema[(A, B), λ[Format[_] => SemiGroupalInterpreter[Format, A, B, AInterpreter[Format], BInterpreter[Format]]]] {
    override def bind[Format[_]](implicit interpreter: SemiGroupalInterpreter[Format, A, B, AInterpreter[Format], BInterpreter[Format]]): Format[(A, B)] =
      interpreter.product(schemaA.bind[Format](interpreter.originalInterpreterA), schemaB.bind[Format](interpreter.originalInterpreterB))
  }

  final case class Or[A, B, AInterpreter[Format[_]] <: Interpreter[Format, A], BInterpreter[Format[_]] <: Interpreter[Format, B]](schemaA: Schema[A, AInterpreter], schemaB: Schema[B, BInterpreter]) extends Schema[Either[A, B], λ[Format[_] => OneOfInterpreter[Format, A, B, AInterpreter[Format], BInterpreter[Format]]]] {
    override def bind[Format[_]](implicit interpreter: OneOfInterpreter[Format, A, B, AInterpreter[Format], BInterpreter[Format]]): Format[Either[A, B]] =
      interpreter.or(schemaA.bind[Format](interpreter.originalInterpreterA), schemaB.bind[Format](interpreter.originalInterpreterB))
  }

  final class SimpleSchema[Type] extends Schema[Type, λ[Format[_] => SimpleInterpreter[Format, Type]]] {
    override def bind[Format[_]](implicit interpreter: SimpleInterpreter[Format, Type]): Format[Type] =
      interpreter.schema
  }

  final class ManySchema[Collection[A] <: Iterable[A], Type, TypeInterpreter[Format[_]] <: Interpreter[Format, Type]](schema: Schema[Type, TypeInterpreter]) extends Schema[Collection[Type], λ[Format[_] => ManyInterpreter[Format, Type, Collection, TypeInterpreter[Format]]]] {
    override def bind[Format[_]](implicit interpreter: ManyInterpreter[Format, Type, Collection, TypeInterpreter[Format]]): Format[Collection[Type]] =
      interpreter.many(schema.bind[Format](interpreter.originalInterpreter))
  }

  final case class ConstrainedSchema[Type, TypeInterpreter[Format[_]] <: Interpreter[Format, Type]](schema: Schema[Type, TypeInterpreter], constraint: Constraint[Type]) extends Schema[Type, λ[Format[_] => ConstrainedInterpreter[Format, Type, TypeInterpreter[Format]]]] {
    override def bind[Format[_]](implicit interpreter: ConstrainedInterpreter[Format, Type, TypeInterpreter[Format]]): Format[Type] =
      interpreter.verifying(schema.bind[Format](interpreter.originalInterpreter), constraint)
  }

  final case class InvariantSchema[A, B, AInterpreter[Format[_]] <: Interpreter[Format, A]](schema: Schema[A, AInterpreter], f: A => B, g: B => A) extends Schema[B, λ[Format[_] => InvariantInterpreter[Format, A, B, AInterpreter[Format]]]] {
    override def bind[Format[_]](implicit interpreter: InvariantInterpreter[Format, A, B, AInterpreter[Format]]): Format[B] =
      interpreter.imap(schema.bind[Format](interpreter.underlyingInterpreter))(f)(g)
  }
}
