package restruct.refined.data.schema

import eu.timepit.refined.api.Refined
import lib.core.Program
import lib.core.data.schema.SchemaAlgebra
import cats.implicits._
import restruct.core.Program
import restruct.core.data.constraints.ConstraintProvider
import restruct.core.data.schema.SchemaAlgebra

import language.higherKinds

object RefinedSchema {
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
}
