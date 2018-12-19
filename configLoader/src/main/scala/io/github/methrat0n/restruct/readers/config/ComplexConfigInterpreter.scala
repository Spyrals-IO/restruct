package io.github.methrat0n.restruct.readers.config

import com.typesafe.config.Config
import io.github.methrat0n.restruct.core.data.schema.ComplexSchemaAlgebra
import play.api.{ConfigLoader, Configuration}

trait ComplexConfigInterpreter extends ComplexSchemaAlgebra[ConfigLoader] {
  override def many[T](schema: ConfigLoader[T]): ConfigLoader[List[T]] = (config: Config, configPath: String) => {
    import collection.JavaConverters._
    config.getConfigList(configPath).asScala.map(conf => new Configuration(conf).get[T]("")(schema)).toList
  }
}
