package io.github.methrat0n.restruct.handlers

import java.time.{LocalDate, LocalTime, ZonedDateTime}

import io.github.methrat0n.restruct.constraints.Constraint
import io.github.methrat0n.restruct.schema.Interpreter._
import io.github.methrat0n.restruct.schema.{Interpreter, Path}
import play.api.libs.json.{Format, Reads, Writes}
import io.github.methrat0n.restruct.readers.json._
import io.github.methrat0n.restruct.writers.json._

import language.higherKinds
import scala.collection.Factory

object json extends MiddlePriority {

  implicit val charFormatInterpreter: SimpleInterpreter[Format, Char] = new SimpleInterpreter[Format, Char] {
    override def schema: Format[Char] = Format(charReadInterpreter.schema, charWritesInterpreter.schema)
  }

  implicit val byteFormatInterpreter: SimpleInterpreter[Format, Byte] = new SimpleInterpreter[Format, Byte] {
    override def schema: Format[Byte] = Format(byteReadInterpreter.schema, byteWritesInterpreter.schema)
  }

  implicit val shortFormatInterpreter: SimpleInterpreter[Format, Short] = new SimpleInterpreter[Format, Short] {
    override def schema: Format[Short] = Format(shortReadInterpreter.schema, shortWritesInterpreter.schema)
  }

  implicit val floatFormatInterpreter: SimpleInterpreter[Format, Float] = new SimpleInterpreter[Format, Float] {
    override def schema: Format[Float] = Format(floatReadInterpreter.schema, floatWritesInterpreter.schema)
  }

  implicit val decimalFormatInterpreter: SimpleInterpreter[Format, Double] = new SimpleInterpreter[Format, Double] {
    override def schema: Format[Double] = Format(decimalReadInterpreter.schema, decimalWritesInterpreter.schema)
  }

  implicit val bigDecimalFormatInterpreter: SimpleInterpreter[Format, BigDecimal] = new SimpleInterpreter[Format, BigDecimal] {
    override def schema: Format[BigDecimal] = Format(bigDecimalReadInterpreter.schema, bigDecimalWritesInterpreter.schema)
  }

  implicit val integerFormatInterpreter: SimpleInterpreter[Format, Int] = new SimpleInterpreter[Format, Int] {
    override def schema: Format[Int] = Format(integerReadInterpreter.schema, integerWritesInterpreter.schema)
  }

  implicit val longFormatInterpreter: SimpleInterpreter[Format, Long] = new SimpleInterpreter[Format, Long] {
    override def schema: Format[Long] = Format(longReadInterpreter.schema, longWritesInterpreter.schema)
  }

  implicit val bigIntFormatInterpreter: SimpleInterpreter[Format, BigInt] = new SimpleInterpreter[Format, BigInt] {
    override def schema: Format[BigInt] = Format(bigIntReadInterpreter.schema, bigIntWritesInterpreter.schema)
  }

  implicit val booleanFormatInterpreter: SimpleInterpreter[Format, Boolean] = new SimpleInterpreter[Format, Boolean] {
    override def schema: Format[Boolean] = Format(booleanReadInterpreter.schema, booleanWritesInterpreter.schema)
  }

  implicit val stringFormatInterpreter: SimpleInterpreter[Format, String] = new SimpleInterpreter[Format, String] {
    override def schema: Format[String] = Format(stringReadInterpreter.schema, stringWritesInterpreter.schema)
  }

  implicit val dateTimeFormatInterpreter: SimpleInterpreter[Format, ZonedDateTime] = new SimpleInterpreter[Format, ZonedDateTime] {
    override def schema: Format[ZonedDateTime] = Format(dateTimeReadInterpreter.schema, dateTimeWritesInterpreter.schema)
  }

  implicit val timeFormatInterpreter: SimpleInterpreter[Format, LocalTime] = new SimpleInterpreter[Format, LocalTime] {
    override def schema: Format[LocalTime] = Format(timeReadInterpreter.schema, timeWritesInterpreter.schema)
  }

  implicit val dateFormatInterpreter: SimpleInterpreter[Format, LocalDate] = new SimpleInterpreter[Format, LocalDate] {
    override def schema: Format[LocalDate] = Format(dateReadInterpreter.schema, dateWritesInterpreter.schema)
  }

}

trait MiddlePriority extends LowPriority {

  implicit def manyFormatInterpreter[T, Collection[A] <: Iterable[A], UnderlyingInterpreter <: Interpreter[Format, T]](implicit
    algebra: UnderlyingInterpreter,
    factory: Factory[T, Collection[T]]
  ): ManyInterpreter[Format, T, Collection, UnderlyingInterpreter] =
    new ManyInterpreter[Format, T, Collection, UnderlyingInterpreter] {
      override def originalInterpreter: UnderlyingInterpreter = algebra

      override def many(schema: Format[T]): Format[Collection[T]] =
        Format(
          manyReadInterpreter[T, Collection, Interpreter[Reads, T]].many(schema),
          manyWritesInterpreter[T, Collection, Interpreter[Writes, T]].many(schema)
        )
    }

