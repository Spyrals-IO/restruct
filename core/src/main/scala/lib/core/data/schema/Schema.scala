package lib.core.data.schema

import cats.implicits._
import enumeratum.{ Enum, EnumEntry }
import lib.core.Program
import lib.core.data.constraints.Constraints

import scala.language.higherKinds

object Schema {
  def from[T](implicit schemaProvider: SchemaProvider[T]): Program[SchemaAlgebra, T] = schemaProvider()

  def enum[E <: EnumEntry](enum: Enum[E]): Program[SchemaAlgebra, E] = new Program[SchemaAlgebra, E] {
    override def run[F[_]](implicit algebra: SchemaAlgebra[F]): F[E] =
      algebra.verifying(algebra.stringSchema, Constraints.EnumConstraint(enum)).imap(enum.withName)(_.entryName)
  }
}
