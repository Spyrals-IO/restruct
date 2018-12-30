# Restruct

Restruct let you describe your types in a unyfied syntax and derived any format from this very same syntax.

Warning : This library is still in beta version. Tests are not yet fully written and error may be unclear.

### Case Class

Using the unified syntax provided by restruct it's possible to describe a type very simply.

````scala
final case class User(username: String, age: Int, bankAccount: Option[String])

object User {
  import io.github.methrat0n.restruct.schema.Schema
  import io.github.methrat0n.restruct.schema.Syntax._
  implicit lazy val schema: Schema[User] = 
    "username".as[String] and
    "age".as[Int] and
    "bankAccount".asOption[String]      
}
````
The first string in each line is the name we want to use for our field. 
Note that we used the same names for clarity but any valid string would do.
The as function let you specify the type of this field.
Note that for options syntax is slightly different.
Finaly the and function will accumulate your fields declarations

Compilation errors can be raised here. The as and asOption function need an implicit
value of type Schema[T]. In our example, we need a Schema[String] and another Schema[Int].
The string schema will be pass to the first line in the schema declaration and in the third.

Like the syntax itself, all defaults schema are provided by the Syntax import.

The second compilation error is when the schema build here does not match the case class signature.
In our example, any Schema different than String and Int and Option[String] would raise.
Note that the order of the type is important.

With this schema, it's possible to derive any supported format.

````scala
import io.github.methrat0n.restruct.readers.config.configLoader
implicit lazy val schema: Schema[User] = ...
implicit lazy val configLoader: ConfigLoader[User] = schema.bind(configLoader)
````
We ask for a [ConfigLoader](https://www.playframework.com/documentation/2.6.x/ScalaConfig#configloader).
In a [Play](https://www.playframework.com/) application, this implicit would allow to read our case class from the configuration.


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
Here we combine two schema into one.
The or function state that a Person instance will be either a User instance or a Citizen instance.

This will raise a compilation error if the schema used to build the Person schema are not his direct childrens.
In the same manner, another error will be raised if all the childrens schema aren't mixed together.

### Path

It's possible to specify where you want to read / write your data.
Instead of just giving a name to your fields, give them a full path.

This path can be build from String and Int. Strings will be interpret as object name in the structure and Int as Array index.

````scala
import io.github.methrat0n.restruct.schema.Schema
import io.github.methrat0n.restruct.schema.Syntax._

implicit lazy val schema: Schema[User] = 
  "bodies" \ 0 \ "username".as[String] and
  "bodies" \ 0 \ "age".as[Int] and
  "bodies" \ 0 \ "bankAccount".asOption[String]  
}
````

Our username will now be read from the top array named "bodies". At his index 0, which should be an object. In this object we select the field "username".
If we would read our user from a json string, this would be a matching example :

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
The default value must be of the same type as the field. Here we use a String in a string field.
This means that for Optional field, an option must be pass.
The default value will only ever be used if the field cant be found in data.

### Constraints

As for default value, constraints can also be placed onto your field.
This is typicly used for validation, but can also be used to describe more clearly an interface contract.

````scala
import io.github.methrat0n.restruct.schema.Schema
import io.github.methrat0n.restruct.schema.Syntax._

implicit lazy val schema: Schema[User] = 
  "username".as[String].constraintedBy(Constraints.EqualConstraint("kevin")) and
  "age".as[Int] and
  "bankAccount".asOption[String]  
````

By passing a constraint to constraintedBy onto a field I state that this particular field should always equals "kevin".
The constraintedBy function need a Constraint[T] with T being the type of the field. In our example, I use a string constraint.

Constraint can be pass at every levels: on fields, on simple schema or complex one.
```scala
import io.github.methrat0n.restruct.schema.Schema
import io.github.methrat0n.restruct.schema.Syntax._
import io.github.methrat0n.restruct.core.data.constraints.Constraints

val kevinSchema: Schema[String] = string.constraintedBy(Constraints.EqualConstraint("kevin"))
val userWithAccountSchema: Schema[User] = User.schema.constraintedBy(UserConstraints.WithAccount)
```

The first line define a schema for string wich only allow this string to be "kevin".
The second line use a fake UserConstraints package to create a schema for User.
This schema will be in error if the bankAccount property is None.

### Without implicit conversion

Three implicit conversions exist in the syntax. The first two transform a String or an Int to a Path. Allowing the _as_, _asOption_ and _\\_ syntax.
You can avoid it if you want by prefixing your Path with a _Path \\_

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

The third implicit conversion is on the Schema construction itself. When you mix schema using the _and_ function it does not
build a Schema[YourType] if build a composite Schema of tuples. To transform the first into the last, the Schema _apply_ function is called implicitly.
You can call it yourself if you prefer.

````scala
import io.github.methrat0n.restruct.schema.Schema
import io.github.methrat0n.restruct.schema.Syntax._

implicit lazy val schema: Schema[User] = 
  "username".as[String] and
  "age".as[Int] and
  "bankAccount".asOption[String]  
````

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
When working with sealed trait, a matching problem can arise.
If a trait have multiple childrens with the same type signature, it impossible to know which one to choose.

If you use the simple declaration you will always get the last schema you mix in.
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

This could also be used to encode the name of your type, if case of meaningful ADT or enumeration.

### Helpful macro
A schema can be derived from your class or sealed trait directly by calling the macro.

```scala
import io.github.methrat0n.restruct.schema.Schema

implicit lazy val schema: Schema[User] = Schema.of[User]
implicit lazy val strictSchema: Schema[User] = StrictSchema.of[User]
```

The macro will write a schema from your type information.
Which mean neither path syntax to change the structure of your data nor constraints to limit the scope of your types.


### Create a Schema from an existing one
In case some implicit schema is needed but it's not provided by the syntax, a new one can be build easily.
```scala
import io.github.methrat0n.restruct.schema.Schema
import io.github.methrat0n.restruct.schema.Syntax.list

implicit def arraySchema[T](implicit schema: Schema[T]): Schema[Array[T]] = list[T](schema).inmap(_.toArray)(_.toList)
```
Here a Schema[Array[T]] is defined from the default list Schema. The inmap function is defined in Schema and can be used
to obtains a new Schema from an existing one.

#### Using Restruct

Restruct is still in beta and tests aren't fully written. Nevertherless, the last version of the library is 0.1.0 and is
compatible with scala and ScalaJs 2.12

If you are using sbt add the following to your build:
```sbtshell
libraryDependencies ++= Seq(
  "io.github.methrat0n" %% "restruct-all" % "0.1.0", //for all the supported formats
  "io.github.methrat0n" %% "restruct-core" % "0.1.0", //for only the internal, no format supported
  "io.github.methrat0n" %% "restruct-query-string-bindable" % "0.1.0", //for only the play query string format
  "io.github.methrat0n" %% "restruct-config-loader" % "0.1.0", //for only the play config format
  "io.github.methrat0n" %% "restruct-json-schema" % "0.1.0", //for only a jsonSchema writer
  "io.github.methrat0n" %% "restruct-play-json" % "0.1.0", //for only play json Format, Writes and Reads
  "io.github.methrat0n" %% "restruct-play-json-reads" % "0.1.0", //for only play json Reads format
  "io.github.methrat0n" %% "restruct-play-json-writes" % "0.1.0", //for only play json Writes format
  "io.github.methrat0n" %% "restruct-bson" % "0.1.0", //for only reactive-mongo BSONHandler, BSONWriter and BSONReader
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


### Know issues

#### Macro derivation cant find default value
For know, at least, the macro derived schemas will not contains default values even if they are present in the corresponding case class.