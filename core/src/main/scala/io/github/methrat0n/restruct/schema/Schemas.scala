package io.github.methrat0n.restruct.schema

import io.github.methrat0n.restruct.constraints.Constraint

import scala.collection.GenIterable

object Schemas {
  final case class And[A, B](schemaA: Schema[A], schemaB: Schema[B]) extends Schema[(A, B)] {
    override type OwnInterpreter[Format[_]] = SemiGroupalInterpreter[Format, A, B]

    override def bind[Format[_]](implicit algebra: OwnInterpreter[Format]): Format[(A, B)] =
      algebra.product(schemaA.secretBind[Format](algebra.originalInterpreterA), schemaB.secretBind[Format](algebra.originalInterpreterB))
  }

  final case class Or[A, B](schemaA: Schema[A], schemaB: Schema[B]) extends Schema[Either[A, B]] {
    override type OwnInterpreter[Format[_]] = OneOfInterpreter[Format, A, B]

    override def bind[Format[_]](implicit algebra: OwnInterpreter[Format]): Format[Either[A, B]] =
      algebra.or(schemaA.secretBind[Format](algebra.originalInterpreterA), schemaB.secretBind[Format](algebra.originalInterpreterB))
  }

  final class SimpleSchema[Type] extends Schema[Type] {
    override type OwnInterpreter[Format[_]] = SimpleInterpreter[Format, Type]
    override def bind[Format[_]](implicit algebra: SimpleInterpreter[Format, Type]): Format[Type] =
      algebra.schema
  }

  final class ManySchema[Collection[A] <: GenIterable[A], Type](schema: Schema[Type]) extends Schema[Collection[Type]] {
    override type OwnInterpreter[Format[_]] = ManyInterpreter[Format, Type, Collection]

    override def bind[Format[_]](implicit algebra: OwnInterpreter[Format]): Format[Collection[Type]] =
      algebra.many(schema.secretBind[Format](algebra.originalInterpreter))
  }

  final case class ConstrainedSchema[Type](schema: Schema[Type], constraint: Constraint[Type]) extends Schema[Type] {
    override type OwnInterpreter[Format[_]] = ConstrainedInterpreter[Format, Type]

    override def bind[Format[_]](implicit algebra: OwnInterpreter[Format]): Format[Type] =
      algebra.verifying(schema.secretBind[Format](algebra.originalInterpreter), constraint)
  }

  final case class InvariantSchema[A, B](schema: Schema[A], f: A => B, g: B => A) extends Schema[B] {
    override type OwnInterpreter[Format[_]] = InvariantInterpreter[Format, A, B]

    override def bind[Format[_]](implicit algebra: OwnInterpreter[Format]): Format[B] =
      algebra.imap(schema.secretBind[Format](algebra.originalInterpreterA))(f)(g)
  }
}
