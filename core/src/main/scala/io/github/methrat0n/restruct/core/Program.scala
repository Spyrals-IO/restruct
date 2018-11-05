package io.github.methrat0n.restruct.core

import cats.{ Invariant, Semigroupal }
import io.github.methrat0n.restruct.core.data.schema.ComplexSchemaAlgebra

import scala.language.higherKinds

trait Program[Algebra[_[_]], A] {
  def run[F[_]](implicit algebra: Algebra[F]): F[A]
}

object Program {

  implicit class RichProgram[Algebra[_[_]], A](program: Program[Algebra, A]) {

    def product[B](fb: Program[Algebra, B])(implicit semiGroupal: Semigroupal[Program[Algebra, ?]]): Program[Algebra, (A, B)] =
      semiGroupal.product[A, B](program, fb)

    def imap[B](f: A => B)(g: B => A)(implicit invariant: Invariant[Program[Algebra, ?]]): Program[Algebra, B] =
      invariant.imap(program)(f)(g)
  }

  //Todo try to limit the algebra to the most tiny one, semigroupal and invariant here
  implicit def semiGroupal: Semigroupal[Program[ComplexSchemaAlgebra, ?]] = new Semigroupal[Program[ComplexSchemaAlgebra, ?]] {
    override def product[A, B](fa: Program[ComplexSchemaAlgebra, A], fb: Program[ComplexSchemaAlgebra, B]): Program[ComplexSchemaAlgebra, (A, B)] = new Program[ComplexSchemaAlgebra, (A, B)] {
      override def run[F[_]](implicit algebra: ComplexSchemaAlgebra[F]): F[(A, B)] =
        algebra.product(fa.run(algebra), fb.run(algebra))
    }
  }

  implicit def invariant: Invariant[Program[ComplexSchemaAlgebra, ?]] = new Invariant[Program[ComplexSchemaAlgebra, ?]] {
    override def imap[A, B](fa: Program[ComplexSchemaAlgebra, A])(f: A => B)(g: B => A): Program[ComplexSchemaAlgebra, B] = new Program[ComplexSchemaAlgebra, B] {
      override def run[F[_]](implicit algebra: ComplexSchemaAlgebra[F]): F[B] =
        algebra.imap(fa.run(algebra))(f)(g)
    }
  }

}
