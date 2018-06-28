package lib.core.data.schema

import lib.core.Program
import org.scalatest.{FlatSpec, Matchers}
import play.api.libs.json._

class SchemaAlgebraSpec extends FlatSpec with Matchers {

  import SchemaAlgebraSpec._

  "A Schema Algebra" should "be derived to a Reads" in {
    val addressReads = JsonReadsHandler.run(Address.addressSchema)

    val addressJson = Json.obj("streetAddress" -> "streetAddress", "postalCode" -> "postalCode")
    val addressExpect = JsSuccess(Address("streetAddress", "postalCode"))
    val addressResult = addressReads.reads(addressJson)
    addressResult should be(addressExpect)

    val addressMissingFieldJson = addressJson - "postalCode"
    val addressMissingFieldExpect = JsError(List(
      (JsPath \ "postalCode", List(JsonValidationError(List("error.path.missing"))))
    ))
    val addressMissingFieldResult = addressReads.reads(addressMissingFieldJson)
    addressMissingFieldResult should be(addressMissingFieldExpect)

    val addressBadValueJson = Json.obj("streetAddress" -> "streetAddress", "postalCode" -> 95000)
    val addressBadValueExpect = JsError(List(
      (JsPath \ "postalCode", List(JsonValidationError(List("error.expected.jsstring"))))
    ))
    val addressBadValueResult = addressReads.reads(addressBadValueJson)
    addressBadValueResult should be(addressBadValueExpect)

    val personReads = JsonReadsHandler.run(Person.personSchema)

    val personJson = Json.obj(
      "firstName" -> "firstName",
      "lastName" -> "lastName",
      "gender" -> "Male",
      "address" -> addressJson
    )
    val personExpect = JsSuccess(Person("firstName", "lastName", Gender.Male, Some(addressExpect.get)))
    val personResult = personReads.reads(personJson)
    personResult should be(personExpect)

    val personBadValueJson = Json.obj(
      "firstName" -> "firstName",
      "lastName" -> "lastName",
      "gender" -> "male",
      "address" -> addressJson
    )
    val personBadValueExpect = JsError(List(
      (JsPath \ "gender", List(JsonValidationError(List("error.constraints.enum"), List("Male", "Female"))))
    ))
    val personBadValueResult = personReads.reads(personBadValueJson)
    personBadValueResult should be(personBadValueExpect)

    val personWithoutGenderJson = Json.obj(
      "firstName" -> "firstName",
      "lastName" -> "lastName",
      "address" -> addressJson
    )
    val personWithoutGenderResult = personReads.reads(personWithoutGenderJson)
    personWithoutGenderResult should be(personExpect)

    JsonReadsHandler.run(Fruit.fruitSchema).reads(Json.parse("""{"__type":"Apple"}""")) should be(JsSuccess(Fruit.Apple))
    JsonReadsHandler.run(Fruit.fruitSchema).reads(Json.parse("""{"__type":"Orange"}""")) should be(JsSuccess(Fruit.Orange))
    JsonReadsHandler.run(Fruit.fruitSchema).reads(Json.parse("""{"__type":"error"}""")) should be(JsError(List((JsPath \ "__type", List(JsonValidationError(List("error.constraints.equal"), "Apple"))))))

    JsonReadsHandler.run(Contact.contactSchema).reads(Json.parse("""{"__type":"EmailContact","email":"email"}""")) should be(JsSuccess(Contact.EmailContact("email")))
    JsonReadsHandler.run(Contact.contactSchema).reads(Json.parse("""{"__type":"PhoneContact","phone":"phone"}""")) should be(JsSuccess(Contact.PhoneContact("phone")))
  }

