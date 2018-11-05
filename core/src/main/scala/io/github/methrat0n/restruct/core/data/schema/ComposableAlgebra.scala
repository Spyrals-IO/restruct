package io.github.methrat0n.restruct.core.data.schema

import cats.Invariant

import scala.language.higherKinds

trait ComposableAlgebra[F[_]] extends SemiGroupalAlgebra[F] with Invariant[F]
