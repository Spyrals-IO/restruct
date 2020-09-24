package io.github.methrat0n.restruct.schema

import io.github.methrat0n.restruct.constraints.Constraint
import io.github.methrat0n.restruct.schema.Interpreter._

object Schemas {
  final case class And[A, B, AInterpreter[Format[_]] <: Interpreter[Format, A], BInterpreter[Format[_]] <: Interpreter[Format, B]](schemaA: Schema.Aux[A, AInterpreter], schemaB: Schema.Aux[B, BInterpreter]) extends Schema[(A, B)] {
    override type InternalInterpreter[Format[_]] = SemiGroupalInterpreter[Format, A, B, AInterpreter[Format], BInterpreter[Format]]
    def bind[Format[_]](implicit interpreter: SemiGroupalInterpreter[Format, A, B, AInterpreter[Format], BInterpreter[Format]]): Format[(A, B)] =
      interpreter.product(
        schemaA.bind[Format](interpreter.originalInterpreterA),
        schemaB.bind[Format](interpreter.originalInterpreterB)
      )
  }

  final case class Or[A, B, AInterpreter[Format[_]] <: Interpreter[Format, A], BInterpreter[Format[_]] <: Interpreter[Format, B]](schemaA: Schema.Aux[A, AInterpreter], schemaB: Schema.Aux[B, BInterpreter]) extends Schema[Either[A, B]] {
    override type InternalInterpreter[Format[_]] = OneOfInterpreter[Format, A, B, AInterpreter[Format], BInterpreter[Format]]
    override def bind[F[_]](implicit interpreter: OneOfInterpreter[F, A, B, AInterpreter[F], BInterpreter[F]]): F[Either[A, B]] =
      interpreter.or(schemaA.bind[F](interpreter.originalInterpreterA), schemaB.bind[F](interpreter.originalInterpreterB))
  }

  final class SimpleSchema[Type] extends Schema[Type] {
    override type InternalInterpreter[Format[_]] = SimpleInterpreter[Format, Type]
    override def bind[F[_]](implicit interpreter: SimpleInterpreter[F, Type]): F[Type] =
      interpreter.schema
  }

  final case class ManySchema[Collection[A] <: Iterable[A], Type, TypeInterpreter[Format[_]] <: Interpreter[Format, Type]](schema: Schema.Aux[Type, TypeInterpreter]) extends Schema[Collection[Type]] {
    override type InternalInterpreter[Format[_]] = ManyInterpreter[Format, Type, Collection, TypeInterpreter[Format]]
    override def bind[F[_]](implicit interpreter: ManyInterpreter[F, Type, Collection, TypeInterpreter[F]]): F[Collection[Type]] =
      interpreter.many(schema.bind[F](interpreter.originalInterpreter))
  }

  final case class ConstrainedSchema[Type, TypeInterpreter[Format[_]] <: Interpreter[Format, Type]](schema: Schema.Aux[Type, TypeInterpreter], constraint: Constraint[Type]) extends Schema[Type] {
    override type InternalInterpreter[Format[_]] = ConstrainedInterpreter[Format, Type, TypeInterpreter[Format]]
    override def bind[F[_]](implicit interpreter: ConstrainedInterpreter[F, Type, TypeInterpreter[F]]): F[Type] =
      interpreter.verifying(schema.bind[F](interpreter.originalInterpreter), constraint)
  }

  final case class InvariantSchema[A, B, AInterpreter[Format[_]] <: Interpreter[Format, A]](schema: Schema.Aux[A, AInterpreter], f: A => B, g: B => A) extends Schema[B] {
    override type InternalInterpreter[Format[_]] = InvariantInterpreter[Format, A, B, AInterpreter[Format]]
    override def bind[F[_]](implicit interpreter: InvariantInterpreter[F, A, B, AInterpreter[F]]): F[B] =
      interpreter.imap(schema.bind[F](interpreter.underlyingInterpreter))(f)(g)
  }
}
