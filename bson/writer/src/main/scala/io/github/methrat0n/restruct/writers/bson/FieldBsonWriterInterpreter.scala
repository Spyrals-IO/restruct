package io.github.methrat0n.restruct.writers.bson

import io.github.methrat0n.restruct.core.data.constraints.Constraint
import io.github.methrat0n.restruct.core.data.schema.FieldAlgebra
import reactivemongo.bson.{ BSONArray, BSONDocument }

trait FieldBsonWriterInterpreter extends FieldAlgebra[BsonWriter] {

  override def optional[T](name: String, schema: BsonWriter[T], default: Option[Option[T]]): BsonWriter[Option[T]] =
    BsonWriter(
      option => BSONDocument(
        name -> option.map(schema.write)
      )
    )

  override def required[T](name: String, schema: BsonWriter[T], default: Option[T]): BsonWriter[T] =
    BsonWriter(
      required => BSONDocument(
        name -> schema.write(required)
      )
    )

  override def verifying[T](schema: BsonWriter[T], constraint: Constraint[T]): BsonWriter[T] =
    schema

  override def either[A, B](fa: BsonWriter[A], fb: BsonWriter[B]): BsonWriter[Either[A, B]] = BsonWriter({
    case Right(b) => fb.write(b)
    case Left(a)  => fa.write(a)
  })

  override def product[A, B](fa: BsonWriter[A], fb: BsonWriter[B]): BsonWriter[(A, B)] = BsonWriter({
    case (a, b) => (fa.write(a), fb.write(b)) match {
      case (bsonA: BSONDocument, bsonB: BSONDocument) => bsonA.merge(bsonB)
      case (bsonA: BSONArray, bsonB: BSONArray)       => bsonA.merge(bsonB)
      case (bsonA: BSONDocument, bsonB: BSONArray)    => bsonA.merge(bsonB)
      case (bsonA: BSONArray, bsonB: BSONDocument)    => bsonB.merge(bsonA)
      case (bsonA, bsonB)                             => throw new RuntimeException(s"Impossible case exception: cannot merge two BsonValue wich aren't BSONArray or BSONDocument. value1: $bsonA, value2: $bsonB")
    }
  })

  override def imap[A, B](fa: BsonWriter[A])(f: A => B)(g: B => A): BsonWriter[B] =
    fa.beforeWrite[B](g)

  override def pure[T](a: T): BsonWriter[T] = BsonWriter(
    _ => BSONDocument.empty
  )

}
