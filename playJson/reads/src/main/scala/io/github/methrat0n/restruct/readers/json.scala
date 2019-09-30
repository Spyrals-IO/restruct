package io.github.methrat0n.restruct.readers

import java.time.{ LocalDate, LocalTime, ZonedDateTime }

import io.github.methrat0n.restruct.constraints.Constraint
import io.github.methrat0n.restruct.readers.json.ReadsPathBuilder
import io.github.methrat0n.restruct.schema.Interpreter._
import io.github.methrat0n.restruct.schema.Path.\
import io.github.methrat0n.restruct.schema._
import play.api.libs.json._

import scala.collection.Factory

object json extends MiddlePriority {
  implicit val charReadInterpreter: SimpleInterpreter[Reads, Char] = new SimpleInterpreter[Reads, Char] {
    override def schema: Reads[Char] = {
      case JsString(string) if string.length == 1 => JsSuccess(string.charAt(0))
      case _                                      => JsError()
    }
  }

  implicit val byteReadInterpreter: SimpleInterpreter[Reads, Byte] = new SimpleInterpreter[Reads, Byte] {
    override def schema: Reads[Byte] = Reads.ByteReads
  }

  implicit val shortReadInterpreter: SimpleInterpreter[Reads, Short] = new SimpleInterpreter[Reads, Short] {
    override def schema: Reads[Short] = Reads.ShortReads
  }

  implicit val floatReadInterpreter: SimpleInterpreter[Reads, Float] = new SimpleInterpreter[Reads, Float] {
    override def schema: Reads[Float] = Reads.FloatReads
  }

  implicit val decimalReadInterpreter: SimpleInterpreter[Reads, Double] = new SimpleInterpreter[Reads, Double] {
    override def schema: Reads[Double] = Reads.DoubleReads
  }

  implicit val bigDecimalReadInterpreter: SimpleInterpreter[Reads, BigDecimal] = new SimpleInterpreter[Reads, BigDecimal] {
    override def schema: Reads[BigDecimal] = Reads.bigDecReads
  }

  implicit val integerReadInterpreter: SimpleInterpreter[Reads, Int] = new SimpleInterpreter[Reads, Int] {
    override def schema: Reads[Int] = Reads.IntReads
  }

  implicit val longReadInterpreter: SimpleInterpreter[Reads, Long] = new SimpleInterpreter[Reads, Long] {
    override def schema: Reads[Long] = Reads.LongReads
  }

  implicit val bigIntReadInterpreter: SimpleInterpreter[Reads, BigInt] = new SimpleInterpreter[Reads, BigInt] {
    override def schema: Reads[BigInt] = {
      case JsString(s) =>
        try {
          JsSuccess(BigInt(s))
        }
        catch {
          case _: NumberFormatException => JsError(JsonValidationError("error.expected.numberformatexception"))
        }
      case JsNumber(d) =>
        d.toBigIntExact match {
          case Some(bigInt) => JsSuccess(bigInt)
          case None         => JsError(JsonValidationError("error.expected.numberformatexception"))
        }
      case _ => JsError(JsonValidationError("error.expected.jsnumberorjsstring"))
    }
  }

  implicit val booleanReadInterpreter: SimpleInterpreter[Reads, Boolean] = new SimpleInterpreter[Reads, Boolean] {
    override def schema: Reads[Boolean] = Reads.BooleanReads
  }

  implicit val stringReadInterpreter: SimpleInterpreter[Reads, String] = new SimpleInterpreter[Reads, String] {
    override def schema: Reads[String] = Reads.StringReads
  }

  implicit val dateTimeReadInterpreter: SimpleInterpreter[Reads, ZonedDateTime] = new SimpleInterpreter[Reads, ZonedDateTime] {
    override def schema: Reads[ZonedDateTime] = Reads.DefaultZonedDateTimeReads
  }

  implicit val timeReadInterpreter: SimpleInterpreter[Reads, LocalTime] = new SimpleInterpreter[Reads, LocalTime] {
    override def schema: Reads[LocalTime] = Reads.DefaultLocalTimeReads
  }

  implicit val dateReadInterpreter: SimpleInterpreter[Reads, LocalDate] = new SimpleInterpreter[Reads, LocalDate] {
    override def schema: Reads[LocalDate] = Reads.DefaultLocalDateReads
  }

  trait ReadsPathBuilder[P <: Path] {
    def toJsPath(path: P): JsPath
  }

  object ReadsPathBuilder {
    implicit def stringStep2JsPath[RemainingPath <: Path](implicit remainingPath: ReadsPathBuilder[RemainingPath]): ReadsPathBuilder[RemainingPath \ String] =
      (path: RemainingPath \ String) => JsPath(remainingPath.toJsPath(path.previousSteps).path :+ KeyPathNode(path.step))
    implicit def intStep2JsPath[RemainingPath <: Path](implicit remainingPath: ReadsPathBuilder[RemainingPath]): ReadsPathBuilder[RemainingPath \ Int] =
      (path: RemainingPath \ Int) => JsPath(remainingPath.toJsPath(path.previousSteps).path :+ IdxPathNode(path.step))
    implicit def emptyStep2JsPath: ReadsPathBuilder[PathNil] =
      (_: PathNil) => JsPath(List.empty)
  }
}

