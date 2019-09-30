package io.github.methrat0n.restruct.writers

import java.time.{ LocalDate, LocalTime, ZonedDateTime }

import io.github.methrat0n.restruct.constraints.Constraint
import io.github.methrat0n.restruct.schema.Interpreter._
import io.github.methrat0n.restruct.schema.Path.\
import io.github.methrat0n.restruct.schema._
import play.api.libs.json._

object json {
  implicit val stringWritesInterpreter: SimpleInterpreter[Writes, String] = new SimpleInterpreter[Writes, String] {
    override def schema: Writes[String] = Writes.StringWrites
  }
  implicit val decimalWritesInterpreter: SimpleInterpreter[Writes, Double] = new SimpleInterpreter[Writes, Double] {
    override def schema: Writes[Double] = Writes.DoubleWrites
  }
  implicit val integerWritesInterpreter: SimpleInterpreter[Writes, Int] = new SimpleInterpreter[Writes, Int] {
    override def schema: Writes[Int] = Writes.IntWrites
  }
  implicit val booleanWritesInterpreter: SimpleInterpreter[Writes, Boolean] = new SimpleInterpreter[Writes, Boolean] {
    override def schema: Writes[Boolean] = Writes.BooleanWrites
  }
  implicit val charWritesInterpreter: SimpleInterpreter[Writes, Char] = new SimpleInterpreter[Writes, Char] {
    override def schema: Writes[Char] = Writes[Char](char => JsString(char.toString))
  }
  implicit val byteWritesInterpreter: SimpleInterpreter[Writes, Byte] = new SimpleInterpreter[Writes, Byte] {
    override def schema: Writes[Byte] = Writes.ByteWrites
  }
  implicit val shortWritesInterpreter: SimpleInterpreter[Writes, Short] = new SimpleInterpreter[Writes, Short] {
    override def schema: Writes[Short] = Writes.ShortWrites
  }
  implicit val floatWritesInterpreter: SimpleInterpreter[Writes, Float] = new SimpleInterpreter[Writes, Float] {
    override def schema: Writes[Float] = Writes.FloatWrites
  }
  implicit val bigDecimalWritesInterpreter: SimpleInterpreter[Writes, BigDecimal] = new SimpleInterpreter[Writes, BigDecimal] {
    override def schema: Writes[BigDecimal] = Writes.BigDecimalWrites
  }
  implicit val longWritesInterpreter: SimpleInterpreter[Writes, Long] = new SimpleInterpreter[Writes, Long] {
    override def schema: Writes[Long] = Writes.LongWrites
  }
  implicit val bigIntWritesInterpreter: SimpleInterpreter[Writes, BigInt] = new SimpleInterpreter[Writes, BigInt] {
    override def schema: Writes[BigInt] = Writes[BigInt](bigInt => JsNumber(BigDecimal(bigInt)))
  }
  implicit val dateTimeWritesInterpreter: SimpleInterpreter[Writes, ZonedDateTime] = new SimpleInterpreter[Writes, ZonedDateTime] {
    override def schema: Writes[ZonedDateTime] = Writes.DefaultZonedDateTimeWrites
  }
  implicit val timeWritesInterpreter: SimpleInterpreter[Writes, LocalTime] = new SimpleInterpreter[Writes, LocalTime] {
    override def schema: Writes[LocalTime] = Writes.DefaultLocalTimeWrites
  }
  implicit val dateWritesInterpreter: SimpleInterpreter[Writes, LocalDate] = new SimpleInterpreter[Writes, LocalDate] {
    override def schema: Writes[LocalDate] = Writes.DefaultLocalDateWrites
  }

  import language.higherKinds
  implicit def manyWritesInterpreter[Type, Collection[A] <: Iterable[A], UnderlyingInterpreter <: Interpreter[Writes, Type]](implicit original: UnderlyingInterpreter): ManyInterpreter[Writes, Type, Collection, UnderlyingInterpreter] = new ManyInterpreter[Writes, Type, Collection, UnderlyingInterpreter] {
    override def originalInterpreter: UnderlyingInterpreter = original

    override def many(schema: Writes[Type]): Writes[Collection[Type]] = Writes.traversableWrites(schema)
  }

