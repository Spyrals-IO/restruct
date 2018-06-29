package lib.algebras.playjson.format

import lib.algebras.playjson.reads.JsonReadsHandler
import lib.algebras.playjson.writes.JsonWritesHandler
import lib.core.Program
import lib.core.data.constraints.Constraint
import lib.core.data.schema.SchemaAlgebra
import play.api.libs.json.Format

object JsonFormatHandler {

  def run[T](program: Program[SchemaAlgebra, T]): Format[T] = program.run(Handler)

  private[this] val writesHandler = JsonWritesHandler.Handler
  private[this] val readsHandler = JsonReadsHandler.Handler

  private[json] object Handler extends SchemaAlgebra[Format] {
    override def stringSchema: Format[String] =
      Format(
        readsHandler.stringSchema,
        writesHandler.stringSchema
      )

    override def decimalSchema: Format[Double] =
      Format(
        readsHandler.decimalSchema,
        writesHandler.decimalSchema
      )

    override def integerSchema: Format[Int] =
      Format(
        readsHandler.integerSchema,
        writesHandler.integerSchema
      )

    override def booleanSchema: Format[Boolean] =
      Format(
        readsHandler.booleanSchema,
        writesHandler.booleanSchema
      )

    override def many[T](name: String, schema: Format[T], default: Option[List[T]]): Format[List[T]] =
      Format(
        readsHandler.many(name, schema, default),
        writesHandler.many(name, schema, default)
      )

    override def optional[T](name: String, schema: Format[T], default: Option[Option[T]]): Format[Option[T]] =
      Format(
        readsHandler.optional(name, schema, default),
        writesHandler.optional(name, schema, default)
      )

    override def required[T](name: String, schema: Format[T], default: Option[T]): Format[T] =
      Format(
        readsHandler.required(name, schema, default),
        writesHandler.required(name, schema, default)
      )

    override def verifying[T](schema: Format[T], constraint: Constraint[T]): Format[T] =
      Format(
        readsHandler.verifying(schema, constraint),
        writesHandler.verifying(schema, constraint)
      )

    override def either[A, B](a: Format[A], b: Format[B]): Format[Either[A, B]] =
      Format(
        readsHandler.either(a, b),
        writesHandler.either(a, b)
      )

    override def pure[T](t: T): Format[T] =
      Format(
        readsHandler.pure(t),
        writesHandler.pure(t)
      )

    override def imap[A, B](fa: Format[A])(f: A => B)(g: B => A): Format[B] =
      Format(
        readsHandler.imap(fa)(f)(g),
        writesHandler.imap(fa)(f)(g)
      )

    override def product[A, B](fa: Format[A], fb: Format[B]): Format[(A, B)] =
      Format(
        readsHandler.product(fa, fb),
        writesHandler.product(fa, fb)
      )
  }
}
