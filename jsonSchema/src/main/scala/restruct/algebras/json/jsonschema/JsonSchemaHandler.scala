package restruct.algebras.json.jsonschema

import cats.data.Const
import play.api.libs.json._
import restruct.algebras.json.playjson.writes.JsonWritesHandler
import restruct.core.Program
import restruct.core.data.constraints.Constraint
import restruct.core.data.schema.SchemaAlgebra

import scala.collection.immutable.ListMap

object JsonSchemaHandler {

  def run[T](program: Program[SchemaAlgebra, T]): JsValue = program.run(Handler)._1.getConst

  private[json] type HandlerType[A] = (Const[JsObject, A], Writes[A])

  private[json] object Handler extends SchemaAlgebra[HandlerType] {
    override def stringSchema: HandlerType[String] =
      (Const(Json.obj(
        "type" -> "string"
      )), JsonWritesHandler.Handler.stringSchema)

    override def decimalSchema: HandlerType[Double] =
      (Const(Json.obj(
        "type" -> "number"
      )), JsonWritesHandler.Handler.decimalSchema)

    override def integerSchema: HandlerType[Int] =
      (Const(Json.obj(
        "type" -> "integer"
      )), JsonWritesHandler.Handler.integerSchema)

    override def booleanSchema: HandlerType[Boolean] =
      (Const(Json.obj(
        "type" -> "boolean"
      )), JsonWritesHandler.Handler.booleanSchema)

    override def many[T](name: String, schema: HandlerType[T], default: Option[List[T]]): HandlerType[List[T]] =
      (Const(Json.obj(
        "type" -> "object",
        "properties" -> Json.obj(
          name -> (Json.obj(
            "type" -> "array",
            "items" -> schema._1.getConst,
          ) ++ default.map( default =>
            Json.obj("default" -> Writes.traversableWrites(schema._2).writes(default))
          ).getOrElse(Json.obj()))
        ),
        "required" -> Json.arr()
      )), JsonWritesHandler.Handler.many(name, schema._2, default))

    override def optional[T](name: String, schema: HandlerType[T], default: Option[Option[T]]): HandlerType[Option[T]] =
      (Const(Json.obj(
        "type" -> "object",
        "properties" -> Json.obj(
          name -> (schema._1.getConst ++ default.flatten.map( default =>
            Json.obj("default" -> schema._2.writes(default))
          ).getOrElse(Json.obj()))
        ),
        "required" -> Json.arr()
      )), JsonWritesHandler.Handler.optional(name, schema._2, default))

    override def required[T](name: String, schema: HandlerType[T], default: Option[T]): HandlerType[T] =
      (Const(Json.obj(
        "type" -> "object",
        "properties" -> Json.obj(
          name -> (schema._1.getConst ++ default.map( default =>
            Json.obj("default" -> schema._2.writes(default))
          ).getOrElse(Json.obj()))
        ),
        "required" -> Json.arr(name)
      )), JsonWritesHandler.Handler.required(name, schema._2, default))

    override def verifying[T](schema: HandlerType[T], constraint: Constraint[T]): HandlerType[T] =
      (Const(schema._1.getConst ++ Json.obj(
        constraint.name -> writeArgs(constraint.args)
      )), JsonWritesHandler.Handler.verifying(schema._2, constraint))

    private def writeArgs(args: Any): JsValue = args match {
      case Seq(item)        => writeArgs(item)
      case seq @ Seq(_, _*) => JsArray(seq.map(writeArgs))
      case number: Number   => Json.toJson(BigDecimal(number.toString))
      case boolean: Boolean => Json.toJson(boolean)
      case other            => JsString(other.toString)
    }

    override def either[A, B](a: HandlerType[A], b: HandlerType[B]): HandlerType[Either[A, B]] = ((a._1.getConst, b._1.getConst) match {
      case (a, b) if a.value.contains("oneOf") && b.value.contains("oneOf") =>
        Const(deepMerge(a, b))
      case (a, b) if a.value.contains("oneOf") =>
        Const(deepMerge(a, Json.obj(
          "oneOf" -> Json.arr(b)
        )))
      case (a, b) if b.value.contains("oneOf") =>
        Const(deepMerge(Json.obj(
          "oneOf" -> Json.arr(a)
        ), b))
      case (a, b) =>
        Const(Json.obj(
          "oneOf" -> Json.arr(a, b)
        ))
    }, JsonWritesHandler.Handler.either(a._2, b._2))

    override def pure[T](a: T): HandlerType[T] = (Const(Json.obj()), JsonWritesHandler.Handler.pure(a))

    override def imap[A, B](fa: HandlerType[A])(f: A => B)(g: B => A): HandlerType[B] =
      (Const(fa._1.getConst), JsonWritesHandler.Handler.imap(fa._2)(f)(g))

    override def product[A, B](fa: HandlerType[A], fb: HandlerType[B]): HandlerType[(A, B)] =
      (Const(deepMerge(fa._1.getConst, fb._1.getConst)), JsonWritesHandler.Handler.product(fa._2, fb._2))

    private def deepMerge(current: JsObject, other: JsObject): JsObject = {
        def merge(existingObject: JsObject, otherObject: JsObject): JsObject = {
          val result = otherObject.fields.foldLeft(ListMap(existingObject.fields:_*)) { case (result, (otherKey, otherValue)) =>
            val maybeExistingValue = existingObject.value.get(otherKey)

            val newValue = (maybeExistingValue, otherValue) match {
              case (Some(e: JsObject), o: JsObject) => merge(e, o)
              case (Some(e: JsArray), o: JsArray)   => e ++ o
              case _                                => otherValue
            }
            result + (otherKey -> newValue)
          }

          JsObject(result)
        }
      merge(current, other)
    }

  }

}
