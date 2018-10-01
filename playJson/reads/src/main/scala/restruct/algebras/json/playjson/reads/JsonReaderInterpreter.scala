package restruct.algebras.json.playjson.reads

import play.api.libs.json.Reads
import restruct.core.Program
import restruct.core.data.schema.SimpleSchemaAlgebra

object JsonReaderInterpreter extends ComplexJsonReaderInterpreter {
  def run[T](program: Program[SimpleSchemaAlgebra, T]): Reads[T] = program.run(this)
}
