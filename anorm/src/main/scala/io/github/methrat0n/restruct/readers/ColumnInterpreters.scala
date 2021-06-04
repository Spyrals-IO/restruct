package io.github.methrat0n.restruct.readers

import java.time.{LocalDate, LocalTime, ZonedDateTime}

import anorm.{Column, MetaDataItem, SqlRequestError}
import io.github.methrat0n.restruct.constraints.Constraint
import io.github.methrat0n.restruct.schema.Interpreter
import io.github.methrat0n.restruct.schema.Interpreter._

import scala.collection.Factory
import scala.language.higherKinds
import scala.reflect.ClassTag

trait ColumnInterpreters extends ColumnMiddlePriority {
  implicit val columnCharReadInterpreter: SimpleInterpreter[Column, Char] = new SimpleInterpreter[Column, Char] {
    override def schema: Column[Char] = Column.columnToChar
  }
  implicit val columnShortReadInterpreter: SimpleInterpreter[Column, Short] = new SimpleInterpreter[Column, Short] {
    override def schema: Column[Short] = Column.columnToShort
  }
  implicit val columnFloatReadInterpreter: SimpleInterpreter[Column, Float] = new SimpleInterpreter[Column, Float] {
    override def schema: Column[Float] = Column.columnToFloat
  }
  implicit val columnDecimalReadInterpreter: SimpleInterpreter[Column, Double] = new SimpleInterpreter[Column, Double] {
    override def schema: Column[Double] = Column.columnToDouble
  }
  implicit val columnBigDecimalReadInterpreter: SimpleInterpreter[Column, BigDecimal] = new SimpleInterpreter[Column, BigDecimal] {
    override def schema: Column[BigDecimal] = Column.columnToScalaBigDecimal
  }
  implicit val columnIntegerReadInterpreter: SimpleInterpreter[Column, Int] = new SimpleInterpreter[Column, Int] {
    override def schema: Column[Int] = Column.columnToInt
  }
  implicit val columnLongReadInterpreter: SimpleInterpreter[Column, Long] = new SimpleInterpreter[Column, Long] {
    override def schema: Column[Long] = Column.columnToLong
  }
  implicit val columnBigIntReadInterpreter: SimpleInterpreter[Column, BigInt] = new SimpleInterpreter[Column, BigInt] {
    override def schema: Column[BigInt] = Column.columnToBigInt
  }
  implicit val columnBooleanReadInterpreter: SimpleInterpreter[Column, Boolean] = new SimpleInterpreter[Column, Boolean] {
    override def schema: Column[Boolean] = Column.columnToBoolean
  }
  implicit val columnStringReadInterpreter: SimpleInterpreter[Column, String] = new SimpleInterpreter[Column, String] {
    override def schema: Column[String] = Column.columnToString
  }
  implicit val columnDateTimeReadInterpreter: SimpleInterpreter[Column, ZonedDateTime] = new SimpleInterpreter[Column, ZonedDateTime] {
    override def schema: Column[ZonedDateTime] = Column.columnToZonedDateTime
  }
  implicit val columnTimeReadInterpreter: SimpleInterpreter[Column, LocalTime] = new SimpleInterpreter[Column, LocalTime] {
    override def schema: Column[LocalTime] = columnDateTimeReadInterpreter.schema.map(_.toLocalTime)
  }
  implicit val columnDateReadInterpreter: SimpleInterpreter[Column, LocalDate] = new SimpleInterpreter[Column, LocalDate] {
    override def schema: Column[LocalDate] = Column.columnToLocalDate
  }
}

trait ColumnMiddlePriority extends ColumnLowPriority {
  implicit def manyReadInterpreter[T, Collection[A] <: Iterable[A], UnderlyingInterpreter <: Interpreter[Column, T]](implicit
    algebra: UnderlyingInterpreter,
    factory: Factory[T, Collection[T]],
    tag: ClassTag[T]
  ): ManyInterpreter[Column, T, Collection, UnderlyingInterpreter] = new ManyInterpreter[Column, T, Collection, UnderlyingInterpreter] {
    override def originalInterpreter: UnderlyingInterpreter = algebra

    override def many(schema: Column[T]): Column[Collection[T]] =
      Column.columnToArray[T](schema, tag).map(arr => factory.fromSpecific(arr.toSeq))
  }

  implicit def semiGroupalReadInterpreter[A, B, AInterpreter <: Interpreter[Column, A], BInterpreter <: Interpreter[Column, B]](implicit algebraA: AInterpreter, algebraB: BInterpreter): SemiGroupalInterpreter[Column, A, B, AInterpreter, BInterpreter] = new SemiGroupalInterpreter[Column, A, B, AInterpreter, BInterpreter] {
    override def originalInterpreterA: AInterpreter = algebraA

    override def originalInterpreterB: BInterpreter = algebraB

    override def product(fa: Column[A], fb: Column[B]): Column[(A, B)] = Column((any: Any, metaDataItem: MetaDataItem) =>
      fa(any, metaDataItem).flatMap(a => fb(any, metaDataItem).map(b => (a, b)))
    )
  }

  final case class MultipleSqlRequestError(msg: String, error: Throwable*) extends RuntimeException(msg) {
    val errors: List[Throwable] = error.toList

    errors.foreach(super.addSuppressed)
  }

  implicit def oneOfReadInterpreter[A, B, AInterpreter <: Interpreter[Column, A], BInterpreter <: Interpreter[Column, B]](implicit algebraA: AInterpreter, algebraB: BInterpreter): OneOfInterpreter[Column, A, B, AInterpreter, BInterpreter] = new OneOfInterpreter[Column, A, B, AInterpreter, BInterpreter] {
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
    override def or(fa: Column[A], fb: Column[B]): Column[Either[A, B]] = Column((any: Any, metaDataItem: MetaDataItem) =>
      fa(any, metaDataItem) match {
        case Left(errorA) => fb(any, metaDataItem) match {
          case Left(errorB) => Left(SqlRequestError(
            MultipleSqlRequestError(
              s"Multiple errors: (${errorA.message}, ${errorB.message})",
              errorA.toFailure.exception,
              errorB.toFailure.exception
            )))
          case Right(value) => Right(Right[A, B](value))
        }
        case Right(value) => Right(Left[A, B](value))
      }
    )
  }
}

trait ColumnLowPriority extends ColumnFinalPriority {

  implicit def invariantReadInterpreter[A, B, UnderlyingInterpreter <: Interpreter[Column, A]](implicit underlying: UnderlyingInterpreter): InvariantInterpreter[Column, A, B, UnderlyingInterpreter] = new InvariantInterpreter[Column, A, B, UnderlyingInterpreter] {
    override def underlyingInterpreter: UnderlyingInterpreter = underlying

    override def imap(fa: Column[A])(f: A => B)(g: B => A): Column[B] =
      fa.map(f)
  }

}

trait ColumnFinalPriority {
  implicit def constrainedReadInterpreter[T, UnderlyingInterpreter <: Interpreter[Column, T]](implicit algebra: UnderlyingInterpreter): ConstrainedInterpreter[Column, T, UnderlyingInterpreter] = new ConstrainedInterpreter[Column, T, UnderlyingInterpreter] {
    override def originalInterpreter: UnderlyingInterpreter = algebra

    override def verifying(schema: Column[T], constraint: Constraint[T]): Column[T] = {
      schema.mapResult(value => {
        if(constraint.validate(value))
          Right(value)
        else
          Left(SqlRequestError(new RuntimeException(s"Invalid value for constraint ${constraint.name} with parameters ${constraint.args}")))
      })
    }
  }
}