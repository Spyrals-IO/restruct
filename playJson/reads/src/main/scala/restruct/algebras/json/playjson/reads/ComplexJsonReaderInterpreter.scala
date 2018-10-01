package restruct.algebras.json.playjson.reads

import play.api.libs.json.{ JsPath, JsonValidationError, Reads }
import restruct.core.data.constraints.Constraint
import restruct.core.data.schema.ComplexSchemaAlgebra

import scala.collection.generic.CanBuildFrom

private[playjson] trait ComplexJsonReaderInterpreter extends ComplexSchemaAlgebra[Reads]
  with SimpleJsonReaderInterpreter with InvariantJsonReaderInterpreter
  with IdentityJsonReaderInterpreter with SemiGroupalJsonReaderInterpreter {

  private def readsWithDefault[T](name: String, reads: Reads[T], default: Option[T]): Reads[T] =
    default.map(default => (JsPath \ name).readWithDefault(default)(reads)).getOrElse((JsPath \ name).read(reads))

  override def many[T](name: String, schema: Reads[T], default: Option[List[T]]): Reads[List[T]] =
    readsWithDefault(
      name, Reads.traversableReads[List, T](implicitly[CanBuildFrom[List[_], T, List[T]]], schema), default
    )

  override def optional[T](name: String, schema: Reads[T], default: Option[Option[T]]): Reads[Option[T]] =
    (JsPath \ name).readNullableWithDefault(default.flatten)(schema)

  override def required[T](name: String, schema: Reads[T], default: Option[T]): Reads[T] =
    readsWithDefault(name, schema, default)

  override def verifying[T](schema: Reads[T], constraint: Constraint[T]): Reads[T] =
    schema.filter(JsonValidationError(s"error.constraints.${constraint.name}", constraint.args: _*))(constraint.validate)

}
