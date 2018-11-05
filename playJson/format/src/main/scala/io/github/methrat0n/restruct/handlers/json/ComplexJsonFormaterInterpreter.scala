package io.github.methrat0n.restruct.handlers.json

import play.api.libs.json.Format
import io.github.methrat0n.restruct.core.data.constraints.Constraint
import io.github.methrat0n.restruct.core.data.schema.ComplexSchemaAlgebra
import io.github.methrat0n.restruct.readers.json.ComplexJsonReaderInterpreter
import io.github.methrat0n.restruct.writers.json.ComplexJsonWriterInterpreter

trait ComplexJsonFormaterInterpreter extends ComplexSchemaAlgebra[Format]
  with SimpleJsonFormaterInterpreter with SemiGroupalJsonFormaterInterpreter
  with InvariantJsonFormaterInterpreter with IdentityJsonFormaterInterpreter {

  private[this] object ComplexReader extends ComplexJsonReaderInterpreter
  private[this] object ComplexWriter extends ComplexJsonWriterInterpreter

  override def many[T](name: String, schema: Format[T], default: Option[List[T]]): Format[List[T]] =
    Format(
      ComplexReader.many(name, schema, default),
      ComplexWriter.many(name, schema, default)
    )

  override def optional[T](name: String, schema: Format[T], default: Option[Option[T]]): Format[Option[T]] =
    Format(
      ComplexReader.optional(name, schema, default),
      ComplexWriter.optional(name, schema, default)
    )

  override def required[T](name: String, schema: Format[T], default: Option[T]): Format[T] =
    Format(
      ComplexReader.required(name, schema, default),
      ComplexWriter.required(name, schema, default)
    )

  override def verifying[T](schema: Format[T], constraint: Constraint[T]): Format[T] =
    Format(
      ComplexReader.verifying(schema, constraint),
      ComplexWriter.verifying(schema, constraint)
    )

}
