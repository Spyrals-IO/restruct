package restruct.core.data.schema

import cats.Invariant
import restruct.core.Program

import scala.language.higherKinds

trait InvariantAlgebra[F[_]] extends Invariant[F]

object InvariantAlgebra {
  implicit val algebraInvariant: Invariant[Program[InvariantAlgebra, ?]] = new Invariant[Program[InvariantAlgebra, ?]] {
    override def imap[A, B](fa: Program[InvariantAlgebra, A])(f: A => B)(g: B => A): Program[InvariantAlgebra, B] = new Program[InvariantAlgebra, B] {
      override def run[F[_]](implicit algebra: InvariantAlgebra[F]): F[B] =
        algebra.imap(fa.run(algebra))(f)(g)
    }
  }
}
