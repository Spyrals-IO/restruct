package io.github.methrat0n.restruct.core.data.schema

import io.github.methrat0n.restruct.core.data.constraints.Constraint

import scala.language.higherKinds

trait FieldAlgebra[F[_]] extends ComplexSchemaAlgebra[F] {

  def required[T](path: Path, schema: F[T], default: Option[T]): F[T]

  def optional[T](path: Path, schema: F[T], default: Option[Option[T]]): F[Option[T]]

  def verifying[T](schema: F[T], constraint: Constraint[T]): F[T]

  def verifying[T](schema: F[T], constraint: List[Constraint[T]]): F[T] =
    constraint.foldLeft(schema)((schema, constraint) => verifying(schema, constraint))

  /**
   * Should return a success if any found or concatenate errors.
   *
   * Behavior will be the following:
   * fa == sucess => fa
   * fa == error && fb == sucess => fb
   * fa == error && fb == error => concatenate errors
   *
   * If two successes are found, only the first parameter will be taken into account.
   *
   * @return F in error (depends on the implementing F) or successful F with one of the two value
   */
  def or[A, B](fa: F[A], fb: F[B]): F[Either[A, B]]

  def imap[A, B](fa: F[A])(f: A => B)(g: B => A): F[B]

  def product[A, B](fa: F[A], fb: F[B]): F[(A, B)]
}
