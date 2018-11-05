package io.github.methrat0n.restruct.core.data.schema

import io.github.methrat0n.restruct.core.data.constraints.Constraint

import scala.language.higherKinds

trait ComplexSchemaAlgebra[F[_]] extends SimpleSchemaAlgebra[F] with MonoidAlgebra[F] with InvariantAlgebra[F] {

  //def tupleSchema: F[Tuple] TODO find something ? Is it worth adding all of them ?

  //def functionSchema: F[] TODO worth adding them all ? Maybe generate automaticaly with the compiler ?

  // use GenTraversableOnce ?
  def many[T](name: String, schema: F[T], default: Option[List[T]]): F[List[T]]

  def optional[T](name: String, schema: F[T], default: Option[Option[T]]): F[Option[T]]

  def required[T](name: String, schema: F[T], default: Option[T]): F[T]

  //TODO should verification of constraints realy be part of the algebra ? Isn't a type already a constraint ?
  def verifying[T](schema: F[T], constraint: Constraint[T]): F[T]

  def verifying[T](schema: F[T], constraint: List[Constraint[T]]): F[T] =
    constraint.foldLeft(schema)((schema, constraint) => verifying(schema, constraint))
}
