
lazy val restruct = (project in file("."))
  .settings(commonSettings: _*)
  .aggregate(core, jsonSchema, playJson, enumeratum, bson)

lazy val scalaReflect = Def.setting { "org.scala-lang" % "scala-reflect" % "2.12.4" }
lazy val core = (project in file("./core"))
  .settings(commonSettings: _*)
  .settings(addCompilerPlugin("org.spire-math" %% "kind-projector" % "0.9.3"))
  .settings(libraryDependencies += scalaReflect.value)
  .settings(libraryDependencies ++= coreDependencies)
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
  .settings(libraryDependencies ++= playJsonDependencies)
  .settings(name := "restruct-play-json-reads")
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
  .settings(name := "restruct-enumeratum-schema")
  .dependsOn(core)

lazy val bson = (project in file("./bson"))
  .settings(commonSettings: _*)
  .settings(name := "restruct-bson-schema")
  .aggregate(bsonReader, bsonWriter, bsonHandler)

lazy val bsonReader = (project in file("./bson/reader"))
  .settings(commonSettings: _*)
  .settings(libraryDependencies ++= bsonDependencies)
  .settings(addCompilerPlugin("org.spire-math" %% "kind-projector" % "0.9.3"))
  .settings(name := "restruct-bson-reader-schema")
  .dependsOn(core)

lazy val bsonWriter = (project in file("./bson/writer"))
  .settings(commonSettings: _*)
  .settings(libraryDependencies ++= bsonDependencies)
  .settings(addCompilerPlugin("org.spire-math" %% "kind-projector" % "0.9.3"))
  .settings(name := "restruct-bson-writer-schema")
  .dependsOn(core)

lazy val bsonHandler = (project in file("./bson/handler"))
  .settings(commonSettings: _*)
  .settings(libraryDependencies ++= bsonDependencies)
  .settings(name := "restruct-bson-handler-schema")
  .dependsOn(core, bsonReader, bsonWriter)

lazy val configLoader = (project in file("./configLoader"))
  .settings(commonSettings: _*)
  .settings(libraryDependencies ++= configLoaderDependencies)
  .settings(name := "restruct-config-loader")
  .dependsOn(core)

lazy val examples = (project in file("./examples"))
  .settings(commonSettings: _*)
  .settings(name := "restruct-examples")
  .dependsOn(core, reads)

lazy val commonSettings =
  Settings.scala.commonSettings ++
  scalariformCommonSettings ++
  releaseSettings ++
  publishSettings

lazy val coreDependencies = Seq(
  Dependencies.test.scalaTest,
  Dependencies.cats.core
)

lazy val playJsonDependencies = Seq(
  Dependencies.json.play,
)

lazy val enumeratumDependencies = Seq(
  Dependencies.enumeratum.enum,
)

lazy val bsonDependencies = Seq(
  Dependencies.mongo.reactive,
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

import sbtrelease.ReleasePlugin.autoImport.ReleaseTransformations._
lazy val username = "Methrat0n"
lazy val repo = "restruct"
lazy val releaseSettings = Seq(
  releaseProcess := Seq[ReleaseStep](
    inquireVersions,
    runTest,
    setReleaseVersion,
    commitReleaseVersion,
    tagRelease,
    releaseStepCommand("publishSigned"),
    setNextVersion,
    commitNextVersion
  )
)

lazy val publishSettings = Seq(
  homepage := Some(url(s"https://github.com/$username/$repo")),
  scmInfo := Some(ScmInfo(url(s"https://github.com/$username/$repo"), s"git@github.com:$username/$repo.git")),
  developers := List(
    Developer(
      id = username,
      name = "Merlin Goulet",
      email = "merlingoulet@live.com",
      url = new URL(s"http://github.com/$username")
    )
  ),
  publishMavenStyle := true,
  publishTo := Some(if (isSnapshot.value) Opts.resolver.sonatypeSnapshots else Opts.resolver.sonatypeStaging)
)
