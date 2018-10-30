package restruct.reader

import cats.{Invariant, Semigroupal}
import restruct.core.Program
import restruct.core.data.schema.ComplexSchemaAlgebra
import shapeless.labelled.FieldType
import shapeless.tag.Tagged
import shapeless.{::, HList, HNil, LabelledGeneric, labelled}

import scala.language.higherKinds

object Syntax {

  val bigInt: Reader[BigInt] = Reader(new Program[ComplexSchemaAlgebra, BigInt] {
    override def run[F[_]](implicit algebra: ComplexSchemaAlgebra[F]): F[BigInt] =
      algebra.bigIntSchema
  })
  val bigDecimal: Reader[BigDecimal] = Reader(new Program[ComplexSchemaAlgebra, BigDecimal] {
    override def run[F[_]](implicit algebra: ComplexSchemaAlgebra[F]): F[BigDecimal] =
      algebra.bigDecimalSchema
  })
  val string: Reader[String] = Reader(new Program[ComplexSchemaAlgebra, String] {
    override def run[F[_]](implicit algebra: ComplexSchemaAlgebra[F]): F[String] =
      algebra.stringSchema
  })

  val option: ReaderConstructor[Option] = new ReaderConstructor[Option] {
    override def bindReader[A](reader: Reader[A]): NameConstructor[Option[A]] = (nme: String) => new NamedReader[Option[A]] {
      override protected def name: String = nme
      override protected[reader] def program: Program[ComplexSchemaAlgebra, Option[A]] = new Program[ComplexSchemaAlgebra, Option[A]] {
        override def run[F[_]](implicit algebra: ComplexSchemaAlgebra[F]): F[Option[A]] =
          algebra.optional(nme, reader.read(algebra), None)
      }

      override def defaultTo(defaultA: Option[A]): NamedReader[Option[A]] = NamedReader[Option[A]](nme, new Program[ComplexSchemaAlgebra, Option[A]] {
        override def run[F[_]](implicit algebra: ComplexSchemaAlgebra[F]): F[Option[A]] =
          algebra.optional[A](nme, reader.read(algebra), Some(defaultA))
      })
    }
  }
  val list: ReaderConstructor[List] = new ReaderConstructor[List] {
    override def bindReader[A](reader: Reader[A]): NameConstructor[List[A]] = (nme: String) => new NamedReader[List[A]] {
      override protected def name: String = nme
      override protected[reader] def program: Program[ComplexSchemaAlgebra, List[A]] = new Program[ComplexSchemaAlgebra, List[A]] {
        override def run[F[_]](implicit algebra: ComplexSchemaAlgebra[F]): F[List[A]] =
          algebra.many(nme, reader.read(algebra), None)
      }

      override def defaultTo(defaultA: List[A]): NamedReader[List[A]] = NamedReader[List[A]](nme, new Program[ComplexSchemaAlgebra, List[A]] {
        override def run[F[_]](implicit algebra: ComplexSchemaAlgebra[F]): F[List[A]] =
          algebra.many[A](nme, reader.read(algebra), Some(defaultA))
      })
    }
  }

  implicit class FieldName[NAME <: String with Singleton](val name: NAME) {
    def as[A](constructor: NameConstructor[A]): FieldBuilder1[NAME , A] = FieldBuilder1[NAME, A](constructor.bindName(name))
    def as[A](reader: Reader[A]): FieldBuilder1[NAME , A] = FieldBuilder1[NAME, A](reader.bindName(name))
  }

  private[reader] type Symboled[Key] = Symbol with Tagged[Key]

  final case class FieldBuilder1[KEY_1, FIELD_1](reader1: NamedReader[FIELD_1]) {
    def and[KEY_2, FIELD_2](fieldBuilder: FieldBuilder1[KEY_2, FIELD_2]): FieldBuilder2[KEY_1, FIELD_1, KEY_2, FIELD_2] =
      FieldBuilder2(reader1, fieldBuilder.reader1)

    def build[TYPE <: Product](implicit
      invariant: Invariant[Program[ComplexSchemaAlgebra, ?]],
      generic: LabelledGeneric.Aux[TYPE, FieldType[Symboled[KEY_1], FIELD_1] :: HNil]
    ): Program[ComplexSchemaAlgebra, TYPE] = {
      reader1.program.imap(program => labelled.field[Symboled[KEY_1]](program) :: HNil)(_.head)
        .imap(generic.from)(generic.to)
    }
  }

