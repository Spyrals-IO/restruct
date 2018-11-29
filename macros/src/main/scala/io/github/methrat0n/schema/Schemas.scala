package io.github.methrat0n.schema

import io.github.methrat0n.restruct.schema.Schema

object Schemas {
  import language.experimental.macros
  import scala.reflect.macros.blackbox
  def of[Type]: Schema[Type] = macro ofImpl[Type]

  private def ofImpl[T: c.WeakTypeTag](c: blackbox.Context): c.Expr[Schema[T]] = {
    import c.universe._

      def inferSchema(typ: Type): Tree =
        c.inferImplicitValue(appliedType(typeOf[Schema[_]].typeConstructor, typ), silent = false)

      def isOption(typ: Type): Boolean =
        typ.typeConstructor =:= typeOf[Option[_]].typeConstructor

      def isProduct(typ: Type): Boolean =
        typ <:< typeOf[Product]

      def isCoproduct(symbol: Symbol): Boolean =
        symbol.asClass.knownDirectSubclasses.nonEmpty && symbol.asClass.knownDirectSubclasses.forall(symbol =>
          symbol.isType && (isProduct(symbol.asType.toType) || isCoproduct(symbol)))

    val tag = implicitly[c.WeakTypeTag[T]]
    if (isProduct(tag.tpe)) {
      val fieldsTree = tag.tpe.decls.sorted.collect {
        case m: TermSymbol if m.isVal && m.isCaseAccessor => (m.name, m.infoIn(tag.tpe))
      }.map {
        case (name, typ) if isOption(typ) =>
          val treeSchema = inferSchema(typ.typeArgs.head)
          val typName = TypeName(typ.typeArgs.head.typeSymbol.name.decodedName.toString)
          val fieldName = name.decodedName.toString.replaceAll(" ", "")
          q"io.github.methrat0n.restruct.schema.OptionalField[$typName](io.github.methrat0n.restruct.core.data.schema.Path(cats.data.NonEmptyList(io.github.methrat0n.restruct.core.data.schema.StringStep($fieldName), List.empty)), $treeSchema, None)"
        case (name, typ) =>
          val treeSchema = inferSchema(typ)
          val typName = TypeName(typ.typeSymbol.name.decodedName.toString)
          val fieldName = name.decodedName.toString.replaceAll(" ", "")
          q"io.github.methrat0n.restruct.schema.RequiredField[$typName](io.github.methrat0n.restruct.core.data.schema.Path(cats.data.NonEmptyList(io.github.methrat0n.restruct.core.data.schema.StringStep($fieldName), List.empty)), $treeSchema, None)"
      }
      val productName = TypeName(s"ProductSchema${fieldsTree.size.toString}")
      val schemaTree = q"new io.github.methrat0n.restruct.schema.Schema.$productName(..$fieldsTree, List.empty)"
      c.Expr(c.untypecheck(schemaTree)).asInstanceOf[c.Expr[Schema[T]]]
    }
    else if (isCoproduct(tag.tpe.typeSymbol)) {
      val schemas = tag.tpe.typeSymbol.asClass.knownDirectSubclasses.map( childClass => inferSchema(childClass.asType.toType))
      val productName = TypeName(s"CoproductSchema${schemas.size.toString}")
      val schemaTree = q"new io.github.methrat0n.restruct.schema.Schema.$productName(..$schemas, List.empty)"
      c.Expr(c.untypecheck(schemaTree)).asInstanceOf[c.Expr[Schema[T]]]
    }
    else {
      c.abort(
        c.enclosingPosition,
        s"Type '${tag.tpe.typeSymbol.name}' must either be a Product or a Coproduct"
      )
    }
  }
}
