package io.github.methrat0n.restruct.readers

import java.time.{ LocalDate, LocalTime, ZonedDateTime }

import com.typesafe.config._
import io.github.methrat0n.restruct.constraints.Constraint
import io.github.methrat0n.restruct.readers.configLoader.PathBuilder
import io.github.methrat0n.restruct.schema.Interpreter._
import io.github.methrat0n.restruct.schema.Path.\
import io.github.methrat0n.restruct.schema.{ Interpreter, NoMatchException, Path, PathNil }
import play.api.{ ConfigLoader, Configuration }

import scala.collection.Factory
import scala.util.{ Failure, Success, Try }

/**
 * The errors are handle with exceptions to follow the ConfigLoader error handling.
 */
object configLoader extends ConfigLoaderMiddlePriority {
  implicit val charInterpreter: SimpleInterpreter[ConfigLoader, Char] = new SimpleInterpreter[ConfigLoader, Char] {
    override def schema: ConfigLoader[Char] = ConfigLoader.stringLoader.map(_.charAt(0))
  }
  implicit val byteInterpreter: SimpleInterpreter[ConfigLoader, Byte] = new SimpleInterpreter[ConfigLoader, Byte] {
    override def schema: ConfigLoader[Byte] = charInterpreter.schema.map(_.toByte)
  }
  implicit val shortInterpreter: SimpleInterpreter[ConfigLoader, Short] = new SimpleInterpreter[ConfigLoader, Short] {
    override def schema: ConfigLoader[Short] = charInterpreter.schema.map(_.toShort)
  }
  implicit val floatInterpreter: SimpleInterpreter[ConfigLoader, Float] = new SimpleInterpreter[ConfigLoader, Float] {
    override def schema: ConfigLoader[Float] = ConfigLoader.doubleLoader.map(_.toFloat)
  }
  implicit val decimalInterpreter: SimpleInterpreter[ConfigLoader, Double] = new SimpleInterpreter[ConfigLoader, Double] {
    override def schema: ConfigLoader[Double] = ConfigLoader.doubleLoader
  }
  implicit val bigDecimalReadInterpreter: SimpleInterpreter[ConfigLoader, BigDecimal] = new SimpleInterpreter[ConfigLoader, BigDecimal] {
    override def schema: ConfigLoader[BigDecimal] = stringReadInterpreter.schema.map(BigDecimal.apply)
  }
  implicit val integerReadInterpreter: SimpleInterpreter[ConfigLoader, Int] = new SimpleInterpreter[ConfigLoader, Int] {
    override def schema: ConfigLoader[Int] = ConfigLoader.intLoader
  }
  implicit val longReadInterpreter: SimpleInterpreter[ConfigLoader, Long] = new SimpleInterpreter[ConfigLoader, Long] {
    override def schema: ConfigLoader[Long] = ConfigLoader.longLoader
  }
  implicit val bigIntReadInterpreter: SimpleInterpreter[ConfigLoader, BigInt] = new SimpleInterpreter[ConfigLoader, BigInt] {
    override def schema: ConfigLoader[BigInt] = stringReadInterpreter.schema.map(BigInt.apply)
  }
  implicit val booleanReadInterpreter: SimpleInterpreter[ConfigLoader, Boolean] = new SimpleInterpreter[ConfigLoader, Boolean] {
    override def schema: ConfigLoader[Boolean] = ConfigLoader.booleanLoader
  }
  implicit val stringReadInterpreter: SimpleInterpreter[ConfigLoader, String] = new SimpleInterpreter[ConfigLoader, String] {
    override def schema: ConfigLoader[String] = ConfigLoader.stringLoader
  }
  implicit val dateTimeReadInterpreter: SimpleInterpreter[ConfigLoader, ZonedDateTime] = new SimpleInterpreter[ConfigLoader, ZonedDateTime] {
    override def schema: ConfigLoader[ZonedDateTime] = stringReadInterpreter.schema.map(ZonedDateTime.parse)
  }
  implicit val timeReadInterpreter: SimpleInterpreter[ConfigLoader, LocalTime] = new SimpleInterpreter[ConfigLoader, LocalTime] {
    override def schema: ConfigLoader[LocalTime] = stringReadInterpreter.schema.map(LocalTime.parse)
  }
  implicit val dateReadInterpreter: SimpleInterpreter[ConfigLoader, LocalDate] = new SimpleInterpreter[ConfigLoader, LocalDate] {
    override def schema: ConfigLoader[LocalDate] = stringReadInterpreter.schema.map(LocalDate.parse)
  }

