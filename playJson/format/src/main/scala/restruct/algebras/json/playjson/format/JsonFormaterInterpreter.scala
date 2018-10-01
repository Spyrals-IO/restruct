package restruct.algebras.json.playjson.format

import play.api.libs.json.Format
import restruct.core.Program
import restruct.core.data.schema.SimpleSchemaAlgebra

object JsonFormaterInterpreter extends ComplexJsonFormaterInterpreter {
  def run[T](program: Program[SimpleSchemaAlgebra, T]): Format[T] = program.run(this)
}
