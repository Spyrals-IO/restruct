package restruct.refined.data.schema

import cats.implicits._
import eu.timepit.refined.api.Refined
import restruct.core.Program
import restruct.core.data.constraints.ConstraintProvider
import restruct.core.data.schema.ComplexSchemaAlgebra

import scala.language.higherKinds

object RefinedSchema {
  implicit def refinedSchema[T, P](implicit schemaProgram: Program[ComplexSchemaAlgebra, T], constraintProvider: ConstraintProvider[Refined[T, P]]): Program[ComplexSchemaAlgebra, Refined[T, P]] =
    new Program[ComplexSchemaAlgebra, Refined[T, P]] {
      override def run[F[_]](implicit algebra: ComplexSchemaAlgebra[F]): F[Refined[T, P]] =
        algebra.verifying(
          schemaProgram.run(algebra).imap(Refined.unsafeApply[T, P])(_.value),
          constraintProvider()
        )
    }
}
