import sbt._

object Dependencies {
  object cats {
    val version = "1.0.1"
    val core = "org.typelevel" %% "cats-core" % version
  }

  object enumeratum {
    val version = "1.5.13"
    val enum = "com.beachape" % "enumeratum_2.12" % version
  }

  object refined {
    val version = "0.9.0"
    val refined = "eu.timepit" %% "refined" % version
  }

  object test {
    val version = "3.0.5"
    val scalaTest = "org.scalatest" % "scalatest_2.12" % version
  }

  object json {
    val version = "2.6.7"
    val play = "com.typesafe.play" %% "play-json" % version
  }
}
