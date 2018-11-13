package io.github.methrat0n.restruct.core.data.schema

import alleycats.Pure
import cats.{ Invariant, Semigroupal }
import io.github.methrat0n.restruct.core.data.constraints.Constraint

import language.higherKinds

trait FieldAlgebra[F[_]] extends ComplexSchemaAlgebra[F]
  with Semigroupal[F] with Invariant[F] with Pure[F] {

  def required[T](name: String, schema: F[T], default: Option[T]): F[T]

  def optional[T](name: String, schema: F[T], default: Option[Option[T]]): F[Option[T]]

  def verifying[T](schema: F[T], constraint: Constraint[T]): F[T]

  def verifying[T](schema: F[T], constraint: List[Constraint[T]]): F[T] =
    constraint.foldLeft(schema)((schema, constraint) => verifying(schema, constraint))

  def either[A, B](fa: F[A], fb: F[B]): F[Either[A, B]]
}
