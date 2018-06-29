package restruct.examples

import java.time.LocalDate

case class Store(users: List[User], isOpen: Boolean = true, owner: Option[User])

case class User(name: String, aging: Aging, monney: Double, isSmart: Boolean)

case class Aging(age: Int, birthdDate: LocalDate, deathDate: LocalDate = LocalDate.now())