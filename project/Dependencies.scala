import sbt._

object Dependencies {

  object enumeratum {
    val version = "1.5.13" //TODO minimify and retro
    val enum = "com.beachape" % "enumeratum_2.12" % version
  }

  object test {
    val version = "3.0.5"
    val scalaTest = "org.scalatest" % "scalatest_2.12" % version
  }

  object json {
    val version = "2.6.7" //TODO minimify and retro
    val play = "com.typesafe.play" %% "play-json" % version
  }

  object mongo {
    val version = "0.16.0" //TODO retro
    val reactive = "org.reactivemongo" %% "reactivemongo" % version //TODO should work, or do something specific for basic mongo
  }

  object play {
    val version = "2.6.0"
    val play = "com.typesafe.play" %% "play" % version
  }

  object restruct {
    val version = "0.1.0"
    val reads = "io.github.methrat0n" %% "restruct-play-json-reads" % version
  }
}
