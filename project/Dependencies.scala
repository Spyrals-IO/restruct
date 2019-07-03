import sbt._

object Dependencies {

  object enumeratum {
    val version = "1.5.13" //TODO minimify and retro
    val enum = "com.beachape" %% "enumeratum" % version
  }

  object test {
    val version = "3.0.8"
    val scalaTest = "org.scalatest" %% "scalatest" % version
  }

  object json {
    val version = "2.7.4" //TODO minimify and retro
    val play = "com.typesafe.play" %% "play-json" % version
  }

  object mongo {
    val version = "0.16.0" //TODO retro
    val reactive = "org.reactivemongo" %% "reactivemongo" % version //TODO should work, or do something specific for basic mongo
  }

  object play {
    val version = "2.7.3"
    val play = "com.typesafe.play" %% "play" % version
  }

  object restruct {
    val version = "0.1.0"
    val reads = "io.github.methrat0n" %% "restruct-play-json-reads" % version
  }

  //TODO remove
  object chuusai {
    val shapeless = "com.chuusai" %% "shapeless" % "2.3.3"
  }
}
