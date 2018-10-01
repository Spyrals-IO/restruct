package restruct.algebras.json.playjson.writes

import play.api.libs.json._
import restruct.core.Program
import restruct.core.data.schema.SimpleSchemaAlgebra

object JsonWriterInterpreter extends ComplexJsonWriterInterpreter {
  def run[T](program: Program[SimpleSchemaAlgebra, T]): Writes[T] = program.run(this)
}