  /**
   * Read a config at `path` and return it. If a list is found, it's wrapped inside
   * a config at key `__list`
   */
  private[readers] def readConfigOrConfigList(config: Config, path: String): Option[Config] =
    Try {
      config.getConfig(path)
    }.orElse(Try {
      config.getList(path).atKey("__list")
    }).toOption

  trait PathBuilder[P] {
    def toConfigPath(path: P, config: Config): Option[ConfigValue]
  }

  object PathBuilder {
    import io.github.methrat0n.restruct.schema.Path
    implicit def lastStringStep2ConfigPath[RemainingPath](implicit pathBuilder: PathBuilder[PathNil \ RemainingPath]): PathBuilder[PathNil \ String \ RemainingPath] =
      (path: PathNil \ String \ RemainingPath, config: Config) =>
        readConfigOrConfigList(config, path.previousSteps.step).flatMap(conf => pathBuilder.toConfigPath(Path \ path.step, conf))
    implicit def stringStep2ConfigPath: PathBuilder[PathNil \ String] =
      (path: PathNil \ String, config: Config) =>
        Try { config.getValue(path.step) }.toOption

    implicit def lastIntStep2ConfigPath[RemainingPath](implicit pathBuilder: PathBuilder[PathNil \ RemainingPath]): PathBuilder[PathNil \ Int \ RemainingPath] =
      (path: PathNil \ Int \ RemainingPath, config: Config) =>
        Try { config.getConfigList("__list") }.toOption.flatMap(confs => {
          val maybeConf = Try { confs.get(path.previousSteps.step) }.toOption
          maybeConf.flatMap(pathBuilder.toConfigPath(Path \ path.step, _))
        })
    implicit def intStep2ConfigPath: PathBuilder[PathNil \ Int] =
      (path: PathNil \ Int, config: Config) =>
        Try { config.getList("__list") }.toOption.flatMap(conf =>
          Try { conf.get(path.step) }.toOption)
  }
}

trait ConfigLoaderMiddlePriority extends ConfigLoaderLowPriority {

  import language.higherKinds
  import scala.jdk.CollectionConverters._

  implicit def manyReadInterpreter[T, Collection[A] <: Iterable[A], UnderlyingInterpreter <: Interpreter[ConfigLoader, T]](implicit algebra: UnderlyingInterpreter, factory: Factory[T, Collection[T]]): ManyInterpreter[ConfigLoader, T, Collection, UnderlyingInterpreter] = new ManyInterpreter[ConfigLoader, T, Collection, UnderlyingInterpreter] {
    override def originalInterpreter: UnderlyingInterpreter = algebra

    override def many(schema: ConfigLoader[T]): ConfigLoader[Collection[T]] =
      (config: Config, configPath: String) =>
        config.getList(configPath).asScala
          .map(value => schema.load(value.atPath("__value"), "__value"))
          .iterator.to[Collection[T]](factory)
  }

  implicit def optionalReadInterpreter[T, P <: Path, UnderlyingInterpreter <: Interpreter[ConfigLoader, T]](implicit algebra: UnderlyingInterpreter, pathBuilder: PathBuilder[P]): OptionalInterpreter[ConfigLoader, P, T, UnderlyingInterpreter] = new OptionalInterpreter[ConfigLoader, P, T, UnderlyingInterpreter] {
    override def originalInterpreter: UnderlyingInterpreter = algebra

    override def optional(path: P, schema: ConfigLoader[T], default: Option[Option[T]]): ConfigLoader[Option[T]] =
      (config: Config, configPath: String) =>
        configLoader.readConfigOrConfigList(config, configPath)
          .flatMap(pathBuilder.toConfigPath(path, _))
          .map(_.atPath("__restruct"))
          .map(schema.load(_, "__restruct"))
          .orElse(default.flatten)
  }

