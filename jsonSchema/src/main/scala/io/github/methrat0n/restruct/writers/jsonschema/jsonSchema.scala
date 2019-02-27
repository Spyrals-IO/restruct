package io.github.methrat0n.restruct.writers.jsonschema

import play.api.libs.json.{ JsObject, Writes }

final case class JsonSchemaWriter[A](json: JsObject, private[writers] val writer: Writes[A])

object jsonSchema extends SimpleJsonSchemaWriterInterpreter with ComplexJsonSchemaWriterInterpreter with FieldJsonSchemaWriterInterpreter
