package restruct.core.data.schema

import cats.Semigroupal
import restruct.core.Program

import scala.language.higherKinds

trait SemiGroupalAlgebra[F[_]] extends Semigroupal[F] {
  def either[A, B](a: F[A], b: F[B]): F[Either[A, B]]
}

object SemiGroupalAlgebra {
  implicit val composableAlgebraTermSemigroupal: Semigroupal[Program[SemiGroupalAlgebra, ?]] = new Semigroupal[Program[SemiGroupalAlgebra, ?]] {
    override def product[A, B](fa: Program[SemiGroupalAlgebra, A], fb: Program[SemiGroupalAlgebra, B]): Program[SemiGroupalAlgebra, (A, B)] = new Program[SemiGroupalAlgebra, (A, B)] {
      override def run[F[_]](implicit algebra: SemiGroupalAlgebra[F]): F[(A, B)] =
        algebra.product(fa.run(algebra), fb.run(algebra))
    }
  }
}
