package io.github.methrat0n.restruct.core.data.schema

import scala.language.higherKinds

trait ComplexSchemaAlgebra[F[_]] extends SimpleSchemaAlgebra[F] {

  // use GenTraversableOnce ?
  def many[T](schema: F[T]): F[List[T]]

  //TODO indexed

}