  final case class FieldBuilder2[KEY_1, FIELD_1, KEY_2, FIELD_2](reader1: NamedReader[FIELD_1], reader2: NamedReader[FIELD_2]) {
    def and[KEY_3, FIELD_3](fieldBuilder: FieldBuilder1[KEY_3, FIELD_3]): FieldBuilder3[KEY_1, FIELD_1, KEY_2, FIELD_2, KEY_3, FIELD_3] =
      FieldBuilder3(reader1, reader2, fieldBuilder.reader1)

    def build[TYPE <: Product](implicit
      semigroupal: Semigroupal[Program[ComplexSchemaAlgebra, ?]],
      invariant: Invariant[Program[ComplexSchemaAlgebra, ?]],
      generic: LabelledGeneric.Aux[TYPE, FieldType[Symboled[KEY_1], FIELD_1] :: FieldType[Symboled[KEY_2], FIELD_2] :: HNil]
    ): Program[ComplexSchemaAlgebra, TYPE] = {
      reader1.program.product(
        reader2.program
      ).imap(firstTuple2ToHlist[Symboled[KEY_1], FIELD_1, Symboled[KEY_2], FIELD_2])(firstHlistToTuple2)
        .imap(generic.from)(generic.to)
    }
  }

  final case class FieldBuilder3[KEY_1, FIELD_1, KEY_2, FIELD_2, KEY_3, FIELD_3](reader1: NamedReader[FIELD_1], reader2: NamedReader[FIELD_2], reader3: NamedReader[FIELD_3]) {
    def and[KEY_4, FIELD_4](fieldBuilder: FieldBuilder1[KEY_4, FIELD_4]): FieldBuilder4[KEY_1, FIELD_1, KEY_2, FIELD_2, KEY_3, FIELD_3, KEY_4, FIELD_4] =
      FieldBuilder4(reader1, reader2, reader3, fieldBuilder.reader1)

    def build[TYPE <: Product](implicit
      semigroupal: Semigroupal[Program[ComplexSchemaAlgebra, ?]],
      invariant: Invariant[Program[ComplexSchemaAlgebra, ?]],
      generic: LabelledGeneric.Aux[TYPE, FieldType[Symboled[KEY_1], FIELD_1] :: FieldType[Symboled[KEY_2], FIELD_2] :: FieldType[Symboled[KEY_3], FIELD_3] :: HNil]
    ): Program[ComplexSchemaAlgebra, TYPE] = {
      reader1.program.product(
        reader2.program.product(reader3.program).imap(firstTuple2ToHlist[Symboled[KEY_2], FIELD_2, Symboled[KEY_3], FIELD_3])(firstHlistToTuple2)
      ).imap(tuple2ToHlist[Symboled[KEY_1], FIELD_1, FieldType[Symboled[KEY_2], FIELD_2] :: FieldType[Symboled[KEY_3], FIELD_3] :: HNil])(hlistToTuple2)
        .imap(generic.from)(generic.to)
    }
  }

  final case class FieldBuilder4[KEY_1, FIELD_1, KEY_2, FIELD_2, KEY_3, FIELD_3, KEY_4, FIELD_4](reader1: NamedReader[FIELD_1], reader2: NamedReader[FIELD_2], reader3: NamedReader[FIELD_3], reader4: NamedReader[FIELD_4]) {
    def and[KEY_5, FIELD_5](fieldBuilder: FieldBuilder1[KEY_5, FIELD_5]): FieldBuilder5[KEY_1, FIELD_1, KEY_2, FIELD_2, KEY_3, FIELD_3, KEY_4, FIELD_4, KEY_5, FIELD_5] =
      FieldBuilder5(reader1, reader2, reader3, reader4, fieldBuilder.reader1)

    def build[TYPE <: Product](implicit
      semigroupal: Semigroupal[Program[ComplexSchemaAlgebra, ?]],
      invariant: Invariant[Program[ComplexSchemaAlgebra, ?]],
      generic: LabelledGeneric.Aux[TYPE, FieldType[Symboled[KEY_1], FIELD_1] :: FieldType[Symboled[KEY_2], FIELD_2] :: FieldType[Symboled[KEY_3], FIELD_3] :: FieldType[Symboled[KEY_4], FIELD_4] :: HNil]
    ): Program[ComplexSchemaAlgebra, TYPE] = {
      reader1.program.product(
        reader2.program.product(
          reader3.program.product(reader4.program).imap(firstTuple2ToHlist[Symboled[KEY_3], FIELD_3, Symboled[KEY_4], FIELD_4])(firstHlistToTuple2)
        ).imap(tuple2ToHlist[Symboled[KEY_2], FIELD_2, FieldType[Symboled[KEY_3], FIELD_3] :: FieldType[Symboled[KEY_4], FIELD_4] :: HNil])(hlistToTuple2)
      ).imap(tuple2ToHlist[Symboled[KEY_1], FIELD_1, FieldType[Symboled[KEY_2], FIELD_2] :: FieldType[Symboled[KEY_3], FIELD_3] :: FieldType[Symboled[KEY_4], FIELD_4] :: HNil])(hlistToTuple2)
        .imap(generic.from)(generic.to)
    }
  }

