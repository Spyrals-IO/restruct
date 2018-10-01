package restruct.core.data.schema

import scala.language.higherKinds

trait IdentityAlgebra[F[_]] {
  def pure[T](t: T): F[T]
}
