package lib.algebras.playjson.writes

import lib.core.Program
import lib.core.data.constraints.Constraint
import lib.core.data.schema.SchemaAlgebra
import play.api.libs.json._
import play.api.libs.functional.syntax._

object JsonWritesHandler {

  def run[T](program: Program[SchemaAlgebra, T]): Writes[T] = program.run(Handler)

  private[json] object Handler extends SchemaAlgebra[Writes] {
    override def stringSchema: Writes[String] =
      Writes.StringWrites

    override def decimalSchema: Writes[Double] =
      Writes.DoubleWrites

    override def integerSchema: Writes[Int] =
      Writes.IntWrites

    override def booleanSchema: Writes[Boolean] =
      Writes.BooleanWrites

    override def many[T](name: String, schema: Writes[T], default: Option[List[T]]): Writes[List[T]] =
      (JsPath \ name).write(Writes.traversableWrites(schema))

    override def optional[T](name: String, schema: Writes[T], default: Option[Option[T]]): Writes[Option[T]] =
      (JsPath \ name).writeNullable(schema)

    override def required[T](name: String, schema: Writes[T], default: Option[T]): Writes[T] =
      (JsPath \ name).write(schema)

    override def verifying[T](schema: Writes[T], constraint: Constraint[T]): Writes[T] =
      schema

    override def either[A, B](a: Writes[A], b: Writes[B]): Writes[Either[A, B]] =
      Writes {
        case Left(input)  => a.writes(input)
        case Right(input) => b.writes(input)
      }

    override def pure[T](a: T): Writes[T] = Writes(_ => Json.obj())

    override def imap[A, B](fa: Writes[A])(f: A => B)(g: B => A): Writes[B] =
      fa.contramap(g)

    override def product[A, B](fa: Writes[A], fb: Writes[B]): Writes[(A, B)] =
      (o: (A, B)) => (fa.writes(o._1), fb.writes(o._2)) match {
        case (a @ JsObject(_), b @ JsObject(_)) => a ++ b
        case (a @ JsArray(_), b @ JsArray(_))   => a ++ b
        case (selected, _)                      => selected
      }
  }

}
