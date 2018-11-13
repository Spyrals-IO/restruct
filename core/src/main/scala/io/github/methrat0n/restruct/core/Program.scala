package io.github.methrat0n.restruct.core

import cats.{ Invariant, Semigroupal }
import io.github.methrat0n.restruct.core.data.schema.FieldAlgebra

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

  implicit def semiGroupal: Semigroupal[Program[FieldAlgebra, ?]] = new Semigroupal[Program[FieldAlgebra, ?]] {
    override def product[A, B](fa: Program[FieldAlgebra, A], fb: Program[FieldAlgebra, B]): Program[FieldAlgebra, (A, B)] = new Program[FieldAlgebra, (A, B)] {
      override def run[F[_]](implicit algebra: FieldAlgebra[F]): F[(A, B)] =
        algebra.product(fa.run(algebra), fb.run(algebra))
    }
  }

  implicit def invariant: Invariant[Program[FieldAlgebra, ?]] = new Invariant[Program[FieldAlgebra, ?]] {
    override def imap[A, B](fa: Program[FieldAlgebra, A])(f: A => B)(g: B => A): Program[FieldAlgebra, B] = new Program[FieldAlgebra, B] {
      override def run[F[_]](implicit algebra: FieldAlgebra[F]): F[B] =
        algebra.imap(fa.run(algebra))(f)(g)
    }
  }

}
