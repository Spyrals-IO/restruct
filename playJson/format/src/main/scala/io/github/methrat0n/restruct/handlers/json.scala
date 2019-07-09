package io.github.methrat0n.restruct.handlers

import io.github.methrat0n.restruct.constraints.Constraint
import io.github.methrat0n.restruct.schema.Interpreter._
import io.github.methrat0n.restruct.schema.{ Interpreter, Path }
import play.api.libs.json.{ Format, Reads, Writes }

object json {

  import io.github.methrat0n.restruct.readers.json._
  import io.github.methrat0n.restruct.writers.json._

  implicit def simpleFormatInterpreter[A <: AnyVal](implicit reader: SimpleInterpreter[Reads, A], writer: SimpleInterpreter[Writes, A]): SimpleInterpreter[Format, A] = new SimpleInterpreter[Format, A] {
    override def schema: Format[A] = Format(reader.schema, writer.schema)
  }

  implicit val stringFormatInterpreter: SimpleInterpreter[Format, String] = new SimpleInterpreter[Format, String] {
    override def schema: Format[String] = Format(stringReadInterpreter.schema, stringWritesInterpreter.schema)
  }
}

trait MiddlePriority extends LowPriority {
  import language.higherKinds
  implicit def manyReadInterpreter[T, Collection[A] <: Iterable[A], UnderlyingInterpreter <: Interpreter[Format, T]](implicit
    algebra: UnderlyingInterpreter,
    reader: ManyInterpreter[Reads, T, Collection, UnderlyingInterpreter],
    writer: ManyInterpreter[Writes, T, Collection, UnderlyingInterpreter]
  ): ManyInterpreter[Format, T, Collection, UnderlyingInterpreter] =
    new ManyInterpreter[Format, T, Collection, UnderlyingInterpreter] {
      override def originalInterpreter: UnderlyingInterpreter = algebra

      override def many(schema: Format[T]): Format[Collection[T]] =
        Format(
          reader.many(schema),
          writer.many(schema)
        )
    }

  implicit def optionalReadInterpreter[T, P <: Path, UnderlyingInterpreter <: Interpreter[Reads, T]](implicit
    algebra: UnderlyingInterpreter,
    reader: OptionalInterpreter[Reads, P, T, UnderlyingInterpreter],
    writer: OptionalInterpreter[Writes, P, T, UnderlyingInterpreter]
  ): OptionalInterpreter[Format, P, T, UnderlyingInterpreter] =
    new OptionalInterpreter[Format, P, T, UnderlyingInterpreter] {
      override def originalInterpreter: UnderlyingInterpreter = algebra

      override def optional(path: P, schema: Format[T], default: Option[Option[T]]): Format[Option[T]] =
        Format(
          reader.optional(path, schema, default),
          writer.optional(path, schema, default)
        )
    }

  implicit def semiGroupalReadInterpreter[A, B, AInterpreter <: Interpreter[Reads, A], BInterpreter <: Interpreter[Reads, B]](implicit
    algebraA: AInterpreter,
    algebraB: BInterpreter,
    reader: SemiGroupalInterpreter[Reads, A, B, AInterpreter, BInterpreter],
    writer: SemiGroupalInterpreter[Writes, A, B, AInterpreter, BInterpreter]
  ): SemiGroupalInterpreter[Format, A, B, AInterpreter, BInterpreter] =
    new SemiGroupalInterpreter[Format, A, B, AInterpreter, BInterpreter] {
      override def originalInterpreterA: AInterpreter = algebraA

      override def originalInterpreterB: BInterpreter = algebraB

      override def product(fa: Format[A], fb: Format[B]): Format[(A, B)] =
        Format(
          reader.product(fa, fb),
          writer.product(fa, fb)
        )
    }

  implicit def oneOfReadInterpreter[A, B, AInterpreter <: Interpreter[Reads, A], BInterpreter <: Interpreter[Reads, B]](implicit
    algebraA: AInterpreter,
    algebraB: BInterpreter,
    reader: OneOfInterpreter[Reads, A, B, AInterpreter, BInterpreter],
    writer: OneOfInterpreter[Writes, A, B, AInterpreter, BInterpreter]
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
          reader.or(fa, fb),
          writer.or(fa, fb)
        )
    }
}

trait LowPriority extends FinalPriority {

  implicit def invariantReadInterpreter[A, B, UnderlyingInterpreter <: Interpreter[Reads, A]](implicit
    underlying: UnderlyingInterpreter,
    reader: InvariantInterpreter[Reads, A, B, UnderlyingInterpreter],
    writer: InvariantInterpreter[Writes, A, B, UnderlyingInterpreter]
  ): InvariantInterpreter[Format, A, B, UnderlyingInterpreter] =
    new InvariantInterpreter[Format, A, B, UnderlyingInterpreter] {
      override def underlyingInterpreter: UnderlyingInterpreter = underlying

      override def imap(fa: Format[A])(f: A => B)(g: B => A): Format[B] =
        Format(
          reader.imap(fa)(f)(g),
          writer.imap(fa)(f)(g)
        )
    }

  implicit def requiredReadInterpreter[P <: Path, T, UnderlyingInterpreter <: Interpreter[Reads, T]](implicit
    interpreter: UnderlyingInterpreter,
    reader: RequiredInterpreter[Reads, P, T, UnderlyingInterpreter],
    writer: RequiredInterpreter[Writes, P, T, UnderlyingInterpreter]
  ): RequiredInterpreter[Format, P, T, UnderlyingInterpreter] =
    new RequiredInterpreter[Format, P, T, UnderlyingInterpreter] {
      override def originalInterpreter: UnderlyingInterpreter = interpreter

      override def required(path: P, schema: Format[T], default: Option[T]): Format[T] =
        Format(
          reader.required(path, schema, default),
          writer.required(path, schema, default)
        )
    }
}

trait FinalPriority {
  implicit def constrainedReadInterpreter[T, UnderlyingInterpreter <: Interpreter[Reads, T]](implicit
    algebra: UnderlyingInterpreter,
    reader: ConstrainedInterpreter[Reads, T, UnderlyingInterpreter],
    writer: ConstrainedInterpreter[Writes, T, UnderlyingInterpreter]
  ): ConstrainedInterpreter[Format, T, UnderlyingInterpreter] =
    new ConstrainedInterpreter[Format, T, UnderlyingInterpreter] {
      override def originalInterpreter: UnderlyingInterpreter = algebra

      override def verifying(schema: Format[T], constraint: Constraint[T]): Format[T] =
        Format(
          reader.verifying(schema, constraint),
          writer.verifying(schema, constraint)
        )
    }
}
