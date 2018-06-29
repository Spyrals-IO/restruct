package restruct.core.data.schema

import cats.{ Invariant, Semigroupal, Show }
import restruct.core.Program
import restruct.core.data.constraints.Constraint

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

object DefaultAlgebra {
  def showAlgebra: SchemaAlgebra[Show] = new SchemaAlgebra[Show] {
    override def stringSchema: Show[String] =
      Show.fromToString[String]

    override def decimalSchema: Show[Double] =
      Show.fromToString[Double]

    override def integerSchema: Show[Int] =
      Show.fromToString[Int]

    override def booleanSchema: Show[Boolean] =
      Show.fromToString[Boolean]

    override def many[T](name: String, schema: Show[T], default: Option[List[T]]): Show[List[T]] =
      Show.fromToString[List[T]]

    override def optional[T](name: String, schema: Show[T], default: Option[Option[T]]): Show[Option[T]] =
      Show.fromToString[Option[T]]

    override def required[T](name: String, schema: Show[T], default: Option[T]): Show[T] =
      Show.fromToString[T]

    override def verifying[T](schema: Show[T], constraint: Constraint[T]): Show[T] =
      Show.fromToString[T]

    override def either[A, B](a: Show[A], b: Show[B]): Show[Either[A, B]] =
      Show.fromToString[Either[A, B]]

    override def pure[T](a: T): Show[T] =
      Show.fromToString[T]

    override def imap[A, B](fa: Show[A])(f: A => B)(g: B => A): Show[B] =
      Show.fromToString[B]

    override def product[A, B](fa: Show[A], fb: Show[B]): Show[(A, B)] =
      Show.fromToString[(A, B)]
  }

}
