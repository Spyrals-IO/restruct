# Restruct

Restruct let you describe your types in a unyfied syntax and derived any format from this very same syntax.

Warning : This library is still in beta. Tests are not yet fully written and error may be unclear.

### Case Class

First, define a field schema. Choose a name and paired it with its type :
```scala
import io.github.methrat0n.restruct.schema.Schema
import io.github.methrat0n.restruct.schema.Syntax._
  
val usernameFieldSchema: Schema[String] = "username".as[String]
```

For optional field a specific function need to be called :
```scala
import io.github.methrat0n.restruct.schema.Schema
import io.github.methrat0n.restruct.schema.Syntax._
  
val optionalUsernameFieldSchema: Schema[Option[String]] = "username".asOption[String]
```

The `as` and `asOption` functions needs an implicit value of type `Schema[T]`. In our example, we need a `Schema[String]`.
All defaults schema are provided by the `io.github.methrat0n.restruct.schema.Syntax._` import.

Then combine different fields to build the case class schema.

```scala
final case class User(username: String, age: Int, bankAccount: Option[String])

object User {
  import io.github.methrat0n.restruct.schema.Schema
  import io.github.methrat0n.restruct.schema.Syntax._
  
  implicit lazy val schema: Schema[User] = 
    "username".as[String] and
    "age".as[Int] and
    "bankAccount".asOption[String]      
}
```

The fields combined with `and` need to match the case class signature.
In our example, any `Schema` other than (`String` and `Int` and `Option[String]`) would have failed.

With this schema, it's possible to derive any supported format.

````scala
import io.github.methrat0n.restruct.readers.config.configLoader
implicit lazy val schema: Schema[User] = ...
implicit lazy val configLoader: ConfigLoader[User] = schema.bind(configLoader)
````

We ask for a [ConfigLoader](https://www.playframework.com/documentation/2.6.x/ScalaConfig#configloader).
In [Play Applications](https://www.playframework.com/), this implicit would allow to read our case class from the configuration.


### Sealed trait

Using the same syntax as before, a sealed trait can be describe

````scala
import io.github.methrat0n.restruct.schema.Schema
import io.github.methrat0n.restruct.schema.Syntax._

sealed trait Person

object Person {
  final case class User(...) extends Person
  object User {
    implicit lazy val schema: Schema[User] = ???
  }
  final case class Citizen(...) extends Person
  object Citizen {
    implicit lazy val schema: Schema[Citizen] = ???
  }
  
  implicit lazy val schema: Schema[Person] =
    User.schema or Citizen.schema
}

````
`or` let us combine two schema into one.
The `personSchema` can be infered if all its children are combined together and only its children.

### Path

It's possible to specify where to read / write your data.
Instead of just giving a name to your fields, give them a full path.

This path can be build from `String` and `Int`. `String`s will be interpreted as object names in the structure and `Int`s as array indexes.

````scala
import io.github.methrat0n.restruct.schema.Schema
import io.github.methrat0n.restruct.schema.Syntax._

implicit lazy val schema: Schema[User] = 
  "bodies" \ 0 \ "username".as[String] and
  "bodies" \ 0 \ "age".as[Int] and
  "bodies" \ 0 \ "bankAccount".asOption[String]  
}
````

Our username will now be read from the top array named `bodies`. At index 0 should be an object, in which we select the field "username".
If we read our user from a json string, a matching example would be :

```json
{
  "bodies": [
    {
      "username": "kevin",
      "age": 12,
      "bankAccount": "0xCCC220JZOCNI"
    }
  ]
}
```

Note that some format does not support this feature, see the limitation parts.

### Default value

You can add a default value to your fields.

````scala
import io.github.methrat0n.restruct.schema.Schema
import io.github.methrat0n.restruct.schema.Syntax._

implicit lazy val schema: Schema[User] = 
  "username".as[String].defaultTo("kevin") and
  "age".as[Int] and
  "bankAccount".asOption[String]  
````
The default value must be of the same type as the field. Following, for optional field, an `Option[T]` must be passed.
The default value will only ever be used if the field cant be found in data.

### Constraints

Constraints can be placed onto your field.
This is typicaly usefull for validation, but can also be used to describe more clearly an interface contract.

````scala
import io.github.methrat0n.restruct.schema.Schema
import io.github.methrat0n.restruct.schema.Syntax._

implicit lazy val schema: Schema[User] = 
  "username".as[String].constraintedBy(Constraints.EqualConstraint("kevin")) and
  "age".as[Int] and
  "bankAccount".asOption[String]  
````

By passing a constraint onto a field we state "this field should always equals 'kevin' ".
Constraint can be pass at every levels: on fields, on simple schema or complex one.

```scala
import io.github.methrat0n.restruct.schema.Schema
import io.github.methrat0n.restruct.schema.Syntax._
import io.github.methrat0n.restruct.core.data.constraints.Constraints

val kevinSchema: Schema[String] = string.constraintedBy(Constraints.EqualConstraint("kevin"))
val userWithAccountSchema: Schema[User] = User.schema.constraintedBy(UserConstraints.WithAccount)
```

The first line define a schema for string wihch only allow "kevin" as valid value.
The second line use a fake UserConstraints package to create a schema for User.
This schema will be in error if the bankAccount property is None.

### Without implicit conversion

Three implicit conversions exist in the syntax. The first two transform a String or an Int to a Path. Allowing the _as_, `asOption` and `\` syntax.
If you prefer, it's also possible to prefix your Path with a `Path \`

```scala
import io.github.methrat0n.restruct.schema.Syntax._

val ageAsInt = "age".as[Int]
```

become

```scala
import io.github.methrat0n.restruct.schema.Syntax._
import io.github.methrat0n.restruct.core.data.schema.Path

val ageAsInt = (Path \ "age").as[Int]
```

The third implicit conversion is on the Schema construction itself. When you mix schemas using the _and_ function it does not
build a Schema[YourType] if build a composite Schema of tuples. To transform the first into the last, the Schema _apply_ function is called implicitly.

```scala
import io.github.methrat0n.restruct.schema.Schema
import io.github.methrat0n.restruct.schema.Syntax._

implicit lazy val schema: Schema[User] = 
  "username".as[String] and
  "age".as[Int] and
  "bankAccount".asOption[String]  
```

become 

````scala
import io.github.methrat0n.restruct.schema.Schema
import io.github.methrat0n.restruct.schema.Syntax._

implicit lazy val schema: Schema[User] = Schema(
  "username".as[String] and
  "age".as[Int] and
  "bankAccount".asOption[String]
)
````

### Strict Schema
When working with sealed traits, there can be a matching problem.
If a trait have multiple childrens with the same type signature, it's impossible to differenciate them.
To match the right type, you need a StrictSchema.

````scala
import io.github.methrat0n.restruct.schema.Schema
import io.github.methrat0n.restruct.schema.Syntax._

implicit lazy val schema: Schema[User] = StrictSchema(
  "username".as[String] and
  "age".as[Int] and
  "bankAccount".asOption[String]
)
````

This schema will add a `__type` field into your structure, which will hold your type's name.
This could also be used to serialize your type's name, in case of meaningful ADT or enumeration.

### Helpful macro
A schema can be derived from your class or sealed trait directly by calling macros.

```scala
import io.github.methrat0n.restruct.schema.Schema

implicit lazy val schema: Schema[User] = Schema.of[User]
implicit lazy val strictSchema: Schema[User] = StrictSchema.of[User]
```

It will write a schema based on your type informations.
Which mean neither path syntax nor fields constraints will be available.

### Create a Schema from an existing one
In case some schema is needed but is not provided by the syntax, a new one can be build easily.
```scala
import io.github.methrat0n.restruct.schema.Schema
import io.github.methrat0n.restruct.schema.Syntax.list

implicit def arraySchema[T](implicit schema: Schema[T]): Schema[Array[T]] =
  list[T](schema).inmap(_.toArray)(_.toList)
```
Here a `Schema[Array[T]]` is defined from the default list Schema. The `inmap` function is defined in `Schema` and can be used
to obtains a new `Schema` from an existing one.

#### Using Restruct
Restruct is still in beta and tests aren't fully written. Nevertherless, the last version of the library is 0.1.0 and is
compatible with scala and ScalaJs 2.12

If you are using sbt add the following to your build:
```sbt
libraryDependencies ++= Seq(
  "io.github.methrat0n" %% "restruct-all" % "0.1.0", //for all the supported formats
  "io.github.methrat0n" %% "restruct-core" % "0.1.0", //for only the internals, no format supported
  "io.github.methrat0n" %% "restruct-query-string-bindable" % "0.1.0", //for only the play query string format
  "io.github.methrat0n" %% "restruct-config-loader" % "0.1.0", //for only the play config format
  "io.github.methrat0n" %% "restruct-json-schema" % "0.1.0", //for only a jsonSchema writer
  "io.github.methrat0n" %% "restruct-play-json" % "0.1.0", //for only play json Format, Writes and Reads
  "io.github.methrat0n" %% "restruct-play-json-reads" % "0.1.0", //for only play json Reads format
  "io.github.methrat0n" %% "restruct-play-json-writes" % "0.1.0", //for only play json Writes format
  "io.github.methrat0n" %% "restruct-bson" % "0.1.0", //for only reactive-mongo BSONHandler, BSONWriter and BSONReader
  "io.github.methrat0n" %% "restruct-bson-writer" % "0.1.0", //for only reactive-mongo BSONWriter
  "io.github.methrat0n" %% "restruct-bson-reader" % "0.1.0", //for only reactive-mongo BSONReader
  
  "io.github.methrat0n" %% "restruct-enumeratum" % "0.1.0" //for enumeratum helper
)
```

For Scala.js just replace %% with %%% above.

Instructions for Maven and other build tools are available at [search.maven.org.](https://search.maven.org/search?q=g:io.github.methrat0n)

### Limitations

#### Query-String-Bindable
The path syntax is not supported for query-string as querystrings contains only one layer.
If a schema with path is bind to the queryStringBindable object a RuntimeException will be raised (this exception should be more specific with time).


### Known issues

#### Macro derivation cant find default value
Currently, the schemas derived using macro will not contains default values even if they are present in the corresponding case class.
