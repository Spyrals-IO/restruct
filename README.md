# Restruct

Write your data model once, derive any data format from it.

## Use with sbt

```scala
libraryDependencies += "io.github.methrat0n" %% "restruct-all" % "2.2.0"
```

You can also choose to include only the parts you need.

```scala
libraryDependencies ++= Seq(
  "io.github.methrat0n" %% "restruct-core" % "2.2.0", //for only the internals, no format supported
  "io.github.methrat0n" %% "restruct-query-string-bindable" % "2.2.0", //for only the play query string format
  "io.github.methrat0n" %% "restruct-config-loader" % "2.2.0", //for only the play config format
  "io.github.methrat0n" %% "restruct-play-json" % "2.2.0", //for only play json Format, Writes and Reads
  "io.github.methrat0n" %% "restruct-play-json-reads" % "2.2.0", //for only play json Reads format
  "io.github.methrat0n" %% "restruct-play-json-writes" % "2.2.0", //for only play json Writes format

  "io.github.methrat0n" %% "restruct-enumeratum" % "2.2.0" //for enumeratum helper
)
```

## Motivation

 - You need an API with an endpoint which accepts bodies in `JSON` and `XML`.
It always responds `YAML`.
 - You take JSON file from the disk and need to save it into some database.
 - You compose multiple API and input using different input and need to store / transform the data.

Without `Restruct` you would have to use multiple different libraries, learn each one of
them and implement their own type classes or structures just to pass the data around.

That is what `Restruct` is about: do not write structures for each format you need,
write your own model along with its data schema. `Restruct` will then _restructurates_ it
for you.

This also has the advantages to bring a unified way of describing data for every format
and unifying libraries interfaces, while still letting you access the specificity of your
library of choice.

## Examples

#### Serializing a User to `JSON` using play-json
```scala

import io.github.methrat0n.restruct.schema.Path
import play.api.libs.json.Writes

//User definition
final case class User(name: String, age: Int)

object User {
  //User schema
  implicit val schema = Schema[User](
    (Path \ "name").as[String]() and //Mark 'name' as String
    (Path \ "age").as[Int]() //Mark 'age' as Int
  )

  implicit val writes: Writes[User] = {
    import io.github.methrat0n.restruct.readers.json._ //Implicits to build the Writes instances
    schema.bind[Writes]
  }
}
```

#### Reading `User` from query-string and writing `JSON`, in [play](https://www.playframework.com/)

```scala

import io.github.methrat0n.restruct.schema.Path
import play.api.libs.json.Writes
import play.api.mvc.QueryStringBindable

//User definition
final case class User(name: String, age: Int)

object User {
  //User schema
  implicit val schema = Schema[User](
    (Path \ "name").as[String]() and
    (Path \ "age").as[Int]()
  )

  //Json Writer
  implicit val writes: Writes[User] = {
    import io.github.methrat0n.restruct.writers.json._
    schema.bind[Writes]
  }

  //Query-string ReaderWriter
  implicit val queryStringBindable: QueryStringBindable[User] = {
    import io.github.methrat0n.restruct.handlers.queryStringBindable._
    schema.bind[QueryStringBindable]
  }
}
```

## Usages

`Restruct` see your types as structures (set of properties) with a name on top of it.
Which means it's necessary to describe each property.
A property is made of an access path (where to read or write the data) and a type.

```scala
// State that data is at the top property 'name'
(Path \ "name")
```

```scala
// State that the data is at 'structure' inside 'data' itself inside 'complex'.
(Path \ "complex" \ "data" \ "structure") 
```

