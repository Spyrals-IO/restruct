package io.github.methrat0n.restruct.schema

import io.github.methrat0n.restruct.constraints.Constraint
import io.github.methrat0n.restruct.schema.Interpreter._

object Schemas {
  final case class And[A, B, AInterpreter[Format[_]] <: Interpreter[Format, A], BInterpreter[Format[_]] <: Interpreter[Format, B]](schemaA: Schema[A, AInterpreter], schemaB: Schema[B, BInterpreter]) extends Schema[(A, B), λ[Format[_] => SemiGroupalInterpreter[Format, A, B, AInterpreter[Format], BInterpreter[Format]]]] {
    override def bind[F[_]](implicit interpreter: SemiGroupalInterpreter[F, A, B, AInterpreter[F], BInterpreter[F]]): F[(A, B)] =
      interpreter.product(schemaA.bind[F](interpreter.originalInterpreterA), schemaB.bind[F](interpreter.originalInterpreterB))
  }

  final case class Or[A, B, AInterpreter[Format[_]] <: Interpreter[Format, A], BInterpreter[Format[_]] <: Interpreter[Format, B]](schemaA: Schema[A, AInterpreter], schemaB: Schema[B, BInterpreter]) extends Schema[Either[A, B], λ[Format[_] => OneOfInterpreter[Format, A, B, AInterpreter[Format], BInterpreter[Format]]]] {
    override def bind[F[_]](implicit interpreter: OneOfInterpreter[F, A, B, AInterpreter[F], BInterpreter[F]]): F[Either[A, B]] =
      interpreter.or(schemaA.bind[F](interpreter.originalInterpreterA), schemaB.bind[F](interpreter.originalInterpreterB))
  }

  final class SimpleSchema[Type] extends Schema[Type, λ[Format[_] => SimpleInterpreter[Format, Type]]] {
    override def bind[F[_]](implicit interpreter: SimpleInterpreter[F, Type]): F[Type] =
      interpreter.schema
  }

  final case class ManySchema[Collection[A] <: Iterable[A], Type, TypeInterpreter[Format[_]] <: Interpreter[Format, Type]](schema: Schema[Type, TypeInterpreter]) extends Schema[Collection[Type], λ[Format[_] => ManyInterpreter[Format, Type, Collection, TypeInterpreter[Format]]]] {
    override def bind[F[_]](implicit interpreter: ManyInterpreter[F, Type, Collection, TypeInterpreter[F]]): F[Collection[Type]] =
      interpreter.many(schema.bind[F](interpreter.originalInterpreter))
  }

  final case class ConstrainedSchema[Type, TypeInterpreter[Format[_]] <: Interpreter[Format, Type]](schema: Schema[Type, TypeInterpreter], constraint: Constraint[Type]) extends Schema[Type, λ[Format[_] => ConstrainedInterpreter[Format, Type, TypeInterpreter[Format]]]] {
    override def bind[F[_]](implicit interpreter: ConstrainedInterpreter[F, Type, TypeInterpreter[F]]): F[Type] =
      interpreter.verifying(schema.bind[F](interpreter.originalInterpreter), constraint)
  }

  final case class InvariantSchema[A, B, AInterpreter[Format[_]] <: Interpreter[Format, A]](schema: Schema[A, AInterpreter], f: A => B, g: B => A) extends Schema[B, λ[Format[_] => InvariantInterpreter[Format, A, B, AInterpreter[Format]]]] {
    override def bind[F[_]](implicit interpreter: InvariantInterpreter[F, A, B, AInterpreter[F]]): F[B] =
      interpreter.imap(schema.bind[F](interpreter.underlyingInterpreter))(f)(g)
  }
}
