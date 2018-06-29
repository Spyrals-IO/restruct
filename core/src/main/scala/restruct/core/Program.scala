package restruct.core

import scala.language.higherKinds

trait Program[Algebra[_[_]], A] {
  def run[F[_]](implicit algebra: Algebra[F]): F[A]
}
