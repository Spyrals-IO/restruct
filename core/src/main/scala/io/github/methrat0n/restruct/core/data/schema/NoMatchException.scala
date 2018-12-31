package io.github.methrat0n.restruct.core.data.schema

class NoMatchException(msg: String, matchErrors: Throwable*) extends RuntimeException(msg) {
  val errors: List[Throwable] = matchErrors.toList

  matchErrors.foreach(super.addSuppressed)
}

object NoMatchException {
  def product(e1: Throwable, e2: Throwable): NoMatchException =
    (e1, e2) match {
      case (aNoMatch: NoMatchException, bNoMatch: NoMatchException) =>
        throw new NoMatchException("Cannot find value in bson", (aNoMatch.errors ++ bNoMatch.errors): _*)
      case (aNoMatch: NoMatchException, _) =>
        throw new NoMatchException("Cannot find value in bson", (aNoMatch.errors :+ e2): _*)
      case (_, bNoMatch: NoMatchException) =>
        throw new NoMatchException("Cannot find value in bson", (bNoMatch.errors :+ e1): _*)
      case _ =>
        throw new NoMatchException("Cannot find value in bson", e1, e2)
    }
}
