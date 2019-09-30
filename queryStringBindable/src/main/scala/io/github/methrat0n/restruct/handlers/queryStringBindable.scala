package io.github.methrat0n.restruct.handlers

import java.time.{ LocalDate, LocalTime, ZonedDateTime }

import io.github.methrat0n.restruct.constraints.Constraint
import io.github.methrat0n.restruct.schema.Interpreter._
import io.github.methrat0n.restruct.schema.Path.\
import io.github.methrat0n.restruct.schema.{ Interpreter, PathNil }
import play.api.mvc.QueryStringBindable

import scala.collection.Factory
import scala.util.Try

import language.higherKinds

object queryStringBindable extends MiddlePriority {

  implicit val stringQueryStringBindableInterpreter: SimpleInterpreter[QueryStringBindable, String] = new SimpleInterpreter[QueryStringBindable, String] {
    override def schema: QueryStringBindable[String] = QueryStringBindable.bindableString
  }
  implicit val decimalQueryStringBindableInterpreter: SimpleInterpreter[QueryStringBindable, Double] = new SimpleInterpreter[QueryStringBindable, Double] {
    override def schema: QueryStringBindable[Double] = QueryStringBindable.bindableDouble
  }
  implicit val integerQueryStringBindableInterpreter: SimpleInterpreter[QueryStringBindable, Int] = new SimpleInterpreter[QueryStringBindable, Int] {
    override def schema: QueryStringBindable[Int] = QueryStringBindable.bindableInt
  }
  implicit val booleanQueryStringBindableInterpreter: SimpleInterpreter[QueryStringBindable, Boolean] = new SimpleInterpreter[QueryStringBindable, Boolean] {
    override def schema: QueryStringBindable[Boolean] = QueryStringBindable.bindableBoolean
  }
  implicit val charQueryStringBindableInterpreter: SimpleInterpreter[QueryStringBindable, Char] = new SimpleInterpreter[QueryStringBindable, Char] {
    override def schema: QueryStringBindable[Char] = QueryStringBindable.bindableChar
  }
  implicit val byteQueryStringBindableInterpreter: SimpleInterpreter[QueryStringBindable, Byte] = new SimpleInterpreter[QueryStringBindable, Byte] {
    override def schema: QueryStringBindable[Byte] = new QueryStringBindable[Byte] {
      override def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, Byte]] =
        QueryStringBindable.bindableChar.bind(key, params)
          .flatMap(either =>
            if (either.isLeft)
              integerQueryStringBindableInterpreter.schema.bind(key, params).map(_.flatMap(int =>
              if (int > Byte.MaxValue || int < Byte.MinValue)
                Left(s"Cannot parse parameter $key with value '$int' as Byte: $key is out of the byte bounds")
              else
                Right(int.toByte)))
            else Some(either.map(_.toByte)))