  implicit def optionalFormatInterpreter[T, P <: Path, UnderlyingInterpreter <: Interpreter[Format, T]](implicit
    algebra: UnderlyingInterpreter,
    readsPathBuilder: ReadsPathBuilder[P],
    writesPathBuilder: WritesPathBuilder[P],
  ): OptionalInterpreter[Format, P, T, UnderlyingInterpreter] =
    new OptionalInterpreter[Format, P, T, UnderlyingInterpreter] {
      override def originalInterpreter: UnderlyingInterpreter = algebra

      override def optional(path: P, schema: Format[T], default: Option[Option[T]]): Format[Option[T]] =
        Format(
          optionalReadInterpreter[T, P, UnderlyingInterpreter].optional(path, schema, default),
          optionalWritesInterpreter[T, P, UnderlyingInterpreter].optional(path, schema, default)
        )
    }

  implicit def semiGroupalFormatInterpreter[A, B, AInterpreter <: Interpreter[Format, A], BInterpreter <: Interpreter[Format, B]](implicit
    algebraA: AInterpreter,
    algebraB: BInterpreter
  ): SemiGroupalInterpreter[Format, A, B, AInterpreter, BInterpreter] =
    new SemiGroupalInterpreter[Format, A, B, AInterpreter, BInterpreter] {
      override def originalInterpreterA: AInterpreter = algebraA

      override def originalInterpreterB: BInterpreter = algebraB

      override def product(fa: Format[A], fb: Format[B]): Format[(A, B)] =
        Format(
          semiGroupalReadInterpreter[A, B, AInterpreter, BInterpreter].product(fa, fb),
          semiGroupalWritesInterpreter[A, B, AInterpreter, BInterpreter].product(fa, fb)
        )
    }

  implicit def oneOfFormatInterpreter[A, B, AInterpreter <: Interpreter[Format, A], BInterpreter <: Interpreter[Format, B]](implicit
    algebraA: AInterpreter,
    algebraB: BInterpreter
  ): OneOfInterpreter[Format, A, B, AInterpreter, BInterpreter] =
    new OneOfInterpreter[Format, A, B, AInterpreter, BInterpreter] {
      override def originalInterpreterA: AInterpreter = algebraA

      override def originalInterpreterB: BInterpreter = algebraB

      /**
       * Should return a success, if any, or concatenate errors.
       *
       * fa == sucess => fa result in Left
       * fa == error && fb == sucess => fb result in Right
       * fa == error && fb == error => concatenate fa and fb errors into F error handling
       *
       * If two successes are found, fa will be choosen.
       *
       * @return F in error (depends on the implementing F) or successful F with one of the two value
       */
      override def or(fa: Format[A], fb: Format[B]): Format[Either[A, B]] =
        Format(
          oneOfReadInterpreter[A, B, AInterpreter, BInterpreter].or(fa, fb),
          oneOfWritesInterpreter[A, B, AInterpreter, BInterpreter].or(fa, fb)
        )
    }
}

trait LowPriority extends FinalPriority {

  implicit def invariantFormatInterpreter[A, B, UnderlyingInterpreter <: Interpreter[Format, A]](implicit underlying: UnderlyingInterpreter): InvariantInterpreter[Format, A, B, UnderlyingInterpreter] =
    new InvariantInterpreter[Format, A, B, UnderlyingInterpreter] {
      override def underlyingInterpreter: UnderlyingInterpreter = underlying

      override def imap(fa: Format[A])(f: A => B)(g: B => A): Format[B] =
        Format(
          invariantReadInterpreter[A, B, UnderlyingInterpreter].imap(fa)(f)(g),
          invariantWritesInterpreter[A, B, UnderlyingInterpreter].imap(fa)(f)(g)
        )
    }

  implicit def requiredFormatInterpreter[P <: Path, T, UnderlyingInterpreter <: Interpreter[Format, T]](implicit
    interpreter: UnderlyingInterpreter,
    readsPathBuilder: ReadsPathBuilder[P],
    writesPathBuilder: WritesPathBuilder[P]
  ): RequiredInterpreter[Format, P, T, UnderlyingInterpreter] =
    new RequiredInterpreter[Format, P, T, UnderlyingInterpreter] {
      override def originalInterpreter: UnderlyingInterpreter = interpreter

      override def required(path: P, schema: Format[T], default: Option[T]): Format[T] =
        Format(
          requiredReadInterpreter[P, T, UnderlyingInterpreter].required(path, schema, default),
          requiredWritesInterpreter[P, T, UnderlyingInterpreter].required(path, schema, default)
        )
    }
}

trait FinalPriority {
  implicit def constrainedFormatInterpreter[T, UnderlyingInterpreter <: Interpreter[Format, T]](implicit algebra: UnderlyingInterpreter): ConstrainedInterpreter[Format, T, UnderlyingInterpreter] =
    new ConstrainedInterpreter[Format, T, UnderlyingInterpreter] {
      override def originalInterpreter: UnderlyingInterpreter = algebra

      override def verifying(schema: Format[T], constraint: Constraint[T]): Format[T] =
        Format(
          constrainedReadInterpreter[T, UnderlyingInterpreter].verifying(schema, constraint),
          constrainedWritesInterpreter[T, UnderlyingInterpreter].verifying(schema, constraint)
        )
    }
}
