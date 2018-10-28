
lazy val core = (project in file("./core"))
  .settings(commonSettings: _*)
  .settings(addCompilerPlugin("org.spire-math" %% "kind-projector" % "0.9.3"))
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

lazy val refined = (project in file("./refined"))
  .settings(commonSettings: _*)
  .settings(libraryDependencies ++= refinedDependencies)
  .settings(name := "restruct-refined-schema")
  .dependsOn(core)

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

lazy val urlEncode = (project in file("./urlEncode"))
  .settings(commonSettings: _*)
  .settings(libraryDependencies ++= urlEncodeDependencies)
  .settings(name := "restruct-url-encode-schema")
  .dependsOn(core)

lazy val examples = (project in file("./examples"))
  .settings(commonSettings: _*)
  .settings(name := "restruct-examples")
  .dependsOn(core, format)


lazy val commonSettings =
  Settings.scala.commonSettings ++
  scalariformCommonSettings

lazy val coreDependencies = Seq(
  Dependencies.cats.core,
  Dependencies.test.scalaTest,
  Dependencies.shapeless.shapeless
)

lazy val playJsonDependencies = Seq(
  Dependencies.json.play
)

lazy val refinedDependencies = Seq(
  Dependencies.refined.core,
  Dependencies.cats.core
)

lazy val enumeratumDependencies = Seq(
  Dependencies.enumeratum.enum,
  Dependencies.cats.core
)

lazy val bsonDependencies = Seq(
  Dependencies.mongo.reactive,
  Dependencies.cats.core
)

lazy val urlEncodeDependencies = Seq(
  Dependencies.cats.core
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
