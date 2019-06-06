package io.github.methrat0n.restruct.readers.config

import com.typesafe.config.ConfigFactory
import io.github.methrat0n.restruct.readers.config.FieldConfigInterpreterSpecs.{ OptionalString, RequiredString }
import io.github.methrat0n.restruct.schema.Schema
import org.scalatest.{ FlatSpec, Matchers }
import play.api.Configuration

class FieldConfigInterpreterSpecs extends FlatSpec with Matchers {
  private val configuration = Configuration(ConfigFactory.load("test.conf"))

  behavior of "ConfigLoader for required fields"

  "ConfigLoader" should "find a loader for required fields" in {
    FieldConfigInterpreterSpecs.requiredStringSchema.bind(configLoader)
  }

  it should "load object without any change" in {
    val requiredLoader = FieldConfigInterpreterSpecs.requiredStringSchema.bind(configLoader)

    val found = configuration.get[RequiredString]("requiredStringTest")(requiredLoader)
    val expect = RequiredString("a string")
    found shouldBe expect
  }

  it should "load object containing array using the index in path (first place)" in {
    val requiredLoader = FieldConfigInterpreterSpecs.requiredStringWithIndexFirstSchema.bind(configLoader)

    val found = configuration.get[RequiredString]("requiredStringWithIndexFirstTest")(requiredLoader)
    val expect = RequiredString("a string")
    found shouldBe expect
  }

  it should "load object containing array using the index in path" in {
    val requiredLoader = FieldConfigInterpreterSpecs.requiredStringWithIndexSchema.bind(configLoader)

    val found = configuration.get[RequiredString]("requiredStringWithIndexTest")(requiredLoader)
    val expect = RequiredString("a string")
    found shouldBe expect
  }

  behavior of "ConfigLoader for optional fields"

  "ConfigLoader" should "find a loader for optional fields" in {
    FieldConfigInterpreterSpecs.optionalStringSchema.bind(configLoader)
  }

  it should "load object if present" in {
    val optionalLoader = FieldConfigInterpreterSpecs.optionalStringSchema.bind(configLoader)

    val found = configuration.get[OptionalString]("optionalStringTest")(optionalLoader)
    val expect = OptionalString(Some("a string"))
    found shouldBe expect
  }

  it should "load none if absent" in {
    val optionalLoader = FieldConfigInterpreterSpecs.optionalStringSchema.bind(configLoader)

    val found = configuration.get[OptionalString]("absentStringTest")(optionalLoader)
    val expect = OptionalString(None)
    found shouldBe expect
  }
}

object FieldConfigInterpreterSpecs {
  //TODO this should'nt be mandatory, a schema should be sufficient for himself
  final case class RequiredString(test: String)
  val requiredStringSchema: Schema[RequiredString] = Schema(("test" \ "test").as[String])
  val requiredStringWithIndexFirstSchema: Schema[RequiredString] = Schema((0 \ "test").as[String])
  val requiredStringWithIndexSchema: Schema[RequiredString] = Schema(("test" \ 0).as[String])

  final case class OptionalString(test: Option[String])
  val optionalStringSchema: Schema[OptionalString] = Schema("test".asOption[String])
}
