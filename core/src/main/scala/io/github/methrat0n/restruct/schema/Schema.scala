package io.github.methrat0n.restruct.schema

import java.time.{ LocalDate, LocalTime, ZonedDateTime }

import io.github.methrat0n.restruct.constraints.Constraint
import io.github.methrat0n.restruct.schema.Schemas._

import language.implicitConversions
import scala.language.experimental.macros
import scala.reflect.macros.blackbox

trait Schema[Type] { self =>

  type InternalInterpreter[Format[_]] <: Interpreter[Format, Type]

  def bind[Format[_]](implicit interpreter: InternalInterpreter[Format]): Format[Type]

  def constraintedBy(constraint: Constraint[Type]): ConstrainedSchema[Type, InternalInterpreter] =
    ConstrainedSchema[Type, InternalInterpreter](self, constraint)

  def and[B, BInterpreter[Format[_]] <: Interpreter[Format, B]](
    schema: Schema.Aux[B, BInterpreter]
  ): And[Type, B, InternalInterpreter, BInterpreter] =
    And[Type, B, InternalInterpreter, BInterpreter](self, schema)

  def or[B, BInterpreter[Format[_]] <: Interpreter[Format, B]](
    schema: Schema.Aux[B, BInterpreter]
  ): Or[Type, B, InternalInterpreter, BInterpreter] =
    Or[Type, B, InternalInterpreter, BInterpreter](self, schema)

  def inmap[B](f: Type => B)(g: B => Type): InvariantSchema[Type, B, InternalInterpreter] =
    InvariantSchema[Type, B, InternalInterpreter](self.asInstanceOf[Schema.Aux[Type, InternalInterpreter]], f, g)
}

object Schema extends LowPriorityImplicits {

  type Aux[Type, InternalInterpreter0[Format[_]] <: Interpreter[Format, Type]] = Schema[Type] { type InternalInterpreter[F[_]] = InternalInterpreter0[F] }

  object Aux {
    implicit def toAux[Type](schema: Schema[Type]): Schema.Aux[Type, schema.InternalInterpreter] = schema
  }
  implicit def fromAux[Type, Interpret[Format[_]] <: Interpreter[Format, Type]](schema: Schema.Aux[Type, Interpret]): Schema[Type] = schema

  def apply[Type <: Product, Composition, OwnInterpreter[Format[_]] <: Interpreter[Format, Composition]](
    schema: Schema.Aux[Composition, OwnInterpreter]
  ): InvariantSchema[Composition, Type, OwnInterpreter] = macro Impl.simple[Type, Composition, OwnInterpreter]

  //  def apply[Type <: Product]: ApplySchemeInferer[Type] = new ApplySchemeInferer[Type]
  //
  //  final class ApplySchemeInferer[Type] {
  //    def apply[Composition, OwnInterpreter[Format[_]] <: Interpreter[Format, Composition]](
  //      schema: Schema.Aux[Composition, OwnInterpreter]
  //    ): InvariantSchema[Composition, Type, OwnInterpreter] = macro Impl.simple[Composition, OwnInterpreter]
  //  }
}

private[schema] trait LowPriorityImplicits {
  def many[Type, Collection[A] <: Iterable[A]]: ManySchemeInferer[Type, Collection] =
    new ManySchemeInferer[Type, Collection]

  final class ManySchemeInferer[Type, Collection[A] <: Iterable[A]] {
    def apply[TypeInterpreter[Format[_]] <: Interpreter[Format, Type]]()(
      implicit
      schema: Schema.Aux[Type, TypeInterpreter]
    ): ManySchema[Collection, Type, TypeInterpreter] =
      new ManySchema[Collection, Type, TypeInterpreter](schema)
  }

  implicit def simpleString: SimpleSchema[String] = new SimpleSchema[String]
  implicit def simpleBigDecimal: SimpleSchema[BigDecimal] = new SimpleSchema[BigDecimal]
  implicit def simpleBigInt: SimpleSchema[BigInt] = new SimpleSchema[BigInt]
  implicit def simpleZDT: SimpleSchema[ZonedDateTime] = new SimpleSchema[ZonedDateTime]
  implicit def simpleD: SimpleSchema[LocalDate] = new SimpleSchema[LocalDate]
  implicit def simpleT: SimpleSchema[LocalTime] = new SimpleSchema[LocalTime]
  implicit def simple[Type <: AnyVal]: SimpleSchema[Type] = new SimpleSchema[Type]
}

