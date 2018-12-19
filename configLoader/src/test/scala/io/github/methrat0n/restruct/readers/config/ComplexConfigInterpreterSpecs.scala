package io.github.methrat0n.restruct.readers.config

import com.typesafe.config.ConfigFactory
import org.scalatest.{ FlatSpec, Matchers }
import play.api.{ ConfigLoader, Configuration }

class ComplexConfigInterpreterSpecs extends FlatSpec with Matchers {
  private val configuration = Configuration(ConfigFactory.load("test.conf"))

  behavior of "ConfigLoader for mutliple elements"

  import io.github.methrat0n.restruct.schema.Syntax._

  "ConfigLoader" should "find a loader for multiple strings" in {
    list(string).bind(configLoader)
  }
  it should "load strings without any change" in {
    val stringLoader = list(string).bind(configLoader)

    val found = configuration.get[List[String]]("stringListTest")(stringLoader)
    val expect = List("test1", "test2")
    found shouldBe expect
  }
  it should "load the same strings than the default string list ConfigLoader" in {
    val derived = list(string).bind(configLoader).asInstanceOf[ConfigLoader[Seq[String]]]
    val default = ConfigLoader.seqStringLoader

    val found = configuration.get[Seq[String]]("stringListTest")(derived)
    val expect = configuration.get[Seq[String]]("stringListTest")(default)

    found shouldBe expect
  }

  "ConfigLoader" should "find a loader for multiple booleans" in {
    list(boolean).bind(configLoader)
  }
  it should "load booleans" in {
    val booleanLoader = list(boolean).bind(configLoader)

    val found = configuration.get[List[Boolean]]("booleanListTest")(booleanLoader)
    val expect = List(true, true)
    found shouldBe expect
  }
  it should "load the same booleans than the default boolean list ConfigLoader" in {
    val derived = list(boolean).bind(configLoader).asInstanceOf[ConfigLoader[Seq[Boolean]]]
    val default = ConfigLoader.seqBooleanLoader

    val found = configuration.get[Seq[Boolean]]("booleanListTest")(derived)
    val expect = configuration.get[Seq[Boolean]]("booleanListTest")(default)

    found shouldBe expect
  }
}
