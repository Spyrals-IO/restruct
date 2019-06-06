package io.github.methrat0n.restruct.readers

import java.time.{ LocalDate, LocalTime, ZonedDateTime }

import io.github.methrat0n.restruct.constraints.Constraint
import io.github.methrat0n.restruct.schema.Path.\
import io.github.methrat0n.restruct.schema._
import play.api.libs.json._

import scala.collection.GenIterable
import scala.collection.generic.CanBuildFrom

object json {
  implicit val charAlgebra: SimpleInterpreter[Reads, Char] = new SimpleInterpreter[Reads, Char] {
    override def schema: Reads[Char] = {
      case JsString(string) if string.length == 1 => JsSuccess(string.charAt(0))
      case _                                      => JsError()
    }
  }

  implicit val byteAlgebra: SimpleInterpreter[Reads, Byte] = new SimpleInterpreter[Reads, Byte] {
    override def schema: Reads[Byte] = Reads.ByteReads
  }

  implicit val shortAlgebra: SimpleInterpreter[Reads, Short] = new SimpleInterpreter[Reads, Short] {
    override def schema: Reads[Short] = Reads.ShortReads
  }

  implicit val floatAlgebra: SimpleInterpreter[Reads, Float] = new SimpleInterpreter[Reads, Float] {
    override def schema: Reads[Float] = Reads.FloatReads
  }

  implicit val decimalAlgebra: SimpleInterpreter[Reads, Double] = new SimpleInterpreter[Reads, Double] {
    override def schema: Reads[Double] = Reads.DoubleReads
  }

  implicit val bigDecimalAlgebra: SimpleInterpreter[Reads, BigDecimal] = new SimpleInterpreter[Reads, BigDecimal] {
    override def schema: Reads[BigDecimal] = Reads.bigDecReads
  }

  implicit val integerAlgebra: SimpleInterpreter[Reads, Int] = new SimpleInterpreter[Reads, Int] {
    override def schema: Reads[Int] = Reads.IntReads
  }

  implicit val longAlgebra: SimpleInterpreter[Reads, Long] = new SimpleInterpreter[Reads, Long] {
    override def schema: Reads[Long] = Reads.LongReads
  }

  implicit val bigIntAlgebra: SimpleInterpreter[Reads, BigInt] = new SimpleInterpreter[Reads, BigInt] {
    override def schema: Reads[BigInt] = {
      case JsString(s) =>
        try {
          JsSuccess(BigInt(s))
        }
        catch {
          case _: NumberFormatException => JsError(JsonValidationError("error.expected.numberformatexception"))
        }
      case JsNumber(d) =>
        d.toBigIntExact() match {
          case Some(bigInt) => JsSuccess(bigInt)
          case None         => JsError(JsonValidationError("error.expected.numberformatexception"))
        }
      case _ => JsError(JsonValidationError("error.expected.jsnumberorjsstring"))
    }
  }

  implicit val booleanAlgebra: SimpleInterpreter[Reads, Boolean] = new SimpleInterpreter[Reads, Boolean] {
    override def schema: Reads[Boolean] = Reads.BooleanReads
  }

  implicit val stringAlgebra: SimpleInterpreter[Reads, String] = new SimpleInterpreter[Reads, String] {
    override def schema: Reads[String] = Reads.StringReads
  }

  implicit val dateTimeAlgebra: SimpleInterpreter[Reads, ZonedDateTime] = new SimpleInterpreter[Reads, ZonedDateTime] {
    override def schema: Reads[ZonedDateTime] = Reads.DefaultZonedDateTimeReads
  }

  implicit val timeAlgebra: SimpleInterpreter[Reads, LocalTime] = new SimpleInterpreter[Reads, LocalTime] {
    override def schema: Reads[LocalTime] = Reads.DefaultLocalTimeReads
  }

  implicit val dateAlgebra: SimpleInterpreter[Reads, LocalDate] = new SimpleInterpreter[Reads, LocalDate] {
    override def schema: Reads[LocalDate] = Reads.DefaultLocalDateReads
  }

  import language.higherKinds
  implicit def manyAlgebra[T, Collection[A] <: GenIterable[A]](implicit algebra: Interpreter[Reads, T], canBuildFrom: CanBuildFrom[Collection[_], T, Collection[T]]): ManyInterpreter[Reads, T, Collection] = new ManyInterpreter[Reads, T, Collection] {
    override def originalInterpreter: Interpreter[Reads, T] = algebra

    override def many(schema: Reads[T]): Reads[Collection[T]] =
      Reads.traversableReads[Collection, T](canBuildFrom, schema)
  }

