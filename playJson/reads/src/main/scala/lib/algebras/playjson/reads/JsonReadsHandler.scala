package lib.algebras.playjson.reads

import lib.core.Program
import lib.core.data.constraints.Constraint
import lib.core.data.schema.SchemaAlgebra
import play.api.libs.json.{JsPath, JsonValidationError, Reads}
import play.api.libs.functional.syntax._

import scala.collection.generic.CanBuildFrom

object JsonReadsHandler {

  def run[T](program: Program[SchemaAlgebra, T]): Reads[T] = program.run(Handler)

  private[json] object Handler extends SchemaAlgebra[Reads] {
    override def stringSchema: Reads[String] =
      Reads.StringReads

    override def decimalSchema: Reads[Double] =
      Reads.DoubleReads

    override def integerSchema: Reads[Int] =
      Reads.IntReads

    override def booleanSchema: Reads[Boolean] =
      Reads.BooleanReads

    override def many[T](name: String, schema: Reads[T], default: Option[List[T]]): Reads[List[T]] =
      readsWithDefault(
        name, Reads.traversableReads[List, T](implicitly[CanBuildFrom[List[_], T, List[T]]], schema), default
      )

    override def optional[T](name: String, schema: Reads[T], default: Option[Option[T]]): Reads[Option[T]] =
      (JsPath \ name).readNullableWithDefault(default.flatten)(schema)

    override def required[T](name: String, schema: Reads[T], default: Option[T]): Reads[T] =
      readsWithDefault(name, schema, default)

    private def readsWithDefault[T](name: String, reads: Reads[T], default: Option[T]): Reads[T] =
      default.map(default => (JsPath \ name).readWithDefault(default)(reads)).getOrElse((JsPath \ name).read(reads))

    override def verifying[T](schema: Reads[T], constraint: Constraint[T]): Reads[T] =
      schema.filter(JsonValidationError(s"error.constraints.${constraint.name}", constraint.args: _*))(constraint.validate)

    override def either[A, B](a: Reads[A], b: Reads[B]): Reads[Either[A, B]] =
      a.map[Either[A, B]](Left.apply).orElse(b.map[Either[A, B]](Right.apply))

    override def pure[T](a: T): Reads[T] = Reads.pure(a)

    override def imap[A, B](fa: Reads[A])(f: A => B)(g: B => A): Reads[B] =
      fa.map(f)

    override def product[A, B](fa: Reads[A], fb: Reads[B]): Reads[(A, B)] =
      (fa and fb).tupled
  }
}
