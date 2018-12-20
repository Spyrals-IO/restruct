package io.github.methrat0n.restruct.readers.config

import com.typesafe.config.{ Config, ConfigException, ConfigList, ConfigObject }
import play.api.{ ConfigLoader, Configuration }
import io.github.methrat0n.restruct.core.data.constraints.Constraint
import io.github.methrat0n.restruct.core.data.schema.{ FieldAlgebra, IntStep, Path, StringStep }

import scala.util.Try

trait FieldConfigInterpreter extends FieldAlgebra[ConfigLoader] {
  override def required[T](path: Path, schema: ConfigLoader[T], default: Option[T]): ConfigLoader[T] = (config: Config, configPath: String) => {
    val configuration = buildConfigurationFromPath(path, config, configPath)
    default.map(dft => configuration.getOptional[T]("|||value|||")(schema).getOrElse(dft))
      .getOrElse(configuration.get[T]("|||value|||")(schema))
  }

  override def optional[T](path: Path, schema: ConfigLoader[T], default: Option[Option[T]]): ConfigLoader[Option[T]] = (config: Config, configPath: String) =>
    try {
      buildConfigurationFromPath(path, config, configPath).getOptional[T]("|||value|||")(schema) orElse default.flatten
    }
    catch {
      case _: ConfigException.Missing => None
    }

  override def verifying[T](schema: ConfigLoader[T], constraint: Constraint[T]): ConfigLoader[T] = (config: Config, configPath: String) => {
    val configuration = Configuration(config)
    val value = schema.load(config, configPath)
    if (constraint.validate(value)) value
    else throw configuration.reportError(configPath, s"Constraint ${constraint.name} check failed for $value", None)
  }

  override def either[A, B](fa: ConfigLoader[A], fb: ConfigLoader[B]): ConfigLoader[Either[A, B]] = (config: Config, configPath: String) => {
    Try { fa.load(config, configPath) }.toOption match {
      case Some(a) => Left(a)
      case None => Try { fb.load(config, configPath) }.toOption match {
        case Some(b) => Right(b)
        case None    => throw new Configuration(config).reportError(configPath, s"Cannot load this config using neither $fa nor $fb", None)
      }
    }
  }

  override def imap[A, B](fa: ConfigLoader[A])(f: A => B)(g: B => A): ConfigLoader[B] = (config: Config, configPath: String) => {
    f(fa.load(config, configPath))
  }

  override def product[A, B](fa: ConfigLoader[A], fb: ConfigLoader[B]): ConfigLoader[(A, B)] = (config: Config, configPath: String) => {
    (
      fa.load(config, configPath),
      fb.load(config, configPath)
    )
  }

  override def pure[A](a: A): ConfigLoader[A] = (config: Config, configPath: String) =>
    a

  import collection.JavaConverters._
  private def buildConfigurationFromPath(path: Path, config: Config, rootPath: String) =
    Configuration(path.steps.tail.foldLeft(path.steps.head match {
      case StringStep(step) =>
        val configPath = s"$rootPath.$step"
        (configPath, config.getValue(s"$rootPath.$step"))
      case IntStep(step) =>
        val configPath = s"$rootPath.$step"
        try {
          (configPath, config.getList(rootPath).asScala.toList(step))
        }
        catch {
          case _: ConfigException.WrongType => throw new Configuration(config).reportError(rootPath, "no array found in path")
          case e: IndexOutOfBoundsException => throw new ConfigException.Missing(configPath, e)
        }
    })((acc, step) => {
      val (path, value) = acc
      value match {
        case obj: ConfigObject => step match {
          case StringStep(string) =>
            val configPath = s"$path.$string"
            (configPath, obj.toConfig.getValue(string))
          case IntStep(int) =>
            val configPath = s"$path.$int"
            try {
              (configPath, obj.toConfig.getList("").asScala.toList(int))
            }
            catch {
              case _: ConfigException.WrongType => throw new Configuration(config).reportError("", "no array found in path")
              case e: IndexOutOfBoundsException => throw new ConfigException.Missing(configPath, e)
            }
        }
        case list: ConfigList => step match {
          case StringStep(string) => throw new ConfigException.Missing(s"$path.$string")
          case IntStep(int) =>
            val configPath = s"$path.$int"
            try {
              (configPath, list.asScala.toList(int))
            }
            catch {
              case _: ConfigException.WrongType => throw new Configuration(config).reportError("", "no array found in path")
              case e: IndexOutOfBoundsException => throw new ConfigException.Missing(configPath, e)
            }
        }
        case _ => throw new ConfigException.Missing(path)
      }
    })._2.atPath("|||value|||"))
}