As you see, you can describe complex access path.
See [Complex Path](#Complex-Path) for more details.

After pointing to the data, you need to tag it with some type.

```scala
// The data at 'name' is a String
(Path \ "name").as[String]()
```

```scala
// Option is a special case, you need to use the asOption function
(Path \ "name").asOption[String]()
```
_Note that the last parentheses couples is mandatory due
to Scala implicit limitations_

With just that, you have stated that a property exist at some path.
__This is enough to read or write.__ You are free to choose readers or writers
from [our supported libraries](#List-of-supported-libraries). 

Let's say you want to write `JSON` using
[Play-Json](https://github.com/playframework/play-json)

```scala
import io.github.methrat0n.restruct.writers.json._
import play.api.libs.json.Writes

val nameSchema = (Path \ "name").as[String]()
val nameWrites: Writes[String] = nameSchema.bind[Writes]

// Normal play-json from now on

val name = "Methrat0n"
nameWrites.writes(name) // Json.obj("name" -> "Methrat0n")
```

As you can see, we can derive a `Writes` from our `nameSchema`,
therefore we can write a `String` to a `JsValue`.
This writing will follow the rules we gave, only the path for now.

_Note that we need the first import to be able to derive a `Writes`._

## case class (Product types)

Because `Restruct` see types as set of properties with a name,
it follows that you just have to describe all properties of a `class` and use
the provided properties to build the actual `class`.

```scala
// Type is called 'User' and has
// two properties 'name' which is a String and 'age', an Int.
final case class User(name: String, age: Int)

//Companion object, because the schema is an implicit specific to User
object User {
  //build the two properties
  val nameProperty = (Path \ "name").as[String]()
  val ageProperty = (Path \ "age").as[Int]()
}
```

Having the two properties, we can combine them:

```scala
...
val nameAndAge = nameProperty and ageProperty
... 
```

`nameAndAge` is the schema of two properties. This is near `User`,
technically it's a `(String, Int)` schema.
The last part is building the actual `User`:

```scala
... 
val userSchema = nameAndAge.inmap {
    case (name, age) => User(name, age)
  } {
    case User(name, age) => (name, age)
  }
...
```

`inmap` let us access the eventual value of the schema and return a new one,
as long as we can do the opposite.
At the end `userSchema` describe a user, or more precisely where to
read/write data to obtain a `User`.

Putting it all together:

```scala
final case class User(name: String, age: Int)

//Companion object, because the schema is an implicit specific to User
object User {
  //User schema
  implicit val schema = (
    (Path \ "name").as[String]() and
    (Path \ "age").as[Int]()
  ).inmap {
    case (name, age) => User(name, age)
  } {
    case User(name, age) => (name, age)
  }
}
```

And we can take advantage of a macro to avoid writing
the inmap for product and coproduct types :

```scala
final case class User(name: String, age: Int)

object User {
  implicit val schema = Schema[User](
    (Path \ "name").as[String]() and
    (Path \ "age").as[Int]()
  )
}
```

To follow our example, we can derive a `Writes` instance from `schema`
which will write `JSON` as we describe it in `schema`.

```scala
//Json Writer
implicit val writes: Writes[User] = {
  import io.github.methrat0n.restruct.writers.json._
  schema.bind[Writes]
}
```

Or we could derive a `ConfigLoader`, which allow us to
read case class from the typesafe config:

```scala
implicit val configLoader: ConfigLoader[User] = {
  import io.github.methrat0n.restruct.readers.configLoader._
  schema.bind[ConfigLoader]
}

```

## Sealed traits (Coproduct types)

Sealed traits are essentially unions.
In this new example, an `Animal` is essentially just a `Cat` or a `Dog`.

```scala
sealed trait Animal

final case class Cat(numberOfLife: Int) extends Animal
final case class Dog(name: String) extends Animal
```

Because we can reduce an `Animal` to just that, `Cat` __or__ `Dog`,
that's how `Restruct` support sealed trait:

```scala

sealed trait Animal

object Animal {
  implicit val schema = Schema[Animal](Cat.schema or Dog.schema)
}

// Assuming Cat.schema and Dog.schema to be define as seen before
```

## Macros
Writing all the above by hand is awesome and very powerful as you can customize a lot of
your schemas. Still, it can become cumbersome at time, especially when you just need the minimum
schema for you type, therefore we made a macro to automate this.

```scala
...
implicit val schema = Schema.of[User]

//Equivalent to

implicit val schema = Schema[User](
  (Path \ "name").as[String]() and
  (Path \ "age").as[Int]()
)

...

implicit val schema = Schema.of[Animal]

//Equivalent to

implicit val schema = Schema[Animal](Cat.schema or Dog.schema)
```

## Complex Path
Paths let you point `Restruct` to your data.
There is mostly two types of steps inside a path: either it's an `Int` or it's a `String`.
The fist case, `Int` correspond to an index inside some sequence,
for example an `Array`, a `Seq` or any other type for which a number can be considered
a unique key to some value (yes, a `Map<Int, T>` will work).
The second case, `String`, is simpler : it's the name of the field you want to actually
point to. For example, inside a `JSON`, objects are made of fields which are named.

This two kinds of steps can be mixed together freely to describe where to read or write.

#### Just a single `String` step

```scala
(Path \ "name")
```

The Simplest path, it prescribes to read or write data inside the `name` field.

Corresponding `JSON` example:
```json
{
  "name": "Methrat0n"
}
```
Corresponding query-string example:
```
name="Methrat0n"
```

<hr />

#### Just a single `Int` step

```scala
(Path \ 0)
```

`Int` step mean to read at "index" 0.
Most of the time this is link to arrays,
but specific format may change this meaning.

Corresponding `JSON` example:
```json
[ "Methrat0n" ]
```

<hr />

#### Mutliple `String` steps

```scala
(Path \ "names" \ "firstname")
```

Going deeper, inside the field to grab the value we need.

Corresponding `JSON` example:
```json
{
  "names": {
    "firstname": "Methrat0n"
  }
}
```

<hr />

#### Mutliple `Int` steps

```scala
(Path \ 0 \ 1)
```

Multiple `Int` steps, most of the time, means imbricated arrays.

Corresponding `JSON` example:
```json
[
  ["Wismerhill", "Methrat0n"]
]
```

<hr />

#### Mixing it all together

```scala
(Path \ "names" \ "firstnames" \ 1)
```

Select the first element of the field `firstnames`, itself inside the `names` field.

Corresponding `JSON` example:
```json
{
  "names": {
    "firstnames": ["Wismerhill", "Methrat0n"]
  }
}
```

### Not all formats support all paths

As we've seen, `Int` and `String` steps are for indexed structure,
but not all formats support index of type `Int` __and__ `String`,
sometimes just one of them.
We've also said that paths can be composed of multiple steps,
but why should all formats support that?
Maybe it just supports three levels, maybe just one ?
How can we be sure ?

Query-strings do not support composite fields and cant be arrays.
Which means that ` (Path \ 0) ` or `(Path \ "names" \ "firstname")` cannot work.
Actually, you can only pass one `String` step for query-strings.

`Restruct` was redesign partly for this problem and since `2.0.0`
it simply does __not__ compile.
If you try to bind a schema which contains paths
(or other features, see [Compile time errors](#Compile-time-errors))
it simply does not compile because of missing implicits.

Let's see an example:
```scala
final case class User(name: String, age: Int)

object User {
  implicit val schema = Schema[User](
    //Note the two level path
    (Path \ "names" \ "firstname").as[String]() and
    (Path \ "age").as[Int]()
  )
}
```

This example compile fine by itself.
It defines a schema for `User`, as long it's syntactically correct, nothing can be wrong.

```scala

// Play-json Writes. Support String step to any deps, compile fine
implicit val writes: Writes[User] = {
  import io.github.methrat0n.restruct.writers.json._
  schema.bind[Writes]
}
// QueryStringBindable. Does not compile, complain about a missing implicits in IDE
implicit val queryStringBindable: QueryStringBindable[User] = {
  import io.github.methrat0n.restruct.handlers.queryStringBindable._
  schema.bind[QueryStringBindable]
}
```

As you can see, compile errors are very format specific.

_Note that even if your IDE may complain about a missing implicit,
the compiler error clearly point to a possible path error._

### Path Override

Most of the time, `Int` steps will mean the data is store inside an array and,
most of the time, `String` steps will point to fields of some kind of object.
But that's just for "most of the time" cases.
You may have cases where you need to read by lexicographical order,
or maybe you want to write to the reverse path that is actually written ?
Because we all have weird and very personal use cases.

`restruct-core` only define how to write a path,
it does not make any assumptions on how this path should actually be
used when reading or writing data.
It allows each format to be independent on how they handle their paths.
As an end user, you can still change how your paths are interpreted
by defining local implicits.
If you are a library writer and want to learn how to implement a
new format for `Restruct`, see [Writing your own format implementation](#Writing-your-own-format-implementation).

We'll take one example on how to override path interpretation.

#### Play-Json

By default, play-json `Reads` will support any path.
In other hands, `Writes` does not accept `Int` steps.
Let's say you actually want to support `Int` steps, you decide your way of doing it:
`Int` step will not be put inside an array but instead a `0` will become `"0"`
and serve as field name. That means:
```scala
(Path \ "names" \ "firstnames" \ 1)
```
will be equivalent to
```scala
(Path \ "names" \ "firstnames" \ "1")
```
We will not judge you, you must have a very good reason.
You can achieve it by looking inside the "restruct-play-json-writes"
package. You will see that the path is implicitly handle by the type
class `WritesPathBuilder`. Only `String` case exist by default, but you can
create the `Int` one.

```scala
implicit def intStep2JsPath[RemainingPath <: Path](implicit remainingPath: WritesPathBuilder[RemainingPath]) = new WritesPathBuilder[RemainingPath \ Int] {
  override def toJsPath(path: RemainingPath \ Int): JsPath = JsPath(remainingPath.toJsPath(path.previousSteps).path :+ KeyPathNode(path.step.toString))
}
```

That's no simple code, but it will suffice to define it once and import it below `Restruct`.

### Compile time errors
_This is an explanation on the current design of `Restruct`,
you do not need this to use the library_

`Restruct 2.0` was re-design to answer a problem: how can we avoid having to `throw` in
each format that does not support all `Restruct` features ?
The most visible case, but not the only one, was about the path.
We want to be able to describe complex paths, but the more complexity in the path syntax,
the fewer formats would be able to support it completely.
At the end, each format will have "holes" into it's implementation
that each end user would have to know before using `Restruct`.
That's terrible user experience and, after many tries, the final answer
was to add a "compilation-phase" to the library.
We did it by realising that _implicits are proofs_. If the compiler
does find an implicit it's only because it was able to build it.
Therefore, by requesting an implicit which respect our model we are
basically asking the compiler if it can prove that a given schema is
compatible with a given format.

This new design allow us to "elevate" runtime error to compilation errors,
therefore detecting incompatibility between a schema and a format sooner.
Most importantly, users do __not__ have to know each "holes" in the implementation,
the compiler will tell them.

### Writing your own format implementation

`Restruct` is made of small modules. Each one of them has the
responsibility to support one format.
A format may just Read or just Write data, or it may do both.
In `Restruct` all modules depends on `restruct-core` which provide
the type class hierarchy of interpreters and the end syntax.
If you want to add support for another format, you can create a new module,
add the dependency to `restruct-core` and begin writing an implementation of
the interpreters.
If you need help, we will be happy to answer any questions. Don't be afraid
and post yours in the issue tracker.

To help you, here is a basic explanation on how to do it:
 - Decide on which library/format you want to add
 - Does it read ? write ? both ?
   - If both is the answer, can you split the read and write part or
     is it made of a whole ?
     To help you, play-json is made of multiple types (Reads, Writes and Format)
     on the other side QueryStringBindable is one lonely type.
   - You should create a module for each format, one for the reader,
     one for the writer and one for the handler. If possible.
 - Place the new format inside the right package: 
   io.github.methrat0n.restruct.<readers | writers | handlers>
 - Create an object named after your schema, then implement all
   the interpreters you can inside it.
- Write a little documentation about what is supported and what isn't.
  Even if the compiler will tell them, your users will appreciate some explanations.

### Constraining types

will return soon

## List of supported formats or libraries:
 - [play-json](https://github.com/playframework/play-json) with `restruct-play-json`
 - [enumeratum](https://github.com/lloydmeta/enumeratum) with `restruct-enumeratum`
 - [ConfigLoader](https://www.playframework.com/documentation/2.6.x/ScalaConfig#configloader) with `restruct-config-loader`
 - [QueryStringBindable](https://www.playframework.com/documentation/2.7.x/api/scala/play/api/mvc/QueryStringBindable.html) with `restruct-query-string-bindable`
 
