package io.github.methrat0n.restruct.schema

import java.time.{ LocalDate, LocalTime, ZonedDateTime }

import cats.{ Invariant, Semigroupal }
import io.github.methrat0n.restruct.core.Program
import io.github.methrat0n.restruct.core.data.schema.FieldAlgebra
import shapeless.{ ::, Generic, HList, HNil }

import scala.language.higherKinds

object Syntax {

  val string: Schema[String] = Schema(new Program[FieldAlgebra, String] {
    override def run[F[_]](implicit algebra: FieldAlgebra[F]): F[String] =
      algebra.stringSchema
  })
  val decimal: Schema[Double] = Schema(new Program[FieldAlgebra, Double] {
    override def run[F[_]](implicit algebra: FieldAlgebra[F]): F[Double] =
      algebra.decimalSchema
  })
  val integer: Schema[Int] = Schema(new Program[FieldAlgebra, Int] {
    override def run[F[_]](implicit algebra: FieldAlgebra[F]): F[Int] =
      algebra.integerSchema
  })
  val boolean: Schema[Boolean] = Schema(new Program[FieldAlgebra, Boolean] {
    override def run[F[_]](implicit algebra: FieldAlgebra[F]): F[Boolean] =
      algebra.booleanSchema
  })
  val char: Schema[Char] = Schema(new Program[FieldAlgebra, Char] {
    override def run[F[_]](implicit algebra: FieldAlgebra[F]): F[Char] =
      algebra.charSchema
  })
  val byte: Schema[Byte] = Schema(new Program[FieldAlgebra, Byte] {
    override def run[F[_]](implicit algebra: FieldAlgebra[F]): F[Byte] =
      algebra.byteSchema
  })
  val short: Schema[Short] = Schema(new Program[FieldAlgebra, Short] {
    override def run[F[_]](implicit algebra: FieldAlgebra[F]): F[Short] =
      algebra.shortSchema
  })
  val float: Schema[Float] = Schema(new Program[FieldAlgebra, Float] {
    override def run[F[_]](implicit algebra: FieldAlgebra[F]): F[Float] =
      algebra.floatSchema
  })
  val bigDecimal: Schema[BigDecimal] = Schema(new Program[FieldAlgebra, BigDecimal] {
    override def run[F[_]](implicit algebra: FieldAlgebra[F]): F[BigDecimal] =
      algebra.bigDecimalSchema
  })
  val long: Schema[Long] = Schema(new Program[FieldAlgebra, Long] {
    override def run[F[_]](implicit algebra: FieldAlgebra[F]): F[Long] =
      algebra.longSchema
  })
  val bigInt: Schema[BigInt] = Schema(new Program[FieldAlgebra, BigInt] {
    override def run[F[_]](implicit algebra: FieldAlgebra[F]): F[BigInt] =
      algebra.bigIntSchema
  })
  val dateTime: Schema[ZonedDateTime] = Schema(new Program[FieldAlgebra, ZonedDateTime] {
    override def run[F[_]](implicit algebra: FieldAlgebra[F]): F[ZonedDateTime] =
      algebra.dateTimeSchema
  })
  val time: Schema[LocalTime] = Schema(new Program[FieldAlgebra, LocalTime] {
    override def run[F[_]](implicit algebra: FieldAlgebra[F]): F[LocalTime] =
      algebra.timeSchema
  })
  val date: Schema[LocalDate] = Schema(new Program[FieldAlgebra, LocalDate] {
    override def run[F[_]](implicit algebra: FieldAlgebra[F]): F[LocalDate] =
      algebra.dateSchema
  })

  val list: SchemaConstructor[List] = new SchemaConstructor[List] {
    override def of[A](reader: Schema[A]): Schema[List[A]] = Schema[List[A]](new Program[FieldAlgebra, List[A]] {
      override def run[F[_]](implicit algebra: FieldAlgebra[F]): F[List[A]] =
        algebra.many(reader.bind(algebra))
    })
  }

  implicit class FieldName(val name: String) extends AnyVal {
    def as[A](reader: Schema[A]): FieldBuilder1[A] = FieldBuilder1(reader.bindName(name))
  }

  final case class FieldBuilder1[FIELD_1](reader1: FieldSchema[FIELD_1]) {
    def and[FIELD_2](fieldBuilder: FieldBuilder1[FIELD_2]): FieldBuilder2[FIELD_1, FIELD_2] =
      FieldBuilder2(reader1, fieldBuilder.reader1)

    def build[TYPE <: Product](implicit
      invariant: Invariant[Program[FieldAlgebra, ?]],
      generic: Generic.Aux[TYPE, FIELD_1 :: HNil]
    ): Program[FieldAlgebra, TYPE] = {
      reader1.program.imap(_ :: HNil)(_.head)
        .imap(generic.from)(generic.to)
    }
  }

  final case class FieldBuilder2[FIELD_1, FIELD_2](reader1: FieldSchema[FIELD_1], reader2: FieldSchema[FIELD_2]) {
    def and[FIELD_3](fieldBuilder: FieldBuilder1[FIELD_3]): FieldBuilder3[FIELD_1, FIELD_2, FIELD_3] =
      FieldBuilder3(reader1, reader2, fieldBuilder.reader1)

    def build[TYPE <: Product](implicit
      semigroupal: Semigroupal[Program[FieldAlgebra, ?]],
      invariant: Invariant[Program[FieldAlgebra, ?]],
      generic: Generic.Aux[TYPE, FIELD_1 :: FIELD_2 :: HNil]
    ): Program[FieldAlgebra, TYPE] = {
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
      semigroupal: Semigroupal[Program[FieldAlgebra, ?]],
      invariant: Invariant[Program[FieldAlgebra, ?]],
      generic: Generic.Aux[TYPE, FIELD_1 :: FIELD_2 :: FIELD_3 :: HNil]
    ): Program[FieldAlgebra, TYPE] = {
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
      semigroupal: Semigroupal[Program[FieldAlgebra, ?]],
      invariant: Invariant[Program[FieldAlgebra, ?]],
      generic: Generic.Aux[TYPE, FIELD_1 :: FIELD_2 :: FIELD_3 :: FIELD_4 :: HNil]
    ): Program[FieldAlgebra, TYPE] = {
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
      semigroupal: Semigroupal[Program[FieldAlgebra, ?]],
      invariant: Invariant[Program[FieldAlgebra, ?]],
      generic: Generic.Aux[TYPE, FIELD_1 :: FIELD_2 :: FIELD_3 :: FIELD_4 :: FIELD_5 :: HNil]
    ): Program[FieldAlgebra, TYPE] = {
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
