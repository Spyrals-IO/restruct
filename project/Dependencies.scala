import sbt._

object Dependencies {
  object cats {
    val version = "1.0.1" //TODO minimify
    val core = "org.typelevel" %% "cats-core" % version
  }

  object enumeratum {
    val version = "1.5.13" //TODO minimify and retro
    val enum = "com.beachape" % "enumeratum_2.12" % version
  }

  object refined {
    val version = "0.9.0" //TODO minimify
    val core = "eu.timepit" %% "refined" % version
  }

  object test {
    val version = "3.0.5"
    val scalaTest = "org.scalatest" % "scalatest_2.12" % version
  }

  object json {
    val version = "2.6.7" //TODO minimify and retro
    val play = "com.typesafe.play" %% "play-json" % version
  }

  object shapeless {
    val version = "2.3.3" //TODO minimify
    val shapeless = "com.chuusai" %% "shapeless" % version
  }

  object mongo {
    val version = "0.16.0" //TODO retro
    val reactive = "org.reactivemongo" %% "reactivemongo" % version //TODO should work, or do something specific for basic mongo
  }
}