trait MiddlePriority extends LowPriority {
  import language.higherKinds
  implicit def manyReadInterpreter[T, Collection[A] <: Iterable[A], UnderlyingInterpreter <: Interpreter[Reads, T]](implicit algebra: UnderlyingInterpreter, factory: Factory[T, Collection[T]]): ManyInterpreter[Reads, T, Collection, UnderlyingInterpreter] = new ManyInterpreter[Reads, T, Collection, UnderlyingInterpreter] {
    override def originalInterpreter: UnderlyingInterpreter = algebra

    override def many(schema: Reads[T]): Reads[Collection[T]] =
      Reads.traversableReads[Collection, T](factory, schema)
  }

  implicit def optionalReadInterpreter[T, P <: Path, UnderlyingInterpreter <: Interpreter[Reads, T]](implicit algebra: UnderlyingInterpreter, pathBuilder: ReadsPathBuilder[P]): OptionalInterpreter[Reads, P, T, UnderlyingInterpreter] = new OptionalInterpreter[Reads, P, T, UnderlyingInterpreter] {
    override def originalInterpreter: UnderlyingInterpreter = algebra

    override def optional(path: P, schema: Reads[T], default: Option[Option[T]]): Reads[Option[T]] =
      pathBuilder.toJsPath(path).readNullableWithDefault(default.flatten)(schema)
  }

  import play.api.libs.functional.syntax._
  implicit def semiGroupalReadInterpreter[A, B, AInterpreter <: Interpreter[Reads, A], BInterpreter <: Interpreter[Reads, B]](implicit algebraA: AInterpreter, algebraB: BInterpreter): SemiGroupalInterpreter[Reads, A, B, AInterpreter, BInterpreter] = new SemiGroupalInterpreter[Reads, A, B, AInterpreter, BInterpreter] {
    override def originalInterpreterA: AInterpreter = algebraA

    override def originalInterpreterB: BInterpreter = algebraB

    override def product(fa: Reads[A], fb: Reads[B]): Reads[(A, B)] = (fa and fb).tupled
  }

  implicit def oneOfReadInterpreter[A, B, AInterpreter <: Interpreter[Reads, A], BInterpreter <: Interpreter[Reads, B]](implicit algebraA: AInterpreter, algebraB: BInterpreter): OneOfInterpreter[Reads, A, B, AInterpreter, BInterpreter] = new OneOfInterpreter[Reads, A, B, AInterpreter, BInterpreter] {
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
    override def or(fa: Reads[A], fb: Reads[B]): Reads[Either[A, B]] = Reads(jsValue =>
      fa.reads(jsValue) match {
        case aSuccess @ JsSuccess(_, _) => aSuccess.map(Left.apply)
        case aError: JsError => fb.reads(jsValue) match {
          case bSuccess @ JsSuccess(_, _) => bSuccess.map(Right.apply)
          case bError: JsError            => aError ++ bError
        }
      })
  }
}

trait LowPriority extends FinalPriority {

  implicit def invariantReadInterpreter[A, B, UnderlyingInterpreter <: Interpreter[Reads, A]](implicit underlying: UnderlyingInterpreter): InvariantInterpreter[Reads, A, B, UnderlyingInterpreter] = new InvariantInterpreter[Reads, A, B, UnderlyingInterpreter] {
    override def underlyingInterpreter: UnderlyingInterpreter = underlying

    override def imap(fa: Reads[A])(f: A => B)(g: B => A): Reads[B] =
      fa.map(f)
  }

  implicit def requiredReadInterpreter[P <: Path, T, UnderlyingInterpreter <: Interpreter[Reads, T]](implicit pathBuilder: ReadsPathBuilder[P], interpreter: UnderlyingInterpreter): RequiredInterpreter[Reads, P, T, UnderlyingInterpreter] = new RequiredInterpreter[Reads, P, T, UnderlyingInterpreter] {
    override def originalInterpreter: UnderlyingInterpreter = interpreter
    override def required(path: P, schema: Reads[T], default: Option[T]): Reads[T] = default
      .map(default => pathBuilder.toJsPath(path).readWithDefault(default)(schema))
      .getOrElse(pathBuilder.toJsPath(path).read(schema))
  }
}

trait FinalPriority {
  implicit def constrainedReadInterpreter[T, UnderlyingInterpreter <: Interpreter[Reads, T]](implicit algebra: UnderlyingInterpreter): ConstrainedInterpreter[Reads, T, UnderlyingInterpreter] = new ConstrainedInterpreter[Reads, T, UnderlyingInterpreter] {
    override def originalInterpreter: UnderlyingInterpreter = algebra

    override def verifying(schema: Reads[T], constraint: Constraint[T]): Reads[T] =
      schema.filter(JsonValidationError(s"error.constraints.${constraint.name}", constraint.args: _*))(constraint.validate)
  }
}
