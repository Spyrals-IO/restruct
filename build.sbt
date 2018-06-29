
lazy val core = (project in file("./core"))
  .settings(commonSettings: _*)
  .settings(
    name := "algebra-core",
    addCompilerPlugin("org.spire-math" %% "kind-projector" % "0.9.3"),
    libraryDependencies ++= Seq(
      Dependencies.cats.core,
      Dependencies.test.scalaTest,
      Dependencies.shapeless.shapeless
  ))

lazy val jsonSchema = (project in file("./jsonSchema"))
  .settings(commonSettings: _*)
  .settings(libraryDependencies ++= playJsonDependencies)
  .settings(name := "algebra-json-schema")
  .dependsOn(core, writes)

lazy val playJson = (project in file("./playJson"))
  .settings(commonSettings: _*)
  .settings(name := "algebra-play-json")
  .aggregate(reads, writes, format)

lazy val reads = (project in file("./playJson/reads"))
  .settings(commonSettings: _*)
  .settings(libraryDependencies ++= playJsonDependencies)
  .settings(name := "algebra-play-json-reads")
  .dependsOn(core)

lazy val writes = (project in file("./playJson/writes"))
  .settings(commonSettings: _*)
  .settings(libraryDependencies ++= playJsonDependencies)
  .settings(name := "algebra-play-json-writes")
  .dependsOn(core)

lazy val format = (project in file("./playJson/format"))
  .settings(commonSettings: _*)
  .settings(name := "algebra-play-json-format")
  .dependsOn(core, writes, reads)

lazy val refined = (project in file("./refined"))
  .settings(commonSettings: _*)
  .settings(libraryDependencies ++= refinedDependencies)
  .settings(name := "algebra-refined-schema")
  .dependsOn(core)

lazy val enumeratum = (project in file("./enumeratum"))
  .settings(commonSettings: _*)
  .settings(libraryDependencies ++= enumeratumDependencies)
  .settings(name := "algebra-enumeratum-schema")
  .dependsOn(core)


lazy val commonSettings =
  Settings.scala.commonSettings ++
  scalariformCommonSettings

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

import scalariform.formatter.preferences._

lazy val scalariformCommonSettings = Seq(
  scalariformPreferences := scalariformPreferences.value
    .setPreference(AlignSingleLineCaseStatements, true)
    .setPreference(CompactControlReadability, true)
    .setPreference(DanglingCloseParenthesis, Force)
    .setPreference(IndentLocalDefs, true)
    .setPreference(NewlineAtEndOfFile, true)
)
