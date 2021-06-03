package io.github.methrat0n.restruct.schema

import java.time.{LocalDate, LocalTime, ZonedDateTime}

import io.github.methrat0n.restruct.constraints.Constraint
import io.github.methrat0n.restruct.schema.Schemas._

import scala.language.experimental.macros
import scala.reflect.macros._

trait Schema[Type, InternalInterpreter[Format[_]] <: Interpreter[Format, Type]] { self =>

  def bind[Format[_]](implicit interpreter: InternalInterpreter[Format]): Format[Type]

  def constraintedBy(constraint: Constraint[Type]): ConstrainedSchema[Type, InternalInterpreter] =
    ConstrainedSchema[Type, InternalInterpreter](self, constraint)

  def and[B, BInterpreter[Format[_]] <: Interpreter[Format, B]](
    schema: Schema[B, BInterpreter]
  ): And[Type, B, InternalInterpreter, BInterpreter] =
    And[Type, B, InternalInterpreter, BInterpreter](self, schema)

  def or[B, BInterpreter[Format[_]] <: Interpreter[Format, B]](
    schema: Schema[B, BInterpreter]
  ): Or[Type, B, InternalInterpreter, BInterpreter] =
    Or[Type, B, InternalInterpreter, BInterpreter](self, schema)

  def inmap[B](f: Type => B)(g: B => Type): InvariantSchema[Type, B, InternalInterpreter] =
    InvariantSchema[Type, B, InternalInterpreter](self, f, g)
}

object Schema extends LowPriorityImplicits {

  /**
   * Automatically derive the Schema of a type.
   *
   * Example:
   * {{{
   *   final case class User(name: String, age: Int)
   *   object User {
   *      // both equivalent
   *     implicit val schema = Schema.of(User)
   *
   *     implicit val schema = Schema(
   *       (Path \ "name").as[String]() and
   *       (Path \ "age").as[Int]()
   *     )
   *   }
   * }}}
   */
  def of[Type <: Product]: Any = macro Impl.of[Type]

  /**
   * Transform a Schema of parts into one of a single type.
   *
   * Example:
   * {{{
   *   final case class User(name: String, age: Int)
   *   object User {
   *     val parts = (
   *       (Path \ "name").as[String]() and
   *       (Path \ "age").as[Int]()
   *     )
   *
   *     // both equivalent
   *     implicit val schema = Schema(parts)
   *
   *     implicit val schema = parts.inmap {
   *       case (name, age) => User(name, age)
   *     } {
   *       case User(name, age) => (name, age)
   *     }
   *   }
 *   }}}
   */
  def apply[Type <: Product]: ApplySchemeInferer[Type] = new ApplySchemeInferer[Type]

  final class ApplySchemeInferer[Type <: Product] {
    def apply[Typ <: Type, Composition, OwnInterpreter[Format[_]] <: Interpreter[Format, Composition]](
      schema: Schema[Composition, OwnInterpreter]
    ): InvariantSchema[Composition, Type, OwnInterpreter] = macro Impl.simple[Type, Composition, OwnInterpreter]
  }

  /**
   * Strict schemas are only relevant for coproduct as it will make no difference for Products.
   * Strict schema will include (and therefore read/write) a field to denote the runtime type.
   */
  object Strict {
    /**
     * Equivalent of Schema.apply but for strict schemas.
     *
     * use `__type` as default field to stock the type name.
     */
    def apply[Type <: Product]: ApplySchemeInferer[Type] = new ApplySchemeInferer[Type]
    final class ApplySchemeInferer[Type <: Product] {
      def apply[Typ <: Type, Composition, OwnInterpreter[Format[_]] <: Interpreter[Format, Composition]](
        schema: Schema[Composition, OwnInterpreter]
      ): Any = macro Impl.defaultStrict[Type, Composition, OwnInterpreter]
      //TODO this macro could be a blackbox by type level computing of the Interpreter
    }