  implicit def requiredWritesInterpreter[P <: Path, Type, UnderlyingInterpreter <: Interpreter[Writes, Type]](implicit pathBuilder: WritesPathBuilder[P], interpreter: UnderlyingInterpreter): RequiredInterpreter[Writes, P, Type, UnderlyingInterpreter] = new RequiredInterpreter[Writes, P, Type, UnderlyingInterpreter] {
    override def originalInterpreter: UnderlyingInterpreter = interpreter

    override def required(path: P, schema: Writes[Type], default: Option[Type]): Writes[Type] =
      pathBuilder.toJsPath(path).write(schema)
  }

  implicit def optionalWritesInterpreter[Type, P <: Path, UnderlyingInterpreter <: Interpreter[Writes, Type]](implicit original: UnderlyingInterpreter, pathBuilder: WritesPathBuilder[P]): OptionalInterpreter[Writes, P, Type, UnderlyingInterpreter] = new OptionalInterpreter[Writes, P, Type, UnderlyingInterpreter] {
    override def originalInterpreter: UnderlyingInterpreter = original

    override def optional(path: P, schema: Writes[Type], default: Option[Option[Type]]): Writes[Option[Type]] =
      pathBuilder.toJsPath(path).writeNullable(schema)
  }

  implicit def constrainedWritesInterpreter[Type, UnderlyingInterpreter <: Interpreter[Writes, Type]](implicit interpreter: UnderlyingInterpreter): ConstrainedInterpreter[Writes, Type, UnderlyingInterpreter] = new ConstrainedInterpreter[Writes, Type, UnderlyingInterpreter] {
    override def originalInterpreter: UnderlyingInterpreter = interpreter

    override def verifying(schema: Writes[Type], constraint: Constraint[Type]): Writes[Type] =
      schema
  }

  implicit def invariantWritesInterpreter[A, B, AInterpreter <: Interpreter[Writes, A]](implicit interpreterA: AInterpreter): InvariantInterpreter[Writes, A, B, AInterpreter] = new InvariantInterpreter[Writes, A, B, AInterpreter] {
    override def underlyingInterpreter: AInterpreter = interpreterA

    override def imap(fa: Writes[A])(f: A => B)(g: B => A): Writes[B] =
      fa.contramap(g)
  }

  implicit def semiGroupalWritesInterpreter[A, B, AInterpreter <: Interpreter[Writes, A], BInterpreter <: Interpreter[Writes, B]](implicit
    interpreterA: AInterpreter,
    interpreterB: BInterpreter
  ): SemiGroupalInterpreter[Writes, A, B, AInterpreter, BInterpreter] = new SemiGroupalInterpreter[Writes, A, B, AInterpreter, BInterpreter] {
    override def originalInterpreterA: AInterpreter = interpreterA

    override def originalInterpreterB: BInterpreter = interpreterB

    override def product(fa: Writes[A], fb: Writes[B]): Writes[(A, B)] =
      (o: (A, B)) => (fa.writes(o._1), fb.writes(o._2)) match {
        case (a @ JsObject(_), b @ JsObject(_)) => a ++ b
        case (a @ JsArray(_), b @ JsArray(_))   => a ++ b
        case (selected, _)                      => selected
      }
  }

  implicit def oneOfWritesInterpreter[A, B, AInterpreter <: Interpreter[Writes, A], BInterpreter <: Interpreter[Writes, B]](implicit
    interpreterA: AInterpreter,
    interpreterB: BInterpreter
  ): OneOfInterpreter[Writes, A, B, AInterpreter, BInterpreter] = new OneOfInterpreter[Writes, A, B, AInterpreter, BInterpreter] {
    override def originalInterpreterA: AInterpreter = interpreterA

    override def originalInterpreterB: BInterpreter = interpreterB

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

  trait WritesPathBuilder[P <: Path] {
    def toJsPath(path: P): JsPath
  }

  object WritesPathBuilder {
    implicit def stringStep2JsPath[RemainingPath <: Path](implicit remainingPath: WritesPathBuilder[RemainingPath]) = new WritesPathBuilder[RemainingPath \ String] {
      override def toJsPath(path: RemainingPath \ String): JsPath = JsPath(remainingPath.toJsPath(path.previousSteps).path :+ KeyPathNode(path.step))
    }

    implicit def emptyStep2JsPath: WritesPathBuilder[PathNil] = (_: PathNil) => JsPath(List.empty)
  }

}
