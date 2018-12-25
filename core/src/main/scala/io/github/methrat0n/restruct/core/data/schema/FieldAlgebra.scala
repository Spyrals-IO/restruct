package io.github.methrat0n.restruct.core.data.schema

import io.github.methrat0n.restruct.core.data.constraints.Constraint

import scala.language.higherKinds

trait FieldAlgebra[F[_]] extends ComplexSchemaAlgebra[F] {

  def required[T](path: Path, schema: F[T], default: Option[T]): F[T]

  def optional[T](path: Path, schema: F[T], default: Option[Option[T]]): F[Option[T]]

  def verifying[T](schema: F[T], constraint: Constraint[T]): F[T]

  def verifying[T](schema: F[T], constraint: List[Constraint[T]]): F[T] =
    constraint.foldLeft(schema)((schema, constraint) => verifying(schema, constraint))

  def either[A, B](fa: F[A], fb: F[B]): F[Either[A, B]]

  def pure[A](a: A): F[A]

  def imap[A, B](fa: F[A])(f: A => B)(g: B => A): F[B]

  def product[A, B](fa: F[A], fb: F[B]): F[(A, B)]
}
