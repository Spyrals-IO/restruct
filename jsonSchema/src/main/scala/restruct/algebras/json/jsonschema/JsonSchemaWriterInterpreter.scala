package restruct.algebras.json.jsonschema

import play.api.libs.json._
import restruct.core.Program
import restruct.core.data.schema.SimpleSchemaAlgebra

object JsonSchemaWriterInterpreter extends ComplexJsonSchemaWriterInterpreter {
  def run[T](program: Program[SimpleSchemaAlgebra, T]): JsValue = program.run(this)._1.getConst
}
