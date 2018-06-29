package restruct.examples.main

import restruct.algebras.json.jsonschema.JsonSchemaHandler
import restruct.examples.data.Aging

object Main extends App {
  println(JsonSchemaHandler.run(Aging.schema))
}
