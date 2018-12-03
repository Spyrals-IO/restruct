import sbt._
import Keys._

object Settings {
  object scala {
    val version = "2.12.4"
    val commonSettings = Seq(
      scalaVersion := version,
      organization := "com.github.methrat0n",
      scalacOptions := Seq (
        "-encoding", "utf-8",
        "-explaintypes",
        "-deprecation",
        "-unchecked",
        "-feature",
        "-Ywarn-value-discard",
        "-Ywarn-unused",
        "-Ypartial-unification",
        "-Ywarn-infer-any",
        "-Xcheckinit",
        "-Xfatal-warnings",
        "-Xlint"
      )
    )
  }
}
