package io.github.methrat0n.schema

import io.github.methrat0n.restruct.schema.Schema

object Schemas {
  //import language.experimental.macros
  import scala.reflect.macros.blackbox
  def of[Type]: Schema[Type] = ??? //macro ofImpl[Type]

  def ofImpl[Type: c.WeakTypeTag](c: blackbox.Context): c.Expr[Schema[Type]] = {
    //import c.universe._
    ???
  }
}