      override def unbind(key: String, value: Byte): String =
        QueryStringBindable.bindableChar.unbind(key, value.toChar)
    }
  }
  implicit val shortQueryStringBindableInterpreter: SimpleInterpreter[QueryStringBindable, Short] = new SimpleInterpreter[QueryStringBindable, Short] {
    override def schema: QueryStringBindable[Short] = new QueryStringBindable[Short] {
      override def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, Short]] =
        QueryStringBindable.bindableChar.bind(key, params).flatMap(either =>
          if (either.isLeft)
            integerQueryStringBindableInterpreter.schema.bind(key, params).map(_.flatMap(int =>
            if (int > Short.MaxValue || int < Short.MinValue)
              Left(s"Cannot parse parameter $key with value '$int' as Short: $key is out of the short bounds")
            else
              Right(int.toShort)))
          else Some(either.map(_.toShort)))

      override def unbind(key: String, value: Short): String =
        QueryStringBindable.bindableChar.unbind(key, value.toChar)
    }
  }
  implicit val floatQueryStringBindableInterpreter: SimpleInterpreter[QueryStringBindable, Float] = new SimpleInterpreter[QueryStringBindable, Float] {
    override def schema: QueryStringBindable[Float] = QueryStringBindable.bindableFloat
  }
  implicit val bigDecimalQueryStringBindableInterpreter: SimpleInterpreter[QueryStringBindable, BigDecimal] = new SimpleInterpreter[QueryStringBindable, BigDecimal] {
    override def schema: QueryStringBindable[BigDecimal] = new QueryStringBindable[BigDecimal] {
      override def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, BigDecimal]] =
        stringQueryStringBindableInterpreter.schema.bind(key, params).map(_.flatMap(string =>
          Try { BigDecimal(string) }.toEither.left.map(_ =>
            s"Cannot parse parameter $key with value '$string' as BigDecimal: $key must be a number")))

      override def unbind(key: String, value: BigDecimal): String =
        stringQueryStringBindableInterpreter.schema.unbind(key, value.toString)
    }
  }
  implicit val longQueryStringBindableInterpreter: SimpleInterpreter[QueryStringBindable, Long] = new SimpleInterpreter[QueryStringBindable, Long] {
    override def schema: QueryStringBindable[Long] = QueryStringBindable.bindableLong
  }
  implicit val bigIntQueryStringBindableInterpreter: SimpleInterpreter[QueryStringBindable, BigInt] = new SimpleInterpreter[QueryStringBindable, BigInt] {
    override def schema: QueryStringBindable[BigInt] = new QueryStringBindable[BigInt] {
      override def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, BigInt]] =
        stringQueryStringBindableInterpreter.schema.bind(key, params).map(_.flatMap(string =>
          Try { BigInt(string) }.toEither.left.map(_ =>
            s"Cannot parse parameter $key with value '$string' as BigDecimal: $key must be a natural number")))

      override def unbind(key: String, value: BigInt): String =
        stringQueryStringBindableInterpreter.schema.unbind(key, value.toString)
    }
  }
  implicit val dateTimeQueryStringBindableInterpreter: SimpleInterpreter[QueryStringBindable, ZonedDateTime] = new SimpleInterpreter[QueryStringBindable, ZonedDateTime] {
    override def schema: QueryStringBindable[ZonedDateTime] = new QueryStringBindable[ZonedDateTime] {
      override def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, ZonedDateTime]] =
        stringQueryStringBindableInterpreter.schema.bind(key, params).map(_.flatMap(string =>
          Try { ZonedDateTime.parse(string) }.toEither.left.map(_ =>
            s"Cannot parse parameter $key with value '$string' as ZonedDateTime: $key must contains a valid date and time")))

      override def unbind(key: String, value: ZonedDateTime): String =
        stringQueryStringBindableInterpreter.schema.unbind(key, value.toString)
    }
  }
  implicit val timeQueryStringBindableInterpreter: SimpleInterpreter[QueryStringBindable, LocalTime] = new SimpleInterpreter[QueryStringBindable, LocalTime] {
    override def schema: QueryStringBindable[LocalTime] = new QueryStringBindable[LocalTime] {
      override def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, LocalTime]] =
        stringQueryStringBindableInterpreter.schema.bind(key, params).map(_.flatMap(string =>
          Try { LocalTime.parse(string) }.toEither.left.map(_ =>
            s"Cannot parse parameter $key with value '$string' as LocalTime: $key must contains a valid time")))

      override def unbind(key: String, value: LocalTime): String =
        stringQueryStringBindableInterpreter.schema.unbind(key, value.toString)
    }
  }
  implicit val dateQueryStringBindableInterpreter: SimpleInterpreter[QueryStringBindable, LocalDate] = new SimpleInterpreter[QueryStringBindable, LocalDate] {
    override def schema: QueryStringBindable[LocalDate] = new QueryStringBindable[LocalDate] {
      override def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, LocalDate]] =
        stringQueryStringBindableInterpreter.schema.bind(key, params).map(_.flatMap(string =>
          Try { LocalDate.parse(string) }.toEither.left.map(_ =>
            s"Cannot parse parameter $key with value '$string' as LocalDate: $key must contains a valid date")))

      override def unbind(key: String, value: LocalDate): String =
        stringQueryStringBindableInterpreter.schema.unbind(key, value.toString)
    }
  }

}

trait MiddlePriority extends LowPriority {

  implicit def manyQueryStringBindableInterpreter[Collection[A] <: Iterable[A], Type, UnderlyingInterpreter <: Interpreter[QueryStringBindable, Type]](implicit original: UnderlyingInterpreter, factory: Factory[Type, Collection[Type]]): ManyInterpreter[QueryStringBindable, Type, Collection, UnderlyingInterpreter] = new ManyInterpreter[QueryStringBindable, Type, Collection, UnderlyingInterpreter] {
    override def originalInterpreter: UnderlyingInterpreter = original

    override def many(schema: QueryStringBindable[Type]): QueryStringBindable[Collection[Type]] = QueryStringBindable.bindableSeq[Type](schema).transform(_.iterator.to[Collection[Type]](factory), _.toSeq)
  }

  implicit def optionalQueryStringBindableInterpreter[Type, UnderlyingInterpreter <: Interpreter[QueryStringBindable, Type]](implicit original: UnderlyingInterpreter): OptionalInterpreter[QueryStringBindable, PathNil \ String, Type, UnderlyingInterpreter] = new OptionalInterpreter[QueryStringBindable, PathNil \ String, Type, UnderlyingInterpreter] {
    override def originalInterpreter: UnderlyingInterpreter = original

    override def optional(path: PathNil \ String, schema: QueryStringBindable[Type], default: Option[Option[Type]]): QueryStringBindable[Option[Type]] =
      new QueryStringBindable[Option[Type]] {
        override def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, Option[Type]]] =
          schema.bind(path.step, params).map(_.map(Some(_).asInstanceOf[Option[Type]])) orElse default.map(Right.apply) orElse Some(Right(None))

