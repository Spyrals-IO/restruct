object Settings {
  object scala {
    val version  = "2.12.4-bin-typelevel-4"
    val scalaOrganization =  "org.typelevel"
    val scalacOptions = Seq (
      "-encoding", "utf-8",
      "-explaintypes",
      "-deprecation",
      "-unchecked",
      "-feature",
      "-Ywarn-value-discard",
      "-Ypartial-unification",
      "-Yliteral-types",
      "-Ywarn-infer-any",
      "-Xcheckinit",
      "-Xfatal-warnings",
      "-Xlint:adapted-args,nullary-unit,inaccessible,nullary-override,infer-any,missing-interpolator,doc-detached,private-shadow,type-parameter-shadow,poly-implicit-overload,option-implicit,delayedinit-select,by-name-right-associative,package-object-classes,unsound-match,stars-align,constant"
    )
    val unsused = Seq(
      "-Xlint:unused"
    )
  }
}