//object StrictSchema {
//  def apply[Typ, Composition](schema: Schema[Composition]): Schema[Typ] = macro Impl.strict[Typ, Composition]
//  def of[Type]: Schema[Type] = macro Impl.strictOf[Type]
//}

object Impl {
  def simple[Typ <: Product: c.WeakTypeTag, Composition: c.WeakTypeTag, OwnInterpreter[Format[_]] <: Interpreter[Format, Composition]](
    c: blackbox.Context
  )(
    schema: c.Expr[Schema.Aux[Composition, OwnInterpreter]]
  ): c.Expr[InvariantSchema[Composition, Typ, OwnInterpreter]] = {
    import c.universe._

      def isProduct(typ: Type): Boolean =
        typ <:< typeOf[Product]

      def isCoproduct(symbol: Symbol): Boolean =
        symbol.asClass.knownDirectSubclasses.nonEmpty && symbol.asClass.knownDirectSubclasses.forall(symbol =>
          symbol.isType && (isProduct(symbol.asType.toType) || isCoproduct(symbol)))

      def abort(error: String): Nothing =
        c.abort(c.enclosingPosition, error)

    val typTag = implicitly[c.WeakTypeTag[Typ]]
    val compositionTag = implicitly[c.WeakTypeTag[Composition]]
    val own = implicitly[c.WeakTypeTag[OwnInterpreter[Id]]]

    type Id[A] = A
    println("@@@@@@@@@@     " + own.tpe.typeConstructor.dealias.typeSymbol)
    println("@@@@@@@@@@     " + typTag.tpe.dealias)
    println("@@@@@@@@@@     " + compositionTag.tpe.dealias)

    if (isProduct(typTag.tpe)) {
        def buildParts(part: Type): List[Type] = {
          val args = part.typeArgs
          if (!(part.typeConstructor =:= typeOf[(_, _)].typeConstructor) || args.isEmpty) List(part)
          else buildParts(args(1)) :+ args.head
        }
        def findApply(): Option[Symbol] = {
          val maybeCompanionApply = typTag.tpe.companion.decl(TermName("apply"))
          if (maybeCompanionApply == NoSymbol) {
            val maybeSelfApply = typTag.tpe.decl(TermName("apply"))
            if (maybeSelfApply == NoSymbol)
              None
            else
              Some(maybeSelfApply)
          }
          else
            Some(maybeCompanionApply)
        }
      val parts = buildParts(compositionTag.tpe)

      val fieldsTypes = typTag.tpe.decls.sorted.collect {
        case m: TermSymbol if m.isVal && m.isCaseAccessor => m.infoIn(typTag.tpe)
      }

      val partsNotInTyp = parts.filterNot(fieldsTypes.contains)
      if (partsNotInTyp.nonEmpty)
        abort(s"types ${partsNotInTyp.mkString(",")} aren't part of ${typTag.tpe.typeSymbol.name}")

      val missingParts = fieldsTypes.filterNot(parts.contains)
      if (missingParts.nonEmpty)
        abort(s"missing schemas for types ${missingParts.mkString(", ")} to build a schema for ${typTag.tpe.typeSymbol.name}")

      findApply().orElse(abort(s"No apply function found for type ${typTag.tpe.typeSymbol.name}"))
      c.Expr[InvariantSchema[Composition, Typ, OwnInterpreter]](q"""
        $schema.inmap(${typTag.tpe.typeSymbol}.apply _ tupled)(${typTag.tpe.typeSymbol}.unapply _ andThen (_.get))
      """)
    }
    else if (isCoproduct(typTag.tpe.typeSymbol)) {

        def buildParts(part: Type): List[Type] = {
          val args = part.typeArgs
          if (!(part.typeConstructor =:= typeOf[Either[_, _]].typeConstructor) || args.isEmpty) List(part)
          else buildParts(args(1)) :+ args.head
        }
      val parts = buildParts(compositionTag.tpe)

      val typSubtypes = typTag.tpe.typeSymbol.asClass.knownDirectSubclasses.map(childClass => childClass.asType.toType)

      if (typSubtypes.isEmpty)
        abort(s"sealed trait ${typTag.tpe.typeSymbol.name} has no subclass")

      val partsNotInTypCoproduct = parts.filterNot(typSubtypes.contains)
      if (partsNotInTypCoproduct.nonEmpty)
        abort(s"types ${partsNotInTypCoproduct.mkString(",")} aren't subtypes of ${typTag.tpe.typeSymbol.name.decodedName.toString}")

      val missingParts = typSubtypes.filterNot(parts.contains)
      if (missingParts.nonEmpty)
        abort(s"missing schemas for types ${missingParts.mkString(",")} to build a schema for ${typTag.tpe.typeSymbol.name.decodedName.toString}")

      val eitherCases =
        parts.indices.map(index =>
          if (index == parts.length - 1)
            "case " + "Left(" * index + "value" + ")" * index + " => value "
          else
            "case " + "Left(" * index + "Right(value)" + ")" * index + " => value ")

      val typCases =
        parts.indices.map(index =>
          if (index == parts.length - 1)
            s"case value: ${parts(index).typeSymbol.name.decodedName.toString} => " + "Left(" * index + "value" + ") " * index
          else
            s"case value: ${parts(index).typeSymbol.name.decodedName.toString} => " + "Left(" * index + "Right(value)" + ") " * index)

      c.Expr[InvariantSchema[Composition, Typ, OwnInterpreter]](q"""
        import scala.language.higherKinds
        new io.github.methrat0n.restruct.schema.Schema[${typTag.tpe.typeSymbol}] {
          def bind[FORMAT[_]](algebra: io.github.methrat0n.restruct.core.data.schema.FieldAlgebra[FORMAT]): FORMAT[${typTag.tpe.typeSymbol}] = {
            algebra.imap($schema.bind(algebra))(either =>
              ${c.parse(s"either match { ${eitherCases.mkString("\n")} }")}
            )(typ =>
              ${c.parse(s"typ match { ${typCases.mkString("\n")} }")}
            )
          }.asInstanceOf[FORMAT[${typTag.tpe.typeSymbol}]]
        }
      """) //TODO remove the asInstanceOf
    }
    else {
      abort(
        s"Type '${typTag.tpe.typeSymbol.name}' must either be a case class or a sealed trait"
      )
    }
  }