        override def unbind(key: String, value: Option[Type]): String =
          schema.unbind(path.step, (value orElse default.flatten).get)
      }
  }

  implicit def semiGroupalQueryStringBindableInterpreter[A, B, AInterpreter <: Interpreter[QueryStringBindable, A], BInterpreter <: Interpreter[QueryStringBindable, B]](implicit
    interpreterA: AInterpreter,
    interpreterB: BInterpreter
  ): SemiGroupalInterpreter[QueryStringBindable, A, B, AInterpreter, BInterpreter] = new SemiGroupalInterpreter[QueryStringBindable, A, B, AInterpreter, BInterpreter] {
    override def originalInterpreterA: AInterpreter = interpreterA

    override def originalInterpreterB: BInterpreter = interpreterB

    override def product(fa: QueryStringBindable[A], fb: QueryStringBindable[B]): QueryStringBindable[(A, B)] =
      new QueryStringBindable[(A, B)] {
        override def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, (A, B)]] = {
          val a = fa.bind(key, params)
          val b = fb.bind(key, params)

          a.flatMap(eitherA =>
            b.map(eitherB =>
              eitherA.flatMap(A =>
                eitherB.map(B =>
                  (A, B)))))
        }

        override def unbind(key: String, value: (A, B)): String =
          value match {
            case (a, b) => fa.unbind(key, a) + fb.unbind(key, b)
          }
      }
  }

  implicit def oneOfQueryStringBindableInterpreter[A, B, AInterpreter <: Interpreter[QueryStringBindable, A], BInterpreter <: Interpreter[QueryStringBindable, B]](implicit
    interpreterA: AInterpreter,
    interpreterB: BInterpreter
  ): OneOfInterpreter[QueryStringBindable, A, B, AInterpreter, BInterpreter] = new OneOfInterpreter[QueryStringBindable, A, B, AInterpreter, BInterpreter] {
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
    override def or(fa: QueryStringBindable[A], fb: QueryStringBindable[B]): QueryStringBindable[Either[A, B]] =
      new QueryStringBindable[Either[A, B]] {
        override def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, Either[A, B]]] =
          fa.bind(key, params) match {
            case Some(Right(aValue)) => Some(Right(Left(aValue)))
            case Some(Left(aError)) => fb.bind(key, params) match {
              case Some(Right(bValue)) => Some(Right(Right(bValue)))
              case Some(Left(bError))  => Some(Left(s"$aError ; $bError"))
              case None                => None
            }
            case None => None
          }

        override def unbind(key: String, value: Either[A, B]): String =
          value match {
            case Right(b) => fb.unbind(key, b)
            case Left(a)  => fa.unbind(key, a)
          }
      }
  }

}

trait LowPriority extends FinalPriority {

  implicit def invariantQueryStringBindableInterpreter[A, B, AInterpreter <: Interpreter[QueryStringBindable, A]](implicit interpreterA: AInterpreter): InvariantInterpreter[QueryStringBindable, A, B, AInterpreter] = new InvariantInterpreter[QueryStringBindable, A, B, AInterpreter] {
    override def underlyingInterpreter: AInterpreter = interpreterA

    override def imap(fa: QueryStringBindable[A])(f: A => B)(g: B => A): QueryStringBindable[B] =
      new QueryStringBindable[B] {
        override def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, B]] =
          fa.bind(key, params).map(_.map(f))

        override def unbind(key: String, value: B): String =
          fa.unbind(key, g(value))
      }
  }

  implicit def requiredQueryStringBindableInterpreter[Type, UnderlyingInterpreter <: Interpreter[QueryStringBindable, Type]](implicit interpreter: UnderlyingInterpreter): RequiredInterpreter[QueryStringBindable, PathNil \ String, Type, UnderlyingInterpreter] = new RequiredInterpreter[QueryStringBindable, PathNil \ String, Type, UnderlyingInterpreter] {
    override def originalInterpreter: UnderlyingInterpreter = interpreter

    override def required(path: PathNil \ String, schema: QueryStringBindable[Type], default: Option[Type]): QueryStringBindable[Type] =
      new QueryStringBindable[Type] {
        override def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, Type]] =
          schema.bind(path.step, params) orElse default.map(Right.apply)

        override def unbind(key: String, value: Type): String =
          schema.unbind(path.step, value)
      }
  }

}

trait FinalPriority {

  implicit def constrainedQueryStringBindableInterpreter[Type, UnderlyingInterpreter <: Interpreter[QueryStringBindable, Type]](implicit interpreter: UnderlyingInterpreter): ConstrainedInterpreter[QueryStringBindable, Type, UnderlyingInterpreter] = new ConstrainedInterpreter[QueryStringBindable, Type, UnderlyingInterpreter] {
    override def originalInterpreter: UnderlyingInterpreter = interpreter

    override def verifying(schema: QueryStringBindable[Type], constraint: Constraint[Type]): QueryStringBindable[Type] =
      new QueryStringBindable[Type] {
        override def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, Type]] =
          schema.bind(key, params).map(_.flatMap(value => {
            if (constraint.validate(value))
              Right(value)
            else
              Left(s"Constraint ${constraint.name} check failed for $value")
          }))

        override def unbind(key: String, value: Type): String =
          schema.unbind(key, value)
      }
  }

}
