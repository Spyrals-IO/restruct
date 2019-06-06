package io.github.methrat0n.restruct.schema

import io.github.methrat0n.restruct.constraints.Constraint

import scala.annotation.implicitNotFound
import scala.collection.GenIterable

sealed trait Interpreter[Format[_], +Type]

@implicitNotFound("""
  Cannot find an interpreter for ${Type}
""")
trait SimpleInterpreter[Format[_], Type] extends Interpreter[Format, Type] {
  def schema: Format[Type]
}

@implicitNotFound("""
  Cannot find an interpreter for collection ${Collection} of ${Type}
""")
trait ManyInterpreter[Format[_], Type, Collection[A] <: GenIterable[A]] extends Interpreter[Format, Collection[Type]] {
  def originalInterpreter: Interpreter[Format, Type]
  def many(schema: Format[Type]): Format[Collection[Type]]
}

@implicitNotFound("""
  Cannot find an interpreter for ${Type}.
  Maybe the format you ask for does not support the path you used.
""")
trait RequiredInterpreter[Format[_], P <: Path, Type] extends Interpreter[Format, Type] {
  def required(path: P, schema: Format[Type], default: Option[Type]): Format[Type]
}

@implicitNotFound("""
  Cannot find an interpreter for ${Type}.
  Maybe the format you ask for does not support the path you used.
""")
trait OptionalInterpreter[Format[_], P <: Path, Type] extends Interpreter[Format, Option[Type]] {
  def originalInterpreter: Interpreter[Format, Type]
  def optional(path: P, schema: Format[Type], default: Option[Option[Type]]): Format[Option[Type]]
}

trait ConstrainedInterpreter[Format[_], Type] extends Interpreter[Format, Type] {
  def originalInterpreter: Interpreter[Format, Type]
  def verifying(schema: Format[Type], constraint: Constraint[Type]): Format[Type]

  def verifying(schema: Format[Type], constraint: List[Constraint[Type]]): Format[Type] =
    constraint.foldLeft(schema)((schema, constraint) => verifying(schema, constraint))
}

@implicitNotFound("""
  Cannot find an interpreter for Either[${A}, ${B}].
  It needs the interpreters for ${A} and ${B}, maybe their missing ?
""")
trait OneOfInterpreter[Format[_], A, B] extends Interpreter[Format, Either[A, B]] {
  def originalInterpreterA: Interpreter[Format, A]
  def originalInterpreterB: Interpreter[Format, B]
  /**
   * Should return a success, if any, or concatenate errors.
   *
   * fa == sucess => fa result in Left
   * fa == error && fb == sucess => fb result in Right
   * fa == error && fb == error => concatenate fa and fb errors into F error handling
   *
   * If two successes are found, fa will be choosen.
   *
   * @return F in error (depends on the implementing F) or successful F with one of the two value
   */
  def or(fa: Format[A], fb: Format[B]): Format[Either[A, B]]
}

@implicitNotFound("""
  Cannot find an interpreter for ${A} with ${B}.
  It needs the interpreters for ${A} and ${B}, maybe their missing ?
""")
trait InvariantInterpreter[Format[_], A, B] extends Interpreter[Format, A with B] {
  def originalInterpreterA: Interpreter[Format, A]
  def originalInterpreterB: Interpreter[Format, B]
  def imap(fa: Format[A])(f: A => B)(g: B => A): Format[B]
}

@implicitNotFound("""
  Cannot find an interpreter for (${A}, ${B}).
  It needs the interpreters for ${A} and ${B}, maybe their missing ?
""")
trait SemiGroupalInterpreter[Format[_], A, B] extends Interpreter[Format, (A, B)] {
  def originalInterpreterA: Interpreter[Format, A]
  def originalInterpreterB: Interpreter[Format, B]
  def product(fa: Format[A], fb: Format[B]): Format[(A, B)]
}
