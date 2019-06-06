package io.github.methrat0n.restruct.writers

import java.time.{LocalDate, LocalTime, ZonedDateTime}

import io.github.methrat0n.restruct.constraints.Constraint
import io.github.methrat0n.restruct.schema.Path.\
import io.github.methrat0n.restruct.schema._
import play.api.libs.json._

object json {
  implicit val stringInterpreter: SimpleInterpreter[Writes, String] = new SimpleInterpreter[Writes, String] {
    override def schema: Writes[String] = Writes.StringWrites
  }
  implicit val decimalInterpreter: SimpleInterpreter[Writes, Double] = new SimpleInterpreter[Writes, Double] {
    override def schema: Writes[Double] = Writes.DoubleWrites
  }
  implicit val integerInterpreter: SimpleInterpreter[Writes, Int] = new SimpleInterpreter[Writes, Int] {
    override def schema: Writes[Int] = Writes.IntWrites
  }
  implicit val booleanInterpreter: SimpleInterpreter[Writes, Boolean] = new SimpleInterpreter[Writes, Boolean] {
    override def schema: Writes[Boolean] = Writes.BooleanWrites
  }
  implicit val charInterpreter: SimpleInterpreter[Writes, Char] = new SimpleInterpreter[Writes, Char] {
    override def schema: Writes[Char] = Writes[Char](char => JsString(char.toString))
  }
  implicit val byteInterpreter: SimpleInterpreter[Writes, Byte] = new SimpleInterpreter[Writes, Byte] {
    override def schema: Writes[Byte] =  Writes.ByteWrites
  }
  implicit val shortInterpreter: SimpleInterpreter[Writes, Short] = new SimpleInterpreter[Writes, Short] {
    override def schema: Writes[Short] = Writes.ShortWrites
  }
  implicit val floatInterpreter: SimpleInterpreter[Writes, Float] = new SimpleInterpreter[Writes, Float] {
    override def schema: Writes[Float] = Writes.FloatWrites
  }
  implicit val bigDecimalInterpreter: SimpleInterpreter[Writes, BigDecimal] = new SimpleInterpreter[Writes, BigDecimal] {
    override def schema: Writes[BigDecimal] = Writes.BigDecimalWrites
  }
  implicit val longSInterpreter: SimpleInterpreter[Writes, Long] = new SimpleInterpreter[Writes, Long] {
    override def schema: Writes[Long] = Writes.LongWrites
  }
  implicit val bigIntInterpreter: SimpleInterpreter[Writes, BigInt] = new SimpleInterpreter[Writes, BigInt] {
    override def schema: Writes[BigInt] = Writes[BigInt](bigInt => JsNumber(BigDecimal(bigInt)))
  }
  implicit val dateTimeInterpreter: SimpleInterpreter[Writes, ZonedDateTime] = new SimpleInterpreter[Writes, ZonedDateTime] {
    override def schema: Writes[ZonedDateTime] = Writes.DefaultZonedDateTimeWrites
  }
  implicit val timeInterpreter: SimpleInterpreter[Writes, LocalTime] = new SimpleInterpreter[Writes, LocalTime] {
    override def schema: Writes[LocalTime] = Writes.DefaultLocalTimeWrites
  }
  implicit val dateInterpreter: SimpleInterpreter[Writes, LocalDate] = new SimpleInterpreter[Writes, LocalDate] {
    override def schema: Writes[LocalDate] = Writes.DefaultLocalDateWrites
  }

  import language.higherKinds
  implicit def manyInterpreter[Collection[A] <: Traversable[A], T](implicit originalInterpreter: Interpreter[Writes, T]): ManyInterpreter[Writes, T, Collection] = new ManyInterpreter[Writes, T, Collection] {
    override def originalInterpreter: Interpreter[Writes, T] = originalInterpreter

    override def many(schema: Writes[T]): Writes[Collection[T]] = Writes.traversableWrites(schema)
  }

  implicit def requiredInterpreter[T, P <: Path](implicit pathBuilder: PathBuilder[P]): RequiredInterpreter[Writes, P, T] = (path: P, schema: Writes[T], default: Option[T]) =>
    pathBuilder.toJsPath(path).write(schema)

  implicit def optionalInterpreter[T, P <: Path](implicit originalInterpreter: Interpreter[Writes, T], pathBuilder: PathBuilder[P]): OptionalInterpreter[Writes, P, T] = new OptionalInterpreter[Writes, P, T] {
    override def originalInterpreter: Interpreter[Writes, T] = originalInterpreter

    override def optional(path: P, schema: Writes[T], default: Option[Option[T]]): Writes[Option[T]] =
      pathBuilder.toJsPath(path).writeNullable(schema)
  }

  implicit def constrainedInterpreter[T](implicit interpreter: Interpreter[Writes, T]): ConstrainedInterpreter[Writes, T] = new ConstrainedInterpreter[Writes, T] {
    override def originalInterpreter: Interpreter[Writes, T] = interpreter

    override def verifying(schema: Writes[T], constraint: Constraint[T]): Writes[T] =
      schema
  }

  import play.api.libs.functional.syntax._
  implicit def invariantInterpreter[A, B](implicit interpreterA: Interpreter[Writes, A], interpreterB: Interpreter[Writes, B]): InvariantInterpreter[Writes, A, B] = new InvariantInterpreter[Writes, A, B] {
    override def originalInterpreterA: Interpreter[Writes, A] = interpreterA

    override def originalInterpreterB: Interpreter[Writes, B] = interpreterB

    override def imap(fa: Writes[A])(f: A => B)(g: B => A): Writes[B] =
      fa.contramap(g)
  }

  implicit def semiGroupalInterpreter[A, B](implicit interpreterA: Interpreter[Writes, A], interpreterB: Interpreter[Writes, B]): SemiGroupalInterpreter[Writes, A, B] = new SemiGroupalInterpreter[Writes, A, B] {
    override def originalInterpreterA: Interpreter[Writes, A] = interpreterA

    override def originalInterpreterB: Interpreter[Writes, B] = interpreterB

    override def product(fa: Writes[A], fb: Writes[B]): Writes[(A, B)] =
      (o: (A, B)) => (fa.writes(o._1), fb.writes(o._2)) match {
        case (a @ JsObject(_), b @ JsObject(_)) => a ++ b
        case (a @ JsArray(_), b @ JsArray(_))   => a ++ b
        case (selected, _)                      => selected
      }
  }

  implicit def oneOfInterpreter[A, B](implicit interpreterA: Interpreter[Writes, A], interpreterB: Interpreter[Writes, B]): OneOfInterpreter[Writes, A, B] = new OneOfInterpreter[Writes, A, B] {
    override def originalInterpreterA: Interpreter[Writes, A] = interpreterA

    override def originalInterpreterB: Interpreter[Writes, B] = interpreterB

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
    override def or(fa: Writes[A], fb: Writes[B]): Writes[Either[A, B]] =
      Writes {
        case Left(input)  => fa.writes(input)
        case Right(input) => fb.writes(input)
      }
  }

  trait PathBuilder[P <: Path] {
    def toJsPath(path: P): JsPath
  }

  object PathBuilder {
    implicit def stringStep2JsPath[RemainingPath <: Path](implicit remainingPath: PathBuilder[RemainingPath]) = new PathBuilder[RemainingPath \ String] {
      override def toJsPath(path: RemainingPath \ String): JsPath = JsPath(remainingPath.toJsPath(path.previousSteps).path :+ KeyPathNode(path.step))
    }

    implicit def emptyStep2JsPath: PathBuilder[PathNil] = (_: PathNil) => JsPath(List.empty)
  }

}
