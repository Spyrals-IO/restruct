package io.github.methrat0n.restruct.handlers.queryStringBindable

import io.github.methrat0n.restruct.core.data.constraints.Constraint
import io.github.methrat0n.restruct.core.data.schema.{ FieldAlgebra, Path, StringStep }
import play.api.mvc.QueryStringBindable

trait FieldQueryStringBindableInterpreter extends FieldAlgebra[QueryStringBindable] {

  override def required[T](path: Path, schema: QueryStringBindable[T], default: Option[T]): QueryStringBindable[T] = new QueryStringBindable[T] {
    override def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, T]] =
      path.steps.toList match {
        case List(StringStep(step)) => schema.bind(step, params) orElse default.map(Right.apply)
        case _                      => throw new RuntimeException("Query string does not support path other than a single string")
      }

    override def unbind(key: String, value: T): String =
      path.steps.toList match {
        case List(StringStep(step)) => schema.unbind(step, value)
        case _                      => throw new RuntimeException("Query string does not support path other than a single string")
      }
  }

  //All bindable are optional so this one look strange
  override def optional[T](path: Path, schema: QueryStringBindable[T], default: Option[Option[T]]): QueryStringBindable[Option[T]] = new QueryStringBindable[Option[T]] {
    override def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, Option[T]]] =
      path.steps.toList match {
        case List(StringStep(step)) => schema.bind(step, params).map(_.map(Some(_).asInstanceOf[Option[T]])) orElse default.map(Right.apply) orElse Some(Right(None))
        case _                      => throw new RuntimeException("Query string does not support path other than a single string")
      }

    override def unbind(key: String, value: Option[T]): String =
      path.steps.toList match {
        case List(StringStep(step)) => schema.unbind(step, (value orElse default.flatten).get)
        case _                      => throw new RuntimeException("Query string does not support path other than a single string")
      }
  }

  override def verifying[T](schema: QueryStringBindable[T], constraint: Constraint[T]): QueryStringBindable[T] = new QueryStringBindable[T] {
    override def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, T]] =
      schema.bind(key, params).map(_.flatMap(value => {
        if (constraint.validate(value))
          Right(value)
        else
          Left(s"Constraint ${constraint.name} check failed for $value")
      }))

    override def unbind(key: String, value: T): String =
      schema.unbind(key, value)
  }

  override def or[A, B](fa: QueryStringBindable[A], fb: QueryStringBindable[B]): QueryStringBindable[Either[A, B]] = new QueryStringBindable[Either[A, B]] {
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

  override def imap[A, B](fa: QueryStringBindable[A])(f: A => B)(g: B => A): QueryStringBindable[B] = new QueryStringBindable[B] {
    override def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, B]] =
      fa.bind(key, params).map(_.map(f))

    override def unbind(key: String, value: B): String =
      fa.unbind(key, g(value))
  }

  override def product[A, B](fa: QueryStringBindable[A], fb: QueryStringBindable[B]): QueryStringBindable[(A, B)] = new QueryStringBindable[(A, B)] {
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
