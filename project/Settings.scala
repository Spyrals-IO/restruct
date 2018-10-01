import sbt._
import Keys._

object Settings {
  object scala {
    val commonSettings = Seq(
      scalaOrganization := "org.typelevel",
      scalaVersion := "2.12.4-bin-typelevel-4",
      scalacOptions := Seq (
        "-encoding", "utf-8",
        "-explaintypes",
        "-deprecation",
        "-unchecked",
        "-feature",
        "-Ywarn-value-discard",
        "-Ywarn-unused",
        "-Ypartial-unification",
        "-Yliteral-types",
        "-Ywarn-infer-any",
        "-Xcheckinit",
        "-Xfatal-warnings",
        "-Xlint"
      )
    )
  }
}