  implicit def requiredAlgebra[T, P <: Path](implicit pathBuilder: PathBuilder[P]): RequiredInterpreter[Reads, P, T] = (path: P, schema: Reads[T], default: Option[T]) =>
    default
      .map(default => pathBuilder.toJsPath(path).readWithDefault(default)(schema))
      .getOrElse(pathBuilder.toJsPath(path).read(schema))

  implicit def optionalAlgebra[T, P <: Path](implicit algebra: Interpreter[Reads, T], pathBuilder: PathBuilder[P]): OptionalInterpreter[Reads, P, T] = new OptionalInterpreter[Reads, P, T] {
    override def originalInterpreter: Interpreter[Reads, T] = algebra

    override def optional(path: P, schema: Reads[T], default: Option[Option[T]]): Reads[Option[T]] =
      pathBuilder.toJsPath(path).readNullableWithDefault(default.flatten)(schema)
  }

  implicit def constrainedAlgebra[T](implicit algebra: Interpreter[Reads, T]): ConstrainedInterpreter[Reads, T] = new ConstrainedInterpreter[Reads, T] {
    override def originalInterpreter: Interpreter[Reads, T] = algebra

    override def verifying(schema: Reads[T], constraint: Constraint[T]): Reads[T] =
      schema.filter(JsonValidationError(s"error.constraints.${constraint.name}", constraint.args: _*))(constraint.validate)
  }

  implicit def invariantAlgebra[A, B](implicit algebraA: Interpreter[Reads, A], algebraB: Interpreter[Reads, B]): InvariantInterpreter[Reads, A, B] = new InvariantInterpreter[Reads, A, B] {
    override def originalInterpreterA: Interpreter[Reads, A] = algebraA

    override def originalInterpreterB: Interpreter[Reads, B] = algebraB

    override def imap(fa: Reads[A])(f: A => B)(g: B => A): Reads[B] =
      fa.map(f)
  }

  import play.api.libs.functional.syntax._
  implicit def semiGroupalAlgebra[A, B](implicit algebraA: Interpreter[Reads, A], algebraB: Interpreter[Reads, B]): SemiGroupalInterpreter[Reads, A, B] = new SemiGroupalInterpreter[Reads, A, B] {
    override def originalInterpreterA: Interpreter[Reads, A] = algebraA

    override def originalInterpreterB: Interpreter[Reads, B] = algebraB

    override def product(fa: Reads[A], fb: Reads[B]): Reads[(A, B)] = (fa and fb).tupled
  }

  implicit def oneOfAlgebra[A, B](implicit algebraA: Interpreter[Reads, A], algebraB: Interpreter[Reads, B]): OneOfInterpreter[Reads, A, B] = new OneOfInterpreter[Reads, A, B] {
    override def originalInterpreterA: Interpreter[Reads, A] = algebraA

    override def originalInterpreterB: Interpreter[Reads, B] = algebraB

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
    override def or(fa: Reads[A], fb: Reads[B]): Reads[Either[A, B]] = Reads(jsValue =>
      fa.reads(jsValue) match {
        case aSuccess @ JsSuccess(_, _) => aSuccess.map(Left.apply)
        case aError: JsError => fb.reads(jsValue) match {
          case bSuccess @ JsSuccess(_, _) => bSuccess.map(Right.apply)
          case bError: JsError            => aError ++ bError
        }
      })
  }

  trait PathBuilder[P <: Path] {
    def toJsPath(path: P): JsPath
  }

  object PathBuilder {
    implicit def stringStep2JsPath[RemainingPath <: Path](implicit remainingPath: PathBuilder[RemainingPath]) = new PathBuilder[RemainingPath \ String] {
      override def toJsPath(path: RemainingPath \ String): JsPath = JsPath(remainingPath.toJsPath(path.previousSteps).path :+ KeyPathNode(path.step))
    }

    implicit def intStep2JsPath[RemainingPath <: Path](implicit remainingPath: PathBuilder[RemainingPath]) = new PathBuilder[RemainingPath \ Int] {
      override def toJsPath(path: RemainingPath \ Int): JsPath = JsPath(remainingPath.toJsPath(path.previousSteps).path :+ IdxPathNode(path.step))
    }

    implicit def emptyStep2JsPath: PathBuilder[PathNil] = (_: PathNil) => JsPath(List.empty)
  }
}