  implicit def semiGroupalReadInterpreter[A, B, AInterpreter <: Interpreter[ConfigLoader, A], BInterpreter <: Interpreter[ConfigLoader, B]](implicit algebraA: AInterpreter, algebraB: BInterpreter): SemiGroupalInterpreter[ConfigLoader, A, B, AInterpreter, BInterpreter] = new SemiGroupalInterpreter[ConfigLoader, A, B, AInterpreter, BInterpreter] {
    override def originalInterpreterA: AInterpreter = algebraA

    override def originalInterpreterB: BInterpreter = algebraB

    override def product(fa: ConfigLoader[A], fb: ConfigLoader[B]): ConfigLoader[(A, B)] = (config: Config, configPath: String) =>
      (
        fa.load(config, configPath),
        fb.load(config, configPath)
      )
  }

  implicit def oneOfReadInterpreter[A, B, AInterpreter <: Interpreter[ConfigLoader, A], BInterpreter <: Interpreter[ConfigLoader, B]](implicit algebraA: AInterpreter, algebraB: BInterpreter): OneOfInterpreter[ConfigLoader, A, B, AInterpreter, BInterpreter] = new OneOfInterpreter[ConfigLoader, A, B, AInterpreter, BInterpreter] {
    override def originalInterpreterA: AInterpreter = algebraA

    override def originalInterpreterB: BInterpreter = algebraB

    /**
     * Should return a success, if any, or concatenate errors.
     *
     * fa == sucess => fa result in Left
     * fa == error && fb == sucess => fb result in Right
     * fa == error && fb == error => concatenate fa and fb errors into F error handling
     *
     * If two successes are found, fa will be choosen.
     *
     * @return F in error (depends on the implementing F) or successful F with one of the two value
     */
    override def or(fa: ConfigLoader[A], fb: ConfigLoader[B]): ConfigLoader[Either[A, B]] = (config: Config, configPath: String) => {
      Try { fa.load(config, configPath) } match {
        case Success(a) => Left(a)
        case Failure(aThrowable) => Try { fb.load(config, configPath) } match {
          case Success(b)          => Right(b)
          case Failure(bThrowable) => throw NoMatchException.product(aThrowable, bThrowable)
        }
      }
    }
  }
}

trait ConfigLoaderLowPriority extends ConfigLoaderFinalPriority {

  implicit def invariantReadInterpreter[A, B, UnderlyingInterpreter <: Interpreter[ConfigLoader, A]](implicit underlying: UnderlyingInterpreter): InvariantInterpreter[ConfigLoader, A, B, UnderlyingInterpreter] = new InvariantInterpreter[ConfigLoader, A, B, UnderlyingInterpreter] {
    override def underlyingInterpreter: UnderlyingInterpreter = underlying

    override def imap(fa: ConfigLoader[A])(f: A => B)(g: B => A): ConfigLoader[B] =
      fa.map(f)
  }

  implicit def requiredReadInterpreter[P <: Path, T, UnderlyingInterpreter <: Interpreter[ConfigLoader, T]](implicit pathBuilder: PathBuilder[P], interpreter: UnderlyingInterpreter): RequiredInterpreter[ConfigLoader, P, T, UnderlyingInterpreter] = new RequiredInterpreter[ConfigLoader, P, T, UnderlyingInterpreter] {
    override def originalInterpreter: UnderlyingInterpreter = interpreter

    override def required(path: P, schema: ConfigLoader[T], default: Option[T]): ConfigLoader[T] = (config: Config, configPath: String) =>
      pathBuilder.toConfigPath(
        path,
        configLoader.readConfigOrConfigList(config, configPath).get
      )
        .map(_.atPath("__restruct"))
        .map(schema.load(_, "__restruct"))
        .orElse(default)
        .get //TODO clearer errors
  }
}

trait ConfigLoaderFinalPriority {
  implicit def constrainedReadInterpreter[T, UnderlyingInterpreter <: Interpreter[ConfigLoader, T]](implicit algebra: UnderlyingInterpreter): ConstrainedInterpreter[ConfigLoader, T, UnderlyingInterpreter] = new ConstrainedInterpreter[ConfigLoader, T, UnderlyingInterpreter] {
    override def originalInterpreter: UnderlyingInterpreter = algebra

    override def verifying(schema: ConfigLoader[T], constraint: Constraint[T]): ConfigLoader[T] = (config: Config, configPath: String) => {
      val configuration = Configuration(config)
      val value = schema.load(config, configPath)
      if (constraint.validate(value)) value
      else throw configuration.reportError(configPath, s"Constraint ${constraint.name} check failed for $value", None)
    }
  }
}