    /**
     * Equivalent of Strict.apply but let you use a custom field for the type name.
     */
    def withTypeMarker[Type <: Product]: WithTypeMarkerSchemeInferer[Type] = new WithTypeMarkerSchemeInferer[Type]
    final class WithTypeMarkerSchemeInferer[Type <: Product] {
      def apply[Typ <: Type, Composition, OwnInterpreter[Format[_]] <: Interpreter[Format, Composition]](
        schema: Schema[Composition, OwnInterpreter]
      )(
        typeMarker: String
      ): Any = macro Impl.strict[Type, Composition, OwnInterpreter]
    }

    /**
     * Strict equivalent of Schema.of
     */
    def of[Type <: Product]: Any = macro Impl.strictOf[Type]
  }
}

private[schema] trait LowPriorityImplicits {
  def many[Type, Collection[A] <: Iterable[A]]: ManySchemeInferer[Type, Collection] =
    new ManySchemeInferer[Type, Collection]

  final class ManySchemeInferer[Type, Collection[A] <: Iterable[A]] {
    def apply[TypeInterpreter[Format[_]] <: Interpreter[Format, Type]]()(
      implicit
      schema: Schema[Type, TypeInterpreter]
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

object Impl {
  class MacroSchemeInferer[Type]() {
    def apply[InternalInterpreter[Format[_]] <: Interpreter[Format, Type]]()(
      implicit schema: Schema[Type, InternalInterpreter]
    ): Schema[Type, InternalInterpreter] = schema
  }

  def simple[Typ <: Product, Composition, OwnInterpreter[Format[_]] <: Interpreter[Format, Composition]](
    c: blackbox.Context
  )(
    schema: c.Expr[Schema[Composition, OwnInterpreter]]
  )(implicit
    typTag: c.WeakTypeTag[Typ],
    compositionTag: c.WeakTypeTag[Composition]
  ): c.Expr[InvariantSchema[Composition, Typ, OwnInterpreter]] = {
    val helper = new MacroHelper[Typ](c)
    import helper.Utils._
    import helper.c.universe._
    val dimensionalTypTag = typTag.in(helper.c.mirror)
    val dimensionalCompositionTag = compositionTag.in(helper.c.mirror)
    val dimensionalSchema = schema.in(helper.c.mirror)

    if (isProduct(dimensionalTypTag.tpe)) {
      val product = new helper.Product(dimensionalTypTag)
      import product._

      val parts = buildParts(dimensionalCompositionTag.tpe)
      checkCompatibility(parts)
      val apply = findApply().getOrElse(abort(s"No apply function found for type ${dimensionalTypTag.tpe.typeSymbol.name}"))
      val variables = listVariables(apply)
      val tupledClause = buildTupledClause(apply)

      val inlining = q"""{
        $dimensionalSchema.inmap(tupled =>
          ${helper.c.parse(s"${typTag.tpe.typeSymbol.name.decodedName.toString}(${variables.mkString(",")})")}
        )(typ =>
          ${helper.c.parse(tupledClause)}
        )
      }"""
      helper.c.Expr[InvariantSchema[Composition, Typ, OwnInterpreter]](inlining)
        .asInstanceOf[c.Expr[InvariantSchema[Composition, Typ, OwnInterpreter]]]
    } else if (isCoproduct(dimensionalTypTag.tpe)) {
      val coproduct = new helper.Coproduct(dimensionalTypTag)
      import coproduct._

      val parts = buildParts(dimensionalCompositionTag.tpe)
      val subSchemas = buildSubSchemas(dimensionalSchema.tree)
      val subs = directSubtypes(dimensionalTypTag)
      checkCompatibility(subSchemas, parts, subs)

      def buildEitherCases(parts: List[Type]): Seq[String] =
        parts.zipWithIndex.map { case (part, index) =>
          val typName = part.typeSymbol.name.decodedName.toString
          if (index == parts.length - 1)
            "case " + "Left(" * index + s"value: $typName" + ")" * index + " => value "
          else
            "case " + "Left(" * index + s"Right(value: $typName)" + ")" * index + " => value "
        }

      def buildTypCases(parts: List[Type]): Seq[String]=
        parts.zipWithIndex.map { case (part, index) =>
          val typName = part.typeSymbol.name.decodedName.toString
          if(index == parts.length - 1)
            s"case value: $typName => " + "Left(" * index + "value" + ") " * index
          else
            s"case value: $typName => " + "Left(" * index + "Right(value)" + ") " * index
        }

      val eitherCases = buildEitherCases(parts)
      val typCases = buildTypCases(parts)

      val inlining = q"""{
        import scala.language.higherKinds
        $dimensionalSchema.inmap(either =>
          ${helper.c.parse(s"either match { ${eitherCases.mkString("\n")} }")}
        )(typ =>
          ${helper.c.parse(s"typ match { ${typCases.mkString("\n")} }")}
        )
      }"""
      helper.c.Expr[InvariantSchema[Composition, Typ, OwnInterpreter]](inlining)
        .asInstanceOf[c.Expr[InvariantSchema[Composition, Typ, OwnInterpreter]]]
    } else
      abort(s"Type '${typTag.tpe.typeSymbol.name}' must either be a case class or a sealed trait")
  }

  def defaultStrict[Typ <: Product, Composition, OwnInterpreter[Format[_]] <: Interpreter[Format, Composition]](
    c: whitebox.Context
  )(
    schema: c.Expr[Schema[Composition, OwnInterpreter]]
  )(implicit
    typTag: c.WeakTypeTag[Typ],
    compositionTag: c.WeakTypeTag[Composition]
  ): c.Expr[Any] = {
    import c.universe._
    strict(c)(schema)(c.Expr(q""" "__type" """))(typTag, compositionTag)
  }

  def strict[Typ <: Product, Composition, OwnInterpreter[Format[_]] <: Interpreter[Format, Composition]](
    c: whitebox.Context
  )(
    schema: c.Expr[Schema[Composition, OwnInterpreter]]
  )(
    typeMarker: c.Expr[String]
  )(implicit
    typTag: c.WeakTypeTag[Typ],
    compositionTag: c.WeakTypeTag[Composition]
  ): c.Expr[Any] = {
    val helper = new MacroHelper[Typ](c)
    import helper.Utils._
    import helper.c.universe._
    val dimensionalTypTag = typTag.in(helper.c.mirror)
    val dimensionalCompositionTag = compositionTag.in(helper.c.mirror)
    val dimensionalTypeMarker = typeMarker.in(helper.c.mirror)

    if (isProduct(dimensionalTypTag.tpe))
      simple[Typ, Composition, OwnInterpreter](c)(schema)(typTag, compositionTag)
    else if (isCoproduct(dimensionalTypTag.tpe)) {
      val coproduct = new helper.Coproduct(dimensionalTypTag)
      import coproduct._
      val dimensionalSchema = schema.in(helper.c.mirror)

      val subSchemas = buildSubSchemas(dimensionalSchema.tree)
      val parts = buildParts(dimensionalCompositionTag.tpe)

      val typSubtypes = directSubtypes[Typ](dimensionalTypTag)
      checkCompatibility(subSchemas, parts, typSubtypes)

      val reverseParts = parts.reverse //declaration order
      val rewrittenSchema = subSchemas
      .zip(reverseParts)
      .tail.foldLeft(
        q"""${subSchemas.head}.and((Path \ $dimensionalTypeMarker).as[String]().constraintedBy(io.github.methrat0n.restruct.constraints.Constraints.Equal[String](${reverseParts.head.typeSymbol.name.decodedName.toString})))"""
      ){
        case (acc, (sub, part)) => q"""$acc or $sub.and((Path \ $dimensionalTypeMarker).as[String]().constraintedBy(io.github.methrat0n.restruct.constraints.Constraints.Equal[String](${part.typeSymbol.name.decodedName.toString})))"""
      }

      def buildEitherCases(parts: List[Type]): Seq[String] =
        parts.zipWithIndex.map { case (part, index) =>
          if (index == parts.length - 1)
            "case " + "Left(" * index + "(value, _)" + ")" * index + " => value "
          else
            "case " + "Left(" * index + "Right((value, _))" + ")" * index + " => value "
        }

      def buildTypCases(parts: List[Type]): Seq[String]=
        parts.zipWithIndex.map { case (part, index) =>
          val typName = part.typeSymbol.name.decodedName.toString
          if(index == parts.length - 1)
            s"case value: $typName => " + "Left(" * index + s"""(value, "$typName")""" + ") " * index
          else
            s"case value: $typName => " + "Left(" * index + s"""Right((value, "$typName"))""" + ") " * index
        }

      val eitherCases = buildEitherCases(parts)
      val typCases = buildTypCases(parts)

      val inlining = q"""{
        import scala.language.higherKinds
        $rewrittenSchema.inmap(either =>
          ${helper.c.parse(s"either match { ${eitherCases.mkString("\n")} }")}
        )(typ =>
          ${helper.c.parse(s"typ match { ${typCases.mkString("\n")} }")}
        )
      }"""
      helper.c.Expr(inlining).asInstanceOf[c.Expr[Any]]
    } else
      abort(s"Type '${typTag.tpe.typeSymbol.name}' must be a sealed trait")
  }

  def of[Typ](
    c: whitebox.Context
  )(implicit
    typTag: c.WeakTypeTag[Typ]
  ): c.Expr[Any] = {
    val helper = new MacroHelper[Typ](c)
    import helper.Utils._
    import helper.c.universe._
    val dimensionalTypTag = typTag.in(helper.c.mirror)

    if(isProduct(dimensionalTypTag.tpe)) {
      val fieldsTree = dimensionalTypTag.tpe.decls.sorted.collect {
        //TODO default
        case m: TermSymbol if m.isVal && m.isCaseAccessor && m.isParamWithDefault => (m.name, m.typeSignature)
        case m: TermSymbol if m.isVal && m.isCaseAccessor => (m.name, m.typeSignature)
      } map {
        case (name, typ) if isOption(typ) =>
          val internalTyp = typ.typeArgs.head
          val fieldName = name.decodedName.toString.replaceAll(" ", "")
          //TODO default
          q"""
            (Path \ ${fieldName}).asOption[${internalTyp.typeSymbol}]()
           """
        case (name, typ) =>
          val fieldName = name.decodedName.toString.replaceAll(" ", "")
          //TODO default
          q"""
            (Path \ ${fieldName}).as[${typ.typeSymbol}]()
           """
      }
      val composeFields = fieldsTree.tail.foldLeft(fieldsTree.head)((acc, field) => q"$acc.and($field)")

      val inlining = q"""{
        import io.github.methrat0n.restruct.schema._
        Schema.apply[${dimensionalTypTag.tpe}]($composeFields)
       }"""
      helper.c.Expr[Any](inlining).asInstanceOf[c.Expr[Any]]
    } else if(isCoproduct(dimensionalTypTag.tpe)) {
      val subtypes = directSubtypes(dimensionalTypTag)
      if(subtypes.isEmpty)
        abort("Sealed trait without subtype provided")

      def applyMacroSchemeInfere(typ: Type) = q"new io.github.methrat0n.restruct.schema.Impl.MacroSchemeInferer[${typ}].apply()"
      val compositeSchema = subtypes.tail.foldLeft(applyMacroSchemeInfere(subtypes.head))(
        (acc, subtype) => q"$acc or ${applyMacroSchemeInfere(subtype)}"
      )

      val inlining = q"""{
       import io.github.methrat0n.restruct.schema._
       Schema.apply[${dimensionalTypTag.tpe}]($compositeSchema)
      }"""
      helper.c.Expr(inlining).asInstanceOf[c.Expr[Any]]
    } else
      abort(s"Type '${typTag.tpe.typeSymbol.name}' must either be a case class or a sealed trait")
  }

  def strictOf[Typ](
    c: whitebox.Context
  )(implicit
    typTag: c.WeakTypeTag[Typ]
  ): c.Expr[Any] = {
    import c.universe._
    of[Typ](c)(typTag) match {
      case c.Expr(q"""{
        import io.github.methrat0n.restruct.schema._
        Schema.apply[${tpe}]($composeFields)
      }""") => c.Expr(q"""{
        import io.github.methrat0n.restruct.schema._
        Schema.Strict.apply[${tpe}]($composeFields)
      }""")
    }
  }

}

class MacroHelper[Typ](val c: blackbox.Context) {
  import c.universe._

  object Utils {
    def directSubtypes[T](typeTag: WeakTypeTag[T]): Set[Type] =
      typeTag.tpe.typeSymbol.asClass.knownDirectSubclasses.map(childClass => childClass.asType.toType)

    def isProduct(typ: Type): Boolean =
      typ <:< typeOf[scala.Product] && !typ.typeSymbol.isAbstract

    def isCoproduct(typ: Type): Boolean =
      typ.typeSymbol.asClass.knownDirectSubclasses.nonEmpty && typ.typeSymbol.asClass.knownDirectSubclasses.forall(symbol =>
        isProduct(symbol.asType.toType) || isCoproduct(symbol.asType.toType))

    def abort(error: String): Nothing =
      c.abort(c.enclosingPosition, error)

    def isOption(typ: Type): Boolean =
      typ.typeConstructor =:= typeOf[Option[_]].typeConstructor

    //        def defaultsFor(typ: Type,fields: List[(TermName, Type)]) = for {
    //          ((_, argTpe), i) <- fields.zipWithIndex
    //          default = typ.companion.member(TermName(s"apply$$default$$${i + 1}")) orElse
    //            altCompanion.member(TermName(s"$$lessinit$$greater$$default$$${i + 1}"))
    //        } yield if (default.isTerm) {
    //          val defaultTpe = appliedType(someTpe, devarargify(argTpe))
    //          val defaultVal = some(q"$companion.$default")
    //          (defaultTpe, defaultVal)
    //        } else (noneTpe, none)

    // See https://github.com/milessabin/shapeless/issues/212
    //        def companionRef(tpe: Type): Tree = {
    //          val global = c.universe.asInstanceOf[scala.tools.nsc.Global]
    //          val gTpe = tpe.asInstanceOf[global.Type]
    //          val pre = gTpe.prefix
    //          val cSym = patchedCompanionSymbolOf(tpe.typeSymbol).asInstanceOf[global.Symbol]
    //          if(cSym != NoSymbol)
    //            global.gen.mkAttributedRef(pre, cSym).asInstanceOf[Tree]
    //          else
    //            Ident(tpe.typeSymbol.name.toTermName) // Attempt to refer to local companion
    //        }
  }

  class Product(typTag: c.WeakTypeTag[Typ]) {

    def buildParts(part: Type): List[Type] = {
      val args = part.typeArgs
      if (!(part.typeConstructor =:= typeOf[(_, _)].typeConstructor) || args.isEmpty) List(part)
      else buildParts(args(1)) :+ args.head
    }

    def checkCompatibility(parts: List[Type]): Unit = {
      val fieldsTypes = typTag.tpe.decls.sorted.collect {
        case m: TermSymbol if m.isVal && m.isCaseAccessor => m.infoIn(typTag.tpe)
      } map { fieldsType => fieldsType.dealias }

      val dealiasParts = parts.map(_.dealias)

      val partsNotInTyp = dealiasParts.filterNot(fieldsTypes.contains)
      if (partsNotInTyp.nonEmpty)
        Utils.abort(s"types ${partsNotInTyp.mkString(",")} aren't part of ${typTag.tpe.typeSymbol.name}")

      val missingParts = fieldsTypes.filterNot(dealiasParts.contains)
      if (missingParts.nonEmpty)
        Utils.abort(s"missing schemas for types ${missingParts.mkString(", ")} to build a schema for ${typTag.tpe.typeSymbol.name}")
    }

    def findApply(): Option[Symbol] = {
      val maybeCompanionApply = typTag.tpe.companion.decl(TermName("apply"))
      if (maybeCompanionApply == NoSymbol) {
        val maybeSelfApply = typeTag.tpe.decl(TermName("apply"))
        if (maybeSelfApply == NoSymbol)
          None
        else
          Some(maybeSelfApply)
      }
      else
        Some(maybeCompanionApply)
    }

    def listVariables(apply: Symbol): Seq[String] = {
      val variableNames = apply.asMethod.paramLists.flatten
      variableNames match {
        case List(_) => List("tupled")
        case list => (1 until list.length).flatMap(value => {
          val prefix = "tupled" + ("._1" * (list.length - (value + 1)))
          List(
            s"$prefix._1",
            s"$prefix._2",
          )
        })
      }
    }

    def buildTupledClause(apply: Symbol): String = {
      val variableNames = apply.asMethod.paramLists.flatten
      val acessedVariables = variableNames.map(name => s"typ.${name.name.decodedName.toString}")
      acessedVariables.tail match {
        case Nil => acessedVariables.head
        case tail => ("(" * (variableNames.length - 1)) + acessedVariables.head + tail.mkString(start = ", ", sep = "), ", end = ")")
      }
    }
  }

  class Coproduct(typTag: c.WeakTypeTag[Typ]) {

    def buildParts(part: Type): List[Type] = {
      val args = part.typeArgs
      if (args.isEmpty) List(part)
      else buildParts(args(1)) :+ args.head
    }

    def buildSubSchemas(schema: Tree): List[Tree] = {
      schema match {
        case q"$sub.or[$typ, $_]($subs)" => List(sub.asInstanceOf[Tree]) ++ buildSubSchemas(subs.asInstanceOf[Tree])
        case q"$sub" => List(sub.asInstanceOf[Tree])
      }
    }

    def checkCompatibility(subSchemas: List[Tree], parts: List[Type], typSubtypes: Set[Type]): Unit = {
      if(typSubtypes.isEmpty)
        Utils.abort(s"sealed trait ${typTag.tpe.typeSymbol.name} has no subclass")

      val partsNotInTypCoproduct = parts.filterNot(typSubtypes.contains)
      if(partsNotInTypCoproduct.nonEmpty)
        Utils.abort(s"types ${partsNotInTypCoproduct.mkString(",")} aren't subtypes of ${typTag.tpe.typeSymbol.name.decodedName.toString}")

      val missingParts = typSubtypes.filterNot(parts.contains)
      if(missingParts.nonEmpty)
        Utils.abort(s"missing schemas for types ${missingParts.mkString(",")} to build a schema for ${typTag.tpe.typeSymbol.name.decodedName.toString}")

      if(subSchemas.length != parts.length)
        Utils.abort("Incoherent call: The number of found schema is different than the number of type in Composition")
    }

    def buildEitherCases(parts: List[Type]): Seq[String] =
      parts.zipWithIndex.map { case (part, index) =>
        if (index == parts.length - 1)
          "case " + "Left(" * index + "(value, _)" + ")" * index + " => value "
        else
          "case " + "Left(" * index + "Right((value, _))" + ")" * index + " => value "
      }

    def buildTypCases(parts: List[Type]): Seq[String]=
      parts.zipWithIndex.map { case (part, index) =>
        val typName = part.typeSymbol.name.decodedName.toString
        if(index == parts.length - 1)
          s"case value: $typName => " + "Left(" * index + s"""(value, "$typName")""" + ") " * index
        else
          s"case value: $typName => " + "Left(" * index + s"""Right((value, "$typName"))""" + ") " * index
      }
  }
}

