package io.github.methrat0n.restruct.schema

import cats.{ Invariant, Semigroupal }
import io.github.methrat0n.restruct.core.Program
import io.github.methrat0n.restruct.core.data.schema.ComplexSchemaAlgebra
import shapeless.{ ::, Generic, HList, HNil }

import scala.language.higherKinds

object Syntax {

  val bigInt: Schema[BigInt] = Schema(new Program[ComplexSchemaAlgebra, BigInt] {
    override def run[F[_]](implicit algebra: ComplexSchemaAlgebra[F]): F[BigInt] =
      algebra.bigIntSchema
  })
  val bigDecimal: Schema[BigDecimal] = Schema(new Program[ComplexSchemaAlgebra, BigDecimal] {
    override def run[F[_]](implicit algebra: ComplexSchemaAlgebra[F]): F[BigDecimal] =
      algebra.bigDecimalSchema
  })
  val string: Schema[String] = Schema(new Program[ComplexSchemaAlgebra, String] {
    override def run[F[_]](implicit algebra: ComplexSchemaAlgebra[F]): F[String] =
      algebra.stringSchema
  })

  val option: SchemaConstructor[Option] = new SchemaConstructor[Option] {
    override def bindSchema[A](reader: Schema[A]): NameConstructor[Option[A]] = (nme: String) => new FieldSchema[Option[A]] {
      override protected def name: String = nme
      override protected[schema] def program: Program[ComplexSchemaAlgebra, Option[A]] = new Program[ComplexSchemaAlgebra, Option[A]] {
        override def run[F[_]](implicit algebra: ComplexSchemaAlgebra[F]): F[Option[A]] =
          algebra.optional(nme, reader.read(algebra), None)
      }

      override def defaultTo(defaultA: Option[A]): FieldSchema[Option[A]] = FieldSchema[Option[A]](nme, new Program[ComplexSchemaAlgebra, Option[A]] {
        override def run[F[_]](implicit algebra: ComplexSchemaAlgebra[F]): F[Option[A]] =
          algebra.optional[A](nme, reader.read(algebra), Some(defaultA))
      })
    }
  }
  val list: SchemaConstructor[List] = new SchemaConstructor[List] {
    override def bindSchema[A](reader: Schema[A]): NameConstructor[List[A]] = (nme: String) => new FieldSchema[List[A]] {
      override protected def name: String = nme
      override protected[schema] def program: Program[ComplexSchemaAlgebra, List[A]] = new Program[ComplexSchemaAlgebra, List[A]] {
        override def run[F[_]](implicit algebra: ComplexSchemaAlgebra[F]): F[List[A]] =
          algebra.many(nme, reader.read(algebra), None)
      }

      override def defaultTo(defaultA: List[A]): FieldSchema[List[A]] = FieldSchema[List[A]](nme, new Program[ComplexSchemaAlgebra, List[A]] {
        override def run[F[_]](implicit algebra: ComplexSchemaAlgebra[F]): F[List[A]] =
          algebra.many[A](nme, reader.read(algebra), Some(defaultA))
      })
    }
  }

  implicit class FieldName(val name: String) extends AnyVal {
    def as[A](constructor: NameConstructor[A]): FieldBuilder1[A] = FieldBuilder1(constructor.bindName(name))
    def as[A](reader: Schema[A]): FieldBuilder1[A] = FieldBuilder1(reader.bindName(name))
  }

  final case class FieldBuilder1[FIELD_1](reader1: FieldSchema[FIELD_1]) {
    def and[FIELD_2](fieldBuilder: FieldBuilder1[FIELD_2]): FieldBuilder2[FIELD_1, FIELD_2] =
      FieldBuilder2(reader1, fieldBuilder.reader1)

    def build[TYPE <: Product](implicit
      invariant: Invariant[Program[ComplexSchemaAlgebra, ?]],
      generic: Generic.Aux[TYPE, FIELD_1 :: HNil]
    ): Program[ComplexSchemaAlgebra, TYPE] = {
      reader1.program.imap(_ :: HNil)(_.head)
        .imap(generic.from)(generic.to)
    }
  }

  final case class FieldBuilder2[FIELD_1, FIELD_2](reader1: FieldSchema[FIELD_1], reader2: FieldSchema[FIELD_2]) {
    def and[FIELD_3](fieldBuilder: FieldBuilder1[FIELD_3]): FieldBuilder3[FIELD_1, FIELD_2, FIELD_3] =
      FieldBuilder3(reader1, reader2, fieldBuilder.reader1)

    def build[TYPE <: Product](implicit
      semigroupal: Semigroupal[Program[ComplexSchemaAlgebra, ?]],
      invariant: Invariant[Program[ComplexSchemaAlgebra, ?]],
      generic: Generic.Aux[TYPE, FIELD_1 :: FIELD_2 :: HNil]
    ): Program[ComplexSchemaAlgebra, TYPE] = {
      reader1.program.product(
        reader2.program
      ).imap(firstTuple2ToHlist)(firstHlistToTuple2)
        .imap(generic.from)(generic.to)
    }
  }

