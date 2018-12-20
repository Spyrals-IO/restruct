package io.github.methrat0n.restruct.readers.config

import com.typesafe.config.Config
import io.github.methrat0n.restruct.core.data.schema.ComplexSchemaAlgebra
import play.api.ConfigLoader
import collection.JavaConverters._

trait ComplexConfigInterpreter extends ComplexSchemaAlgebra[ConfigLoader] {
  override def many[T](schema: ConfigLoader[T]): ConfigLoader[List[T]] = (config: Config, configPath: String) =>
    config.getList(configPath).asScala.toList.map(value => schema.load(value.atPath("|||value|||"), "|||value|||"))
}