  /*
  def strict[Typ: c.WeakTypeTag, Composition: c.WeakTypeTag](c: blackbox.Context)(schema: c.Expr[Schema[Composition]]): c.Expr[Schema[Typ]] = {
    import c.universe._

    def isProduct(typ: Type): Boolean =
      typ <:< typeOf[Product]

    def isCoproduct(symbol: Symbol): Boolean =
      symbol.asClass.knownDirectSubclasses.nonEmpty && symbol.asClass.knownDirectSubclasses.forall(symbol =>
        symbol.isType && (isProduct(symbol.asType.toType) || isCoproduct(symbol)))

    def abort(error: String): Nothing =
      c.abort(c.enclosingPosition, error)

    val typTag = implicitly[c.WeakTypeTag[Typ]]
    val compositionTag = implicitly[c.WeakTypeTag[Composition]]

    def buildParts(part: Type): List[Type] = {
      val args = part.typeArgs
      if (args.isEmpty) List(part)
      else buildParts(args(1)) :+ args.head
    }
    val parts = buildParts(compositionTag.tpe)

    if (isCoproduct(typTag.tpe.typeSymbol)) {
      val typSubtypes = typTag.tpe.typeSymbol.asClass.knownDirectSubclasses.map(childClass => childClass.asType.toType)

      if(typSubtypes.isEmpty)
        abort(s"sealed trait ${typTag.tpe.typeSymbol.name} has no subclass")

      val partsNotInTypCoproduct = parts.filterNot(typSubtypes.contains)
      if(partsNotInTypCoproduct.nonEmpty)
        abort(s"types ${partsNotInTypCoproduct.mkString(",")} aren't subtypes of ${typTag.tpe.typeSymbol.name.decodedName.toString}")

      val missingParts = typSubtypes.filterNot(parts.contains)
      if(missingParts.nonEmpty)
        abort(s"missing schemas for types ${missingParts.mkString(",")} to build a schema for ${typTag.tpe.typeSymbol.name.decodedName.toString}")

      val eitherCases =
        parts.indices.map(index =>
          if(index == parts.length - 1)
            "case " + "Left(" * index + "(value, _)" + ")" * index + " => value "
          else
            "case " + "Left(" * index + "Right((value, _))" + ")" * index + " => value "
        )

      val typCases =
        parts.indices.map(index =>
          if(index == parts.length - 1)
            s"case value: ${parts(index).typeSymbol.name.decodedName.toString} => " + "Left(" * index + "(value, value.getClass.getSimpleName)" + ") " * index
          else
            s"case value: ${parts(index).typeSymbol.name.decodedName.toString} => " + "Left(" * index + "Right((value, value.getClass.getSimpleName))" + ") " * index
        )

      def buildSubSchemas(schema: Tree): List[Tree] = {
        schema match {
          case q"$sub.or[$typ]($subs)" => List(sub.asInstanceOf[Tree]) ++ buildSubSchemas(subs.asInstanceOf[Tree])
          case q"$sub" => List(sub.asInstanceOf[Tree])
        }
      }

      val subSchemas = buildSubSchemas(schema.tree)

      if(subSchemas.length != parts.length)
        abort("Incoherent call: The number of found schema is different than the number of type in Composition")

      val subsWithFlag = subSchemas.zipWithIndex.map { case (sub, index) =>
        s"""algebra.product(
           |$sub.bind(algebra),
           |io.github.methrat0n.restruct.schema.RequiredField(
           |  io.github.methrat0n.restruct.core.data.schema.Path(
           |    io.github.methrat0n.restruct.schema.StepList(
           |      io.github.methrat0n.restruct.schema.StringStep("__type"),List.empty)
           |    ),
           |    io.github.methrat0n.restruct.schema.Syntax.string.constraintedBy(
           |      io.github.methrat0n.restruct.core.data.constraints.Constraints.EqualConstraint("${parts((parts.length - 1) - index).typeSymbol.name.decodedName.toString}")
           |    ), None
           |  ).bind(algebra)
           |)""".stripMargin
      }
      val typName = typTag.tpe.typeSymbol.name.decodedName.toString
      val restructSchema = subsWithFlag.tail.foldLeft(subsWithFlag.head)((acc, sub) => s"algebra.or($acc,$sub)")
      c.Expr[Schema[Typ]](c.parse(s"""
          import scala.language.higherKinds
          new io.github.methrat0n.restruct.schema.Schema[$typName] {
            def bind[FORMAT[_]](algebra: io.github.methrat0n.restruct.core.data.schema.FieldAlgebra[FORMAT]): FORMAT[$typName] = {
              algebra.imap($restructSchema)( either =>
                either match { ${eitherCases.mkString("\n")} }
              )((typ: $typName) =>
                typ match { ${typCases.mkString("\n")} }
              )
            }.asInstanceOf[FORMAT[$typName]]
          }
        """)) //TODO remove the asInstanceOf
    }
    else {
      abort(
        s"Type '${typTag.tpe.typeSymbol.name}' must be a sealed trait"
      )
    }
  }

  //The only difference between this two is the apply called at the end.
  def simpleOf[T: c.WeakTypeTag](c: blackbox.Context): c.Expr[Schema[T]] = {
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
          val internalTyp = typ.typeArgs.head
          val fieldName = name.decodedName.toString.replaceAll(" ", "")
          q"""
             io.github.methrat0n.restruct.schema.OptionalField[$internalTyp](
               io.github.methrat0n.restruct.core.data.schema.Path(
                 io.github.methrat0n.restruct.schema.StepList(
                   io.github.methrat0n.restruct.schema.StringStep($fieldName), List.empty
                 )
               ), $treeSchema, None
             )
          """
        case (name, typ) =>
          val treeSchema = inferSchema(typ)
          val fieldName = name.decodedName.toString.replaceAll(" ", "")
          q"""
              io.github.methrat0n.restruct.schema.RequiredField[$typ](
                io.github.methrat0n.restruct.core.data.schema.Path(
                  io.github.methrat0n.restruct.schema.StepList(
                    io.github.methrat0n.restruct.schema.StringStep($fieldName), List.empty
                  )
                ), $treeSchema, None
              )
          """
      }
      val composeFields = fieldsTree.tail.foldLeft(fieldsTree.head)((acc, field) => q"$acc.and($field)")
      val schemaTree = q"io.github.methrat0n.restruct.schema.Schema($composeFields)"
      c.Expr[Schema[T]](c.untypecheck(schemaTree))
    }
    else if (isCoproduct(tag.tpe.typeSymbol)) {
      val schemas = tag.tpe.typeSymbol.asClass.knownDirectSubclasses.map(childClass => inferSchema(childClass.asType.toType))
      val composeFields = schemas.tail.foldLeft(schemas.head)((acc, field) => q"$acc.or($field)")
      val schemaTree = q"io.github.methrat0n.restruct.schema.Schema($composeFields)"
      c.Expr[Schema[T]](c.untypecheck(schemaTree))
    }
    else {
      c.abort(
        c.enclosingPosition,
        s"Type '${tag.tpe.typeSymbol.name}' must either be a Product or a Coproduct"
      )
    }
  }

  def strictOf[T: c.WeakTypeTag](c: blackbox.Context): c.Expr[Schema[T]] = {
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
          val internalTyp = typ.typeArgs.head
          val fieldName = name.decodedName.toString.replaceAll(" ", "")
          q"""
             io.github.methrat0n.restruct.schema.OptionalField[$internalTyp](
               io.github.methrat0n.restruct.core.data.schema.Path(
                 io.github.methrat0n.restruct.schema.StepList(
                   io.github.methrat0n.restruct.schema.StringStep($fieldName), List.empty
                 )
               ), $treeSchema, None
             )
          """
        case (name, typ) =>
          val treeSchema = inferSchema(typ)
          val fieldName = name.decodedName.toString.replaceAll(" ", "")
          q"""
              io.github.methrat0n.restruct.schema.RequiredField[$typ](
                io.github.methrat0n.restruct.core.data.schema.Path(
                  io.github.methrat0n.restruct.schema.StepList(
                    io.github.methrat0n.restruct.schema.StringStep($fieldName), List.empty
                  )
                ), $treeSchema, None
              )
          """
      }
      val composeFields = fieldsTree.tail.foldLeft(fieldsTree.head)((acc, field) => q"$acc.and($field)")
      val schemaTree = q"io.github.methrat0n.restruct.schema.Schema($composeFields)"
      c.Expr[Schema[T]](c.untypecheck(schemaTree))
    }
    else if (isCoproduct(tag.tpe.typeSymbol)) {
      val schemas = tag.tpe.typeSymbol.asClass.knownDirectSubclasses.map(childClass => inferSchema(childClass.asType.toType))
      val composeFields = schemas.tail.foldLeft(schemas.head)((acc, field) => q"$acc.or($field)")
      val schemaTree = q"io.github.methrat0n.restruct.schema.StrictSchema($composeFields)"
      c.Expr[Schema[T]](c.untypecheck(schemaTree))
    }
    else {
      c.abort(
        c.enclosingPosition,
        s"Type '${tag.tpe.typeSymbol.name}' must either be a Product or a Coproduct"
      )
    }
  }
  */
}