  final case class FieldBuilder5[KEY_1, FIELD_1, KEY_2, FIELD_2, KEY_3, FIELD_3, KEY_4, FIELD_4, KEY_5, FIELD_5](reader1: NamedReader[FIELD_1], reader2: NamedReader[FIELD_2], reader3: NamedReader[FIELD_3], reader4: NamedReader[FIELD_4], reader5: NamedReader[FIELD_5]) {
    def build[TYPE <: Product](implicit
      semigroupal: Semigroupal[Program[ComplexSchemaAlgebra, ?]],
      invariant: Invariant[Program[ComplexSchemaAlgebra, ?]],
      generic: LabelledGeneric.Aux[TYPE, FieldType[Symboled[KEY_1], FIELD_1] :: FieldType[Symboled[KEY_2], FIELD_2] :: FieldType[Symboled[KEY_3], FIELD_3] :: FieldType[Symboled[KEY_4], FIELD_4] :: FieldType[Symboled[KEY_5], FIELD_5] :: HNil]
    ): Program[ComplexSchemaAlgebra, TYPE] = {
      reader1.program.product(
        reader2.program.product(
          reader3.program.product(
            reader4.program.product(reader5.program).imap(firstTuple2ToHlist[Symboled[KEY_4], FIELD_4, Symboled[KEY_5], FIELD_5])(firstHlistToTuple2)
          ).imap(tuple2ToHlist[Symboled[KEY_3], FIELD_3, FieldType[Symboled[KEY_4], FIELD_4] :: FieldType[Symboled[KEY_5], FIELD_5] :: HNil])(hlistToTuple2)
        ).imap(tuple2ToHlist[Symboled[KEY_2], FIELD_2, FieldType[Symboled[KEY_3], FIELD_3] :: FieldType[Symboled[KEY_4], FIELD_4] :: FieldType[Symboled[KEY_5], FIELD_5] :: HNil])(hlistToTuple2)
      ).imap(tuple2ToHlist[Symboled[KEY_1], FIELD_1, FieldType[Symboled[KEY_2], FIELD_2] :: FieldType[Symboled[KEY_3], FIELD_3] :: FieldType[Symboled[KEY_4], FIELD_4] :: FieldType[Symboled[KEY_5], FIELD_5] :: HNil])(hlistToTuple2)
        .imap(generic.from)(generic.to)
    }
  }

  private def firstTuple2ToHlist[KEY_1, FIELD_1, KEY_2, FIELD_2](tuple: (FIELD_1, FIELD_2)): FieldType[KEY_1, FIELD_1] :: FieldType[KEY_2, FIELD_2] :: HNil = tuple2ToHlist[KEY_1, FIELD_1, FieldType[KEY_2, FIELD_2] :: HNil]((tuple._1, labelled.field[KEY_2](tuple._2)  :: HNil))
  private def firstHlistToTuple2[KEY_1, FIELD_1, KEY_2, FIELD_2](hlist: FieldType[KEY_1, FIELD_1] :: FieldType[KEY_2, FIELD_2] :: HNil): (FIELD_1, FIELD_2) = {
    val tuple = hlistToTuple2(hlist)
    (tuple._1, tuple._2.head)
  }
  private def tuple2ToHlist[KEY, FIELD, LIST <: HList](tuple: (FIELD, LIST)): FieldType[KEY, FIELD] :: LIST = labelled.field[KEY](tuple._1) :: tuple._2
  private def hlistToTuple2[A, B <: HList](hlist: A :: B): (A, B) = (hlist.head, hlist.tail)
}