  final case class FieldBuilder3[FIELD_1, FIELD_2, FIELD_3](reader1: FieldSchema[FIELD_1], reader2: FieldSchema[FIELD_2], reader3: FieldSchema[FIELD_3]) {
    def and[FIELD_4](fieldBuilder: FieldBuilder1[FIELD_4]): FieldBuilder4[FIELD_1, FIELD_2, FIELD_3, FIELD_4] =
      FieldBuilder4(reader1, reader2, reader3, fieldBuilder.reader1)

    def build[TYPE <: Product](implicit
      semigroupal: Semigroupal[Program[ComplexSchemaAlgebra, ?]],
      invariant: Invariant[Program[ComplexSchemaAlgebra, ?]],
      generic: Generic.Aux[TYPE, FIELD_1 :: FIELD_2 :: FIELD_3 :: HNil]
    ): Program[ComplexSchemaAlgebra, TYPE] = {
      reader1.program.product(
        reader2.program.product(reader3.program).imap(firstTuple2ToHlist)(firstHlistToTuple2)
      ).imap(tuple2ToHlist)(hlistToTuple2)
        .imap(generic.from)(generic.to)
    }
  }

  final case class FieldBuilder4[FIELD_1, FIELD_2, FIELD_3, FIELD_4](reader1: FieldSchema[FIELD_1], reader2: FieldSchema[FIELD_2], reader3: FieldSchema[FIELD_3], reader4: FieldSchema[FIELD_4]) {
    def and[FIELD_5](fieldBuilder: FieldBuilder1[FIELD_5]): FieldBuilder5[FIELD_1, FIELD_2, FIELD_3, FIELD_4, FIELD_5] =
      FieldBuilder5(reader1, reader2, reader3, reader4, fieldBuilder.reader1)

    def build[TYPE <: Product](implicit
      semigroupal: Semigroupal[Program[ComplexSchemaAlgebra, ?]],
      invariant: Invariant[Program[ComplexSchemaAlgebra, ?]],
      generic: Generic.Aux[TYPE, FIELD_1 :: FIELD_2 :: FIELD_3 :: FIELD_4 :: HNil]
    ): Program[ComplexSchemaAlgebra, TYPE] = {
      reader1.program.product(
        reader2.program.product(
          reader3.program.product(reader4.program).imap(firstTuple2ToHlist)(firstHlistToTuple2)
        ).imap(tuple2ToHlist)(hlistToTuple2)
      ).imap(tuple2ToHlist)(hlistToTuple2)
        .imap(generic.from)(generic.to)
    }
  }

  final case class FieldBuilder5[FIELD_1, FIELD_2, FIELD_3, FIELD_4, FIELD_5](reader1: FieldSchema[FIELD_1], reader2: FieldSchema[FIELD_2], reader3: FieldSchema[FIELD_3], reader4: FieldSchema[FIELD_4], reader5: FieldSchema[FIELD_5]) {
    def build[TYPE <: Product](implicit
      semigroupal: Semigroupal[Program[ComplexSchemaAlgebra, ?]],
      invariant: Invariant[Program[ComplexSchemaAlgebra, ?]],
      generic: Generic.Aux[TYPE, FIELD_1 :: FIELD_2 :: FIELD_3 :: FIELD_4 :: FIELD_5 :: HNil]
    ): Program[ComplexSchemaAlgebra, TYPE] = {
      reader1.program.product(
        reader2.program.product(
          reader3.program.product(
            reader4.program.product(reader5.program).imap(firstTuple2ToHlist)(firstHlistToTuple2)
          ).imap(tuple2ToHlist)(hlistToTuple2)
        ).imap(tuple2ToHlist)(hlistToTuple2)
      ).imap(tuple2ToHlist)(hlistToTuple2)
        .imap(generic.from)(generic.to)
    }
  }

  private def firstTuple2ToHlist[A, B](tuple: (A, B)): A :: B :: HNil = tuple2ToHlist((tuple._1, tuple._2 :: HNil))
  private def firstHlistToTuple2[A, B](hlist: A :: B :: HNil): (A, B) = {
    val tuple = hlistToTuple2(hlist)
    (tuple._1, tuple._2.head)
  }
  private def tuple2ToHlist[A, B <: HList](tuple: (A, B)): A :: B = tuple._1 :: tuple._2
  private def hlistToTuple2[A, B <: HList](hlist: A :: B): (A, B) = (hlist.head, hlist.tail)
}
