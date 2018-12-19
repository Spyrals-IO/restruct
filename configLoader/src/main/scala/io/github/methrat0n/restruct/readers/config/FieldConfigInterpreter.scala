package io.github.methrat0n.restruct.readers.config

import com.typesafe.config.Config
import io.github.methrat0n.restruct.core.data.constraints.Constraint
import io.github.methrat0n.restruct.core.data.schema.{FieldAlgebra, Path}
import play.api.{ConfigLoader, Configuration}

import scala.util.Try

trait FieldConfigInterpreter extends FieldAlgebra[ConfigLoader] {
  override def required[T](path: Path, schema: ConfigLoader[T], default: Option[T]): ConfigLoader[T] = (config: Config, configPath: String) =>
      default.map(dft => new Configuration(config).getOptional[T](s"$configPath.$path")(schema).getOrElse(dft))
        .getOrElse(new Configuration(config).get[T](s"$configPath.$path")(schema))

  override def optional[T](path: Path, schema: ConfigLoader[T], default: Option[Option[T]]): ConfigLoader[Option[T]] = (config: Config, configPath: String) =>
    new Configuration(config).getOptional[T](s"$configPath.$path")(schema) orElse default.flatten

  override def verifying[T](schema: ConfigLoader[T], constraint: Constraint[T]): ConfigLoader[T] = (config: Config, configPath: String) => {
    val configuration = Configuration(config)
    val value = schema.load(config, configPath)
    if(constraint.validate(value)) value
    else throw configuration.reportError(configPath, s"Constraint ${constraint.name} check failed for $value", None)
  }

  override def either[A, B](fa: ConfigLoader[A], fb: ConfigLoader[B]): ConfigLoader[Either[A, B]] = (config: Config, configPath: String) => {
    Try { fa.load(config, configPath) }.toOption match {
      case Some(a) => Left(a)
      case None => Try { fb.load(config, configPath) }.toOption match {
        case Some(b) => Right(b)
        case None => throw new Configuration(config).reportError(configPath, s"Cannot load this config using neither $fa nor $fb", None)
      }
    }
  }

  override def imap[A, B](fa: ConfigLoader[A])(f: A => B)(g: B => A): ConfigLoader[B] = (config: Config, configPath: String) => {
    f(fa.load(config))
  }

  override def product[A, B](fa: ConfigLoader[A], fb: ConfigLoader[B]): ConfigLoader[(A, B)] = (config: Config, configPath: String) => {
    (
      fa.load(config, configPath),
      fb.load(config, configPath)
    )
  }

  override def pure[A](a: A): ConfigLoader[A] = (config: Config, configPath: String) =>
    a
}
