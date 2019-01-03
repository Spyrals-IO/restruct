package io.github.methrat0n.restruct.writers.jsonschema

import io.github.methrat0n.restruct.core.data.constraints.Constraint
import io.github.methrat0n.restruct.core.data.schema._
import io.github.methrat0n.restruct.writers.json.jsonWrites
import play.api.libs.json._

trait FieldJsonSchemaWriterInterpreter extends FieldAlgebra[JsonSchemaWriter] {

  private val writer = jsonWrites

  override def required[T](path: Path, schema: JsonSchemaWriter[T], default: Option[T]): JsonSchemaWriter[T] =
    (
      path2JsonSchema(path, schema, default, isRequired = true),
      writer.required(path, schema._2, default)
    )

  override def optional[T](path: Path, schema: JsonSchemaWriter[T], default: Option[Option[T]]): JsonSchemaWriter[Option[T]] =
    (
      path2JsonSchema(path, schema, default.flatten, isRequired = false),
      writer.optional(path, schema._2, default)
    )

  override def verifying[T](schema: JsonSchemaWriter[T], constraint: Constraint[T]): JsonSchemaWriter[T] =
    (schema._1 ++ Json.obj(
      constraint.name -> writeArgs(constraint.args)
    ), writer.verifying(schema._2, constraint))

  override def either[A, B](fa: JsonSchemaWriter[A], fb: (JsObject, Writes[B])): JsonSchemaWriter[Either[A, B]] = ((fa._1, fb._1) match {
    case (a, b) if a.value.contains("oneOf") && b.value.contains("oneOf") =>
      deepMerge(a, b)
    case (a, b) if a.value.contains("oneOf") =>
      deepMerge(a, Json.obj(
        "oneOf" -> Json.arr(b)
      ))
    case (a, b) if b.value.contains("oneOf") =>
      deepMerge(Json.obj(
        "oneOf" -> Json.arr(a)
      ), b)
    case (a, b) =>
      Json.obj(
        "oneOf" -> Json.arr(a, b)
      )
  }, writer.either(fa._2, fb._2))

  override def product[A, B](fa: JsonSchemaWriter[A], fb: JsonSchemaWriter[B]): JsonSchemaWriter[(A, B)] =
    (deepMerge(fa._1, fb._1), writer.product(fa._2, fb._2))

  override def pure[A](a: A): JsonSchemaWriter[A] =
    (Json.obj(), writer.pure(a))

  override def imap[A, B](fa: JsonSchemaWriter[A])(f: A => B)(g: B => A): JsonSchemaWriter[B] =
    (fa._1, writer.imap(fa._2)(f)(g))

  private def writeArgs(args: Any): JsValue = args match {
    case Seq(item)        => writeArgs(item)
    case seq @ Seq(_, _*) => JsArray(seq.map(writeArgs))
    case number: Number   => Json.toJson(BigDecimal(number.toString))
    case boolean: Boolean => Json.toJson(boolean)
    case other            => JsString(other.toString)
  }

  private def path2JsonSchema[T](path: Path, schema: JsonSchemaWriter[T], maybeDefault: Option[T], isRequired: Boolean): JsObject =
    path.steps.toList.foldRight(
      schema._1 ++ maybeDefault.map(default =>
        Json.obj("default" -> schema._2.writes(default))).getOrElse(JsObject.empty)
    )((step, acc) => step match {
        case StringStep(name) => Json.obj(
          "type" -> "object",
          "properties" -> Json.obj(
            name -> acc
          ),
          "required" -> (if (isRequired) Json.arr(name) else JsArray.empty)
        )
        case IntStep(_) => Json.obj(
          "type" -> "array",
          "contains" -> acc
        )
      })

}
