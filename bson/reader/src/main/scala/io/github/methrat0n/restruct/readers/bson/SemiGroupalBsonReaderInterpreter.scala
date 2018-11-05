package io.github.methrat0n.restruct.readers.bson

import reactivemongo.bson.{ BSONReader, BSONValue }
import io.github.methrat0n.restruct.core.data.schema.SemiGroupalAlgebra

trait SemiGroupalBsonReaderInterpreter extends SemiGroupalAlgebra[BsonReader] {
  override def either[A, B](fa: BsonReader[A], fb: BsonReader[B]): BsonReader[Either[A, B]] =
    BSONReader[BSONValue, Either[A, B]](bsonValue => fa.asInstanceOf[BSONReader[BSONValue, A]].readOpt(bsonValue) match {
      case Some(a) => Left(a)
      case None    => Right(fb.asInstanceOf[BSONReader[BSONValue, B]].read(bsonValue))
    })

  override def product[A, B](fa: BsonReader[A], fb: BsonReader[B]): BsonReader[(A, B)] =
    BSONReader[BSONValue, (A, B)](value => (
      fa.asInstanceOf[BSONReader[BSONValue, A]].read(value),
      fb.asInstanceOf[BSONReader[BSONValue, B]].read(value)
    ))
}
