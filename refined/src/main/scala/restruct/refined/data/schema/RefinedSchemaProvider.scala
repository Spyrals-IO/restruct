package restruct.refined.data.schema

import lib.core.Program
import eu.timepit.refined.api.Refined
import lib.core.data.schema.{SchemaAlgebra, SchemaProvider}
import shapeless.{Witness, labelled}
import shapeless.labelled.FieldType
import cats.implicits._
import restruct.core.Program
import restruct.core.data.constraints.ConstraintProvider
import restruct.core.data.schema.{SchemaAlgebra, SchemaProvider}

import language.higherKinds

object RefinedSchemaProvider {
  implicit def refinedManyFieldSchemaProgram[KEY <: Symbol, VALUE, P](implicit
    witness: Witness.Aux[KEY],
    valueSchema: Program[SchemaAlgebra, VALUE],
    constraintProvider: ConstraintProvider[Refined[List[VALUE], P]]
  ): SchemaProvider.WithDefault[FieldType[KEY, Refined[List[VALUE], P]], Option[Refined[List[VALUE], P]]] =
    (default: Option[Refined[List[VALUE], P]]) => new Program[SchemaAlgebra, FieldType[KEY, Refined[List[VALUE], P]]] {
      override def run[F[_]](implicit algebra: SchemaAlgebra[F]): F[FieldType[KEY, Refined[List[VALUE], P]]] =
        algebra.verifying(
          algebra.many(witness.value.name, valueSchema.run(algebra), default.map(_.value)).imap(Refined.unsafeApply[List[VALUE], P])(_.value),
          constraintProvider()
        ).imap(labelled.field[KEY].apply _)(identity)
    }

}
