package lib.core.data.forms

import kernel.forms.{Call, Form}
import kernel.data.schema.SchemaAlgebraSpec
import lib.core.data.schema.SchemaAlgebraSpec.Person
import play.api.mvc.Call
import play.mvc.Http.HttpVerbs

object FormSpec {

  val personForm = Form(
    Call(HttpVerbs.POST, "/"),
    Some(Person.personSchema)
  )

}
