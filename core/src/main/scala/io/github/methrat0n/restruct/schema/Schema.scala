package io.github.methrat0n.restruct.schema

import cats.{ Invariant, Semigroupal }
import io.github.methrat0n.restruct.core.Program
import io.github.methrat0n.restruct.core.data.constraints.Constraint
import io.github.methrat0n.restruct.core.data.schema.FieldAlgebra
import io.github.methrat0n.restruct.schema.ProductSchema.{ ProductSchema1, ProductSchema2 }
import shapeless.{ ::, Generic, HList, HNil }

import scala.language.higherKinds

trait SchemaConstructor[TYPE_CONSTRUCTOR[_]] {
  def of[A](schema: Schema[A]): Schema[TYPE_CONSTRUCTOR[A]]
}

sealed trait Schema[A] {
  def constraints: List[Constraint[A]]
  def constraintedBy(constraint: Constraint[A]): Schema[A]
  def bind[FORMAT[_]](algebra: FieldAlgebra[FORMAT]): FORMAT[A]
}

object ProductSchema {

  final case class ProductSchema1[A, Field_1](
    field1: Field[Field_1],
    constraints: List[Constraint[A]]
  )(implicit generic: Generic.Aux[A, Field_1 :: HNil]) extends Schema[A] {
    def bind[FORMAT[_]](algebra: FieldAlgebra[FORMAT]): FORMAT[A] =
      field1.program.imap(_ :: HNil)(_.head)
        .imap(generic.from)(generic.to).run(algebra)

    override def constraintedBy(constraint: Constraint[A]): Schema[A] =
      copy(constraints = constraints :+ constraint)
  }

  final case class ProductSchema2[A, Field_1, Field_2](
    field1: Field[Field_1],
    field2: Field[Field_2],
    constraints: List[Constraint[A]]
  )(implicit
    semigroupal: Semigroupal[Program[FieldAlgebra, ?]],
    invariant: Invariant[Program[FieldAlgebra, ?]],
    generic: Generic.Aux[A, Field_1 :: Field_2 :: HNil]
  ) extends Schema[A] {
    def bind[FORMAT[_]](algebra: FieldAlgebra[FORMAT]): FORMAT[A] =
      field1.program
        .product(field2.program).imap(firstTuple2ToHlist)(firstHlistToTuple2)
        .imap(generic.from)(generic.to)
        .run(algebra)

    override def constraintedBy(constraint: Constraint[A]): Schema[A] =
      copy(constraints = constraints :+ constraint)
  }

  private def firstTuple2ToHlist[A, B](tuple: (A, B)): A :: B :: HNil = tuple2ToHlist((tuple._1, tuple._2 :: HNil))
  private def firstHlistToTuple2[A, B](hlist: A :: B :: HNil): (A, B) = {
    val tuple = hlistToTuple2(hlist)
    (tuple._1, tuple._2.head)
  }
  private def tuple2ToHlist[A, B <: HList](tuple: (A, B)): A :: B = tuple._1 :: tuple._2
  private def hlistToTuple2[A, B <: HList](hlist: A :: B): (A, B) = (hlist.head, hlist.tail)
}

trait TypedSchema[A] extends Schema[A] {
  def bind[FORMAT[_]](algebra: FieldAlgebra[FORMAT]): FORMAT[A] =
    program.run(algebra)

  def program: Program[FieldAlgebra, A]

  override def constraintedBy(constraint: Constraint[A]): Schema[A] = TypedSchema(program, constraints :+ constraint)
}

object TypedSchema {
  def apply[A](prgram: Program[FieldAlgebra, A], constaints: List[Constraint[A]]): TypedSchema[A] = new TypedSchema[A]() {
    override def program: Program[FieldAlgebra, A] = prgram
    override def constraints: List[Constraint[A]] = constaints
  }
}

object Schema {
  def apply[Type <: Product, Field_1](field1: Field[Field_1])(implicit generic: Generic.Aux[Type, Field_1 :: HNil]): Schema[Type] = new ProductSchema1[Type, Field_1](field1, List.empty)
  def apply[Type <: Product, Field_1, Field_2](field1: Field[Field_1], field2: Field[Field_2])(implicit generic: Generic.Aux[Type, Field_1 :: Field_2 :: HNil]): Schema[Type] = new ProductSchema2[Type, Field_1, Field_2](field1, field2, List.empty)

  def apply[Type](program: Program[FieldAlgebra, Type]): Schema[Type] = TypedSchema(program, List.empty)
}