  it should "be derived to a json schema" in {
    val addressJsonSchema = JsonSchemaHandler.run(Address.addressSchema)
    addressJsonSchema should be(Json.parse(
      """{
        |  "type":"object",
        |  "properties":{
        |    "streetAddress":{
        |      "type":"string"
        |    },
        |    "postalCode":{
        |      "type":"string"
        |    }
        |  },
        |  "required":["streetAddress","postalCode"]
        |}""".stripMargin
    ))
    val personJsonSchema = JsonSchemaHandler.run(Person.personSchema)
    personJsonSchema should be(Json.parse(
      """{
        |  "type": "object",
        |  "properties": {
        |    "contactPreference": {
        |      "type": "object",
        |      "properties": {
        |        "message": {
        |          "type": "boolean"
        |        },
        |        "vocal": {
        |          "type": "boolean"
        |        }
        |      },
        |      "required": [
        |        "message",
        |        "vocal"
        |      ],
        |      "default": {
        |        "message": true,
        |        "vocal": true
        |      }
        |    },
        |    "contacts": {
        |      "type": "array",
        |      "items": {
        |        "oneOf": [
        |          {
        |            "type": "object",
        |            "properties": {
        |              "__type": {
        |                "type": "string",
        |                "equal": "PhoneContact"
        |              },
        |              "phone": {
        |                "type": "string"
        |              }
        |            },
        |            "required": [
        |              "__type",
        |              "phone"
        |            ]
        |          },
        |          {
        |            "type": "object",
        |            "properties": {
        |              "__type": {
        |                "type": "string",
        |                "equal": "EmailContact"
        |              },
        |              "email": {
        |                "type": "string"
        |              }
        |            },
        |            "required": [
        |              "__type",
        |              "email"
        |            ]
        |          }
        |        ]
        |      },
        |      "default": [
        |
        |      ]
        |    },
        |    "likes": {
        |      "type": "array",
        |      "items": {
        |        "oneOf": [
        |          {
        |            "type": "object",
        |            "properties": {
        |              "__type": {
        |                "type": "string",
        |                "equal": "Orange"
        |              }
        |            },
        |            "required": [
        |              "__type"
        |            ]
        |          },
        |          {
        |            "type": "object",
        |            "properties": {
        |              "__type": {
        |                "type": "string",
        |                "equal": "Apple"
        |              }
        |            },
        |            "required": [
        |              "__type"
        |            ]
        |          }
        |        ]
        |      },
        |      "default": [
        |        {
        |          "__type": "Apple"
        |        }
        |      ]
        |    },
        |    "lastName": {
        |      "type": "string"
        |    },
        |    "firstName": {
        |      "type": "string"
        |    },
        |    "address": {
        |      "type": "object",
        |      "properties": {
        |        "streetAddress": {
        |          "type": "string"
        |        },
        |        "postalCode": {
        |          "type": "string"
        |        }
        |      },
        |      "required": [
        |        "streetAddress",
        |        "postalCode"
        |      ]
        |    },
        |    "gender": {
        |      "type": "string",
        |      "enum": [
        |        "Male",
        |        "Female"
        |      ],
        |      "default": "Male"
        |    }
        |  },
        |  "required": [
        |    "firstName",
        |    "lastName",
        |    "gender",
        |    "contactPreference"
        |  ]
        |}""".stripMargin
    ))
  }
}

object SchemaAlgebraSpec {
  sealed trait Gender extends EnumEntry

  object Gender extends PlayEnum[Gender] {
    val values = findValues

    case object Male extends Gender
    case object Female extends Gender

    implicit val schema = Schema.enum(this)
  }

  case class Address(streetAddress: String, postalCode: String)

  object Address {
    implicit val addressSchema: Program[SchemaAlgebra, Address] = Schema.from[Address]
  }

  sealed trait Fruit

  object Fruit {
    case object Apple extends Fruit
    case object Orange extends Fruit

    implicit val fruitSchema: Program[SchemaAlgebra, Fruit] = Schema.from[Fruit]
  }

  case class ContactPreference(message: Boolean, vocal: Boolean)

  object ContactPreference {
    implicit val contactPreferenceSchema: Program[SchemaAlgebra, ContactPreference] = Schema.from[ContactPreference]
  }

  sealed trait Contact

  object Contact {
    case class PhoneContact(phone: String) extends Contact
    case class EmailContact(email: String) extends Contact

    implicit val contactSchema: Program[SchemaAlgebra, Contact] = Schema.from[Contact]
  }

  case class Person(
    firstName: String,
    lastName: String,
    gender: Gender = Gender.Male,
    address: Option[Address],
    likes: List[Fruit] = List(Fruit.Apple),
    contactPreference: ContactPreference = ContactPreference(true, true),
    contacts: List[Contact] = List()
  )

  object Person {
    implicit val personSchema: Program[SchemaAlgebra, Person] = Schema.from[Person]
  }
}
