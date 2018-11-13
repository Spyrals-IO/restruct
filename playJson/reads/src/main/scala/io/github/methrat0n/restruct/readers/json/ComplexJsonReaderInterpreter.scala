package io.github.methrat0n.restruct.readers.json

import io.github.methrat0n.restruct.core.data.schema.ComplexSchemaAlgebra
import play.api.libs.json._

import scala.collection.generic.CanBuildFrom

trait ComplexJsonReaderInterpreter extends ComplexSchemaAlgebra[Reads] {

  override def many[T](schema: Reads[T]): Reads[List[T]] =
    Reads.traversableReads[List, T](implicitly[CanBuildFrom[List[_], T, List[T]]], schema)

}
