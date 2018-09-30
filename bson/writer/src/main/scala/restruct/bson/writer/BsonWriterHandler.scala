package restruct.bson.writer

import java.time.ZonedDateTime
import java.util.Date

import reactivemongo.bson.{BSONArray, BSONDocument, BSONValue, BSONWriter, DefaultBSONHandlers}
import restruct.core.Program
import restruct.core.data.constraints.Constraint
import restruct.core.data.schema.SchemaAlgebra

object BsonWriterHandler {

  def run[T](program: Program[SchemaAlgebra, T]): BSONWriter[T, BSONValue] = program.run[BSONWriter[?, BSONValue]](Handler)

  private[bson] object Handler extends SchemaAlgebra[BSONWriter[?, BSONValue]] {
    override def stringSchema: BSONWriter[String, BSONValue] =
      DefaultBSONHandlers.BSONStringHandler.asInstanceOf[BSONWriter[String, BSONValue]]

    override def decimalSchema: BSONWriter[Double, BSONValue] =
      DefaultBSONHandlers.BSONDoubleHandler.asInstanceOf[BSONWriter[Double, BSONValue]]

    override def integerSchema: BSONWriter[Int, BSONValue] =
      DefaultBSONHandlers.BSONIntegerHandler.asInstanceOf[BSONWriter[Int, BSONValue]]

    override def longSchema: BSONWriter[Long, BSONValue] =
      DefaultBSONHandlers.BSONLongHandler.asInstanceOf[BSONWriter[Long, BSONValue]]

    override def booleanSchema: BSONWriter[Boolean, BSONValue] =
      DefaultBSONHandlers.BSONBooleanHandler.asInstanceOf[BSONWriter[Boolean, BSONValue]]

    override def bigDecimalSchema: BSONWriter[BigDecimal, BSONValue] =
      DefaultBSONHandlers.BSONDecimalHandler.asInstanceOf[BSONWriter[BigDecimal, BSONValue]]

    override def dateTimeSchema: BSONWriter[ZonedDateTime, BSONValue] =
      DefaultBSONHandlers.BSONDateTimeHandler
        .beforeWrite[ZonedDateTime](zonedDateTime => Date.from(zonedDateTime.toInstant))
        .asInstanceOf[BSONWriter[ZonedDateTime, BSONValue]]

    override def many[T](name: String, schema: BSONWriter[T, BSONValue], default: Option[List[T]]): BSONWriter[List[T], BSONValue] = BSONWriter[List[T], BSONValue](
      list => BSONDocument(
        name -> DefaultBSONHandlers.collectionToBSONArrayCollectionWriter[T, List[T]](identity, schema).write(list)
      )
    )
    override def keyValue[T](name: String, schema: BSONWriter[T, BSONValue], default: Option[Map[String, T]]): BSONWriter[Map[String, T], BSONValue] = BSONWriter(
      map => BSONDocument(
        name -> BSONDocument(map.mapValues(schema.write).toList)
      )
    )

    override def optional[T](name: String, schema: BSONWriter[T, BSONValue], default: Option[Option[T]]): BSONWriter[Option[T], BSONValue] = BSONWriter(
      option => BSONDocument(
        name -> option.map(schema.write)
      )
    )

    override def required[T](name: String, schema: BSONWriter[T, BSONValue], default: Option[T]): BSONWriter[T, BSONValue] = BSONWriter[T, BSONValue](
      required => BSONDocument(
        name -> schema.write(required)
      )
    )

    override def verifying[T](schema: BSONWriter[T, BSONValue], constraint: Constraint[T]): BSONWriter[T, BSONValue] =
      schema

    override def either[A, B](fa: BSONWriter[A, BSONValue], fb: BSONWriter[B, BSONValue]): BSONWriter[Either[A, B], BSONValue] = BSONWriter({
      case Right(b) => fb.write(b)
      case Left(a)  => fa.write(a)
    })

    override def pure[T](a: T): BSONWriter[T, BSONValue] = BSONWriter[T, BSONValue](
      _ => BSONDocument()
    )

    override def imap[A, B](fa: BSONWriter[A, BSONValue])(f: A => B)(g: B => A): BSONWriter[B, BSONValue] =
      fa.beforeWrite[B](g)

    override def product[A, B](fa: BSONWriter[A, BSONValue], fb: BSONWriter[B, BSONValue]): BSONWriter[(A, B), BSONValue] = BSONWriter({
      case (a, b) => (fa.write(a), fb.write(b)) match {
        case (bsonA: BSONDocument, bsonB: BSONDocument) => bsonA.merge(bsonB)
        case (bsonA: BSONArray, bsonB: BSONArray)       => bsonA.merge(bsonB)
        case (bsonA: BSONDocument, bsonB: BSONArray)    => bsonA.merge(bsonB)
        case (bsonA: BSONArray, bsonB: BSONDocument)    => bsonB.merge(bsonA)
        case (bsonA, bsonB)                             => throw new RuntimeException(s"Impossible case exception: cannot merge two BsonValue wich aren't BSONArray or BSONDocument. value1: $bsonA, value2: $bsonB")
      }
    })
  }
}
