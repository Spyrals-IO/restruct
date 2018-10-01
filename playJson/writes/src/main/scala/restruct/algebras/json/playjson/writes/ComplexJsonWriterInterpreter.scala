package restruct.algebras.json.playjson.writes

import play.api.libs.json.{ JsPath, Writes }
import restruct.core.data.constraints.Constraint
import restruct.core.data.schema.ComplexSchemaAlgebra

trait ComplexJsonWriterInterpreter extends ComplexSchemaAlgebra[Writes]
  with SimpleJsonWriterInterpreter with InvariantJsonWriterInterpreter
  with IdentityJsonWriterInterpreter with SemiGroupalJsonWriterInterpreter {

  override def many[T](name: String, schema: Writes[T], default: Option[List[T]]): Writes[List[T]] =
    (JsPath \ name).write(Writes.traversableWrites(schema))

  override def optional[T](name: String, schema: Writes[T], default: Option[Option[T]]): Writes[Option[T]] =
    (JsPath \ name).writeNullable(schema)

  override def required[T](name: String, schema: Writes[T], default: Option[T]): Writes[T] =
    (JsPath \ name).write(schema)

  override def verifying[T](schema: Writes[T], constraint: Constraint[T]): Writes[T] =
    schema

}
