package io.github.methrat0n.restruct.writers.bson

import reactivemongo.bson.{ BSONArray, BSONDocument }
import io.github.methrat0n.restruct.core.data.schema.SemiGroupalAlgebra

trait SemiGroupalBsonWriterInterpreter extends SemiGroupalAlgebra[BsonWriter] {
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
}
