
lazy val restruct = (project in file("."))
  .settings(commonSettings: _*)
  .settings(name := "restruct-all")
  .aggregate(core, jsonSchema, playJson, enumeratum, configLoader, queryStringBindable)

lazy val scalaReflect = Def.setting { "org.scala-lang" % "scala-reflect" % Settings.scala.version }
lazy val core = (project in file("./core"))
  .settings(commonSettings: _*)
  .settings(addCompilerPlugin("org.typelevel" %% "kind-projector" % "0.10.3"))
  .settings(libraryDependencies += scalaReflect.value)
  .settings(libraryDependencies ++= coreDependencies)
  .settings(scalacOptions += "-language:higherKinds")
  .settings(name := "restruct-core")

lazy val jsonSchema = (project in file("./jsonSchema"))
  .settings(commonSettings: _*)
  .settings(libraryDependencies ++= playJsonDependencies)
  .settings(name := "restruct-json-schema")
  .dependsOn(core, writes)

lazy val playJson = (project in file("./playJson"))
  .settings(commonSettings: _*)
  .settings(name := "restruct-play-json")
  .aggregate(reads, writes, format)

lazy val reads = (project in file("./playJson/reads"))
  .settings(commonSettings: _*)
  .settings(addCompilerPlugin("org.typelevel" %% "kind-projector" % "0.10.3"))
  .settings(libraryDependencies ++= playJsonDependencies)
  .settings(name := "restruct-play-json-reads")
  .settings(libraryDependencies += Dependencies.chuusai.shapeless)
  .dependsOn(core)

lazy val writes = (project in file("./playJson/writes"))
  .settings(commonSettings: _*)
  .settings(libraryDependencies ++= playJsonDependencies)
  .settings(name := "restruct-play-json-writes")
  .dependsOn(core)

lazy val format = (project in file("./playJson/format"))
  .settings(commonSettings: _*)
  .settings(name := "restruct-play-json-format")
  .dependsOn(core, writes, reads)

lazy val enumeratum = (project in file("./enumeratum"))
  .settings(commonSettings: _*)
  .settings(libraryDependencies ++= enumeratumDependencies)
  .settings(name := "restruct-enumeratum")
  .dependsOn(core)
/*
lazy val bson = (project in file("./bson"))
  .settings(commonSettings: _*)
  .settings(name := "restruct-bson")
  .aggregate(bsonReader, bsonWriter, bsonHandler)

lazy val bsonReader = (project in file("./bson/reader"))
  .settings(commonSettings: _*)
  .settings(libraryDependencies ++= bsonDependencies)
  .settings(addCompilerPlugin("org.typelevel" %% "kind-projector" % "0.10.3"))
  .settings(name := "restruct-bson-reader")
  .dependsOn(core)

lazy val bsonWriter = (project in file("./bson/writer"))
  .settings(commonSettings: _*)
  .settings(libraryDependencies ++= bsonDependencies)
  .settings(addCompilerPlugin("org.typelevel" %% "kind-projector" % "0.10.3"))
  .settings(name := "restruct-bson-writer")
  .dependsOn(core)

lazy val bsonHandler = (project in file("./bson/handler"))
  .settings(commonSettings: _*)
  .settings(libraryDependencies ++= bsonDependencies)
  .settings(name := "restruct-bson-handler")
  .dependsOn(core, bsonReader, bsonWriter)
*/
lazy val configLoader = (project in file("./configLoader"))
  .settings(commonSettings: _*)
  .settings(libraryDependencies += Dependencies.play.play)
  .settings(name := "restruct-config-loader")
  .dependsOn(core)

lazy val queryStringBindable = (project in file("./queryStringBindable"))
  .settings(commonSettings: _*)
  .settings(libraryDependencies += Dependencies.play.play)
  .settings(name := "restruct-query-string-bindable")
  .dependsOn(core)

lazy val examples = (project in file("./examples"))
  .settings(commonSettings: _*)
  .settings(name := "restruct-examples")
  .settings(addCompilerPlugin("org.typelevel" %% "kind-projector" % "0.10.3"))
  //.settings(libraryDependencies += Dependencies.restruct.reads)
  .dependsOn(reads, writes)

lazy val commonSettings =
  Settings.scala.commonSettings ++
  scalariformCommonSettings

lazy val coreDependencies = Seq(
  Dependencies.test.scalaTest
)

lazy val playJsonDependencies = Seq(
  Dependencies.json.play
)

lazy val enumeratumDependencies = Seq(
  Dependencies.enumeratum.enum
)

lazy val bsonDependencies = Seq(
  Dependencies.mongo.reactive
)

lazy val configLoaderDependencies = Seq(
  Dependencies.play.play
)

import scalariform.formatter.preferences._

lazy val scalariformCommonSettings = Seq(
  scalariformPreferences := scalariformPreferences.value
    .setPreference(AlignSingleLineCaseStatements, true)
    .setPreference(CompactControlReadability, true)
    .setPreference(DanglingCloseParenthesis, Force)
    .setPreference(IndentLocalDefs, true)
    .setPreference(NewlineAtEndOfFile, true)
)

ThisBuild / organization := "io.github.methrat0n"
ThisBuild / organizationName := "methrat0n"
ThisBuild / organizationHomepage := Some(url("https://methrat0n.github.io/"))

ThisBuild / scmInfo := Some(
  ScmInfo(
    url("https://github.io/methrat0n/restruct"),
    "scm:git@github.io:methrat0n/restruct.git"
  )
)
ThisBuild / developers := List(
  Developer(
    id    = "methrat0n",
    name  = "Merlin Goulet",
    email = "merlin.goulet@live.fr",
    url   = url("https://methrat0n.github.io/")
  )
)

ThisBuild / description := "Obtains any format from your class in just one line"
ThisBuild / licenses := List("MIT" -> new URL("https://github.com/Methrat0n/restruct/blob/master/LICENSE"))
ThisBuild / homepage := Some(url("https://methrat0n.github.io/"))

// Remove all additional repository other than Maven Central from POM
ThisBuild / pomIncludeRepository := { _ => false }
ThisBuild / publishTo := {
  val nexus = "https://oss.sonatype.org/"
  //if (isSnapshot.value) Some("snapshots" at nexus + "content/repositories/snapshots")
  /*else*/ Some("releases" at nexus + "service/local/staging/deploy/maven2")
}
ThisBuild / publishMavenStyle := true

useGpg := true
isSnapshot := true