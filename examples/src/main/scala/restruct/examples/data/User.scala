package restruct.examples.data

import java.time.LocalDate

import restruct.core.Program
import restruct.core.data.schema.{ Schema, SchemaAlgebra }

case class Store(users: List[User], isOpen: Boolean = true, owner: Option[User])

case class User(name: String, aging: Aging, money: Double, isSmart: Boolean)

case class Aging(age: Int, birthDate: Int, deathDate: Int = LocalDate.now().getYear)

object Store {
  implicit val schema: Program[SchemaAlgebra, Store] = Schema.from[Store]
}

object User {
  implicit val schema: Program[SchemaAlgebra, User] = Schema.from[User]
}

object Aging {
  implicit val schema: Program[SchemaAlgebra, Aging] = Schema.from[Aging]
}
