package lib.core.data.schema

import cats.{Invariant, Semigroupal}
import cats.implicits._
import eu.timepit.refined.api.Refined
import lib.core.Program
import lib.core.data.constraints.{Constraint, ConstraintProvider}

import scala.language.higherKinds

trait SchemaAlgebra[F[_]] extends Semigroupal[F] with Invariant[F] {
  def stringSchema: F[String]

  def decimalSchema: F[Double]

  def integerSchema: F[Int]

  def booleanSchema: F[Boolean]

  def many[T](name: String, schema: F[T], default: Option[List[T]]): F[List[T]]

  def optional[T](name: String, schema: F[T], default: Option[Option[T]]): F[Option[T]]

  def required[T](name: String, schema: F[T], default: Option[T]): F[T]

  def verifying[T](schema: F[T], constraint: Constraint[T]): F[T]

  def verifying[T](schema: F[T], constraint: List[Constraint[T]]): F[T] =
    constraint.foldLeft(schema)((schema, constraint) => verifying(schema, constraint))

  def either[A, B](a: F[A], b: F[B]): F[Either[A, B]]

  def pure[T](a: T): F[T]
}

object SchemaAlgebra {
  implicit val stringSchema: Program[SchemaAlgebra, String] = new Program[SchemaAlgebra, String] {
    override def run[F[_]](implicit algebra: SchemaAlgebra[F]): F[String] = algebra.stringSchema
  }

  implicit val decimalSchema: Program[SchemaAlgebra, Double] = new Program[SchemaAlgebra, Double] {
    override def run[F[_]](implicit algebra: SchemaAlgebra[F]): F[Double] = algebra.decimalSchema
  }

  implicit val integerSchema: Program[SchemaAlgebra, Int] = new Program[SchemaAlgebra, Int] {
    override def run[F[_]](implicit algebra: SchemaAlgebra[F]): F[Int] = algebra.integerSchema
  }

  implicit val booleanSchema: Program[SchemaAlgebra, Boolean] = new Program[SchemaAlgebra, Boolean] {
    override def run[F[_]](implicit algebra: SchemaAlgebra[F]): F[Boolean] = algebra.booleanSchema
  }

  //TODO externalize
  implicit def refinedSchema[T, P](implicit
    schemaProgram: Program[SchemaAlgebra, T],
    constraintProvider: ConstraintProvider[Refined[T, P]]
  ): Program[SchemaAlgebra, Refined[T, P]] = new Program[SchemaAlgebra, Refined[T, P]] {
    override def run[F[_]](implicit algebra: SchemaAlgebra[F]): F[Refined[T, P]] =
      algebra.verifying(
        schemaProgram.run(algebra).imap(Refined.unsafeApply[T, P])(_.value),
        constraintProvider()
      )
  }

  implicit val schemaAlgebraTermSemigroupal: Semigroupal[Program[SchemaAlgebra, ?]] = new Semigroupal[Program[SchemaAlgebra, ?]] {
    override def product[A, B](fa: Program[SchemaAlgebra, A], fb: Program[SchemaAlgebra, B]): Program[SchemaAlgebra, (A, B)] = new Program[SchemaAlgebra, (A, B)] {
      override def run[F[_]](implicit algebra: SchemaAlgebra[F]): F[(A, B)] =
        algebra.product(fa.run(algebra), fb.run(algebra))
    }
  }

  implicit val schemaAlgebraInvariant: Invariant[Program[SchemaAlgebra, ?]] = new Invariant[Program[SchemaAlgebra, ?]] {
    override def imap[A, B](fa: Program[SchemaAlgebra, A])(f: A => B)(g: B => A): Program[SchemaAlgebra, B] = new Program[SchemaAlgebra, B] {
      override def run[F[_]](implicit algebra: SchemaAlgebra[F]): F[B] =
        algebra.imap(fa.run(algebra))(f)(g)
    }
  }
}
