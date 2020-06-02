# restruct

Simply derive any format from any type.


## Use with sbt

```scala
libraryDependencies += "io.github.methrat0n" %% "restruct-all" % "2.0.0"
```

You can also choose to include only the parts you need.

```scala
libraryDependencies ++= Seq(
  "io.github.methrat0n" %% "restruct-core" % "2.0.0", //for only the internals, no format supported
  "io.github.methrat0n" %% "restruct-query-string-bindable" % "2.0.0", //for only the play query string format
  "io.github.methrat0n" %% "restruct-config-loader" % "2.0.0", //for only the play config format
  "io.github.methrat0n" %% "restruct-play-json" % "2.0.0", //for only play json Format, Writes and Reads
  "io.github.methrat0n" %% "restruct-play-json-reads" % "2.0.0", //for only play json Reads format
  "io.github.methrat0n" %% "restruct-play-json-writes" % "2.0.0", //for only play json Writes format

  "io.github.methrat0n" %% "restruct-enumeratum" % "2.0.0" //for enumeratum helper
)
```

# Motivation

You need an API with an endpoint which accepts bodies in `JSON` and `XML`. It always responds `YAML`. Without `restruct` you would have to use three different libraries, learn each of them and implement their own type classes or structures just to pass the data around.

That is what `restruct` is about: do not write structures for each format you need, write your own model along with its data schema. `restruct` will then _restructurates_ it for you.

This also has the advantages to bring a unified way of describing data for every format and unifing libraries interfaces, while still letting you access the specificity of your library of choice.

# Examples

#### Serializing a User to `JSON` using play-json
```scala

import io.github.methrat0n.restruct.schema.Path
import play.api.libs.json.Writes

//User definition
final case class User(name: String, age: Int)

object User {
  //User schema
  implicit val schema = (
    (Path \ "name").as[String]() and //Mark 'name' as String
    (Path \ "age").as[Int]() //Mark 'age' as Int
  ).inmap(User.apply _ tupled)(User.unapply _ andThen(_.get)) //User from and to (String, Int)*

  implicit val writes: Writes[User] = {
    import io.github.methrat0n.restruct.readers.json._ //Implicits to build the Writes instances
    schema.bind[Writes]
  }
}
```
\* The `inmap`call should not be necessary as soon as 2.1.0
<hr />

#### Reading `User` from query-string and writing `JSON`, in a [play application](TODO)

```scala

import io.github.methrat0n.restruct.schema.Path
import play.api.libs.json.Writes
import play.api.mvc.QueryStringBindable

//User definition
final case class User(name: String, age: Int)

object User {
  //User schema
  implicit val schema = (
    (Path \ "name").as[String]() and
    (Path \ "age").as[Int]()
  ).inmap(User.apply _ tupled)(User.unapply _ andThen(_.get))

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

# Usages

`restruct` see your types as structures (set of properties) with a name on top of it. Which means it's necessary to describe each property. A property is made of an access path (where to read or write the data) and a type.

```scala
// State that data is at the top property 'name'
(Path \ "name")
```

```scala
// State that the data is at 'structure' inside 'data' itself inside 'complex'.
(Path \ "complex" \ "data" \ "structure") 
```

As you see, it's possible to describe complexe access path to data. See [Paths](#Paths) for more details.

After pointing to the data, you need to tag it with a type.

```scala
// The data at 'name' is a String
(Path \ "name").as[String]()
```

```scala
// Option is a special case, you need to use the asOption function
(Path \ "name").asOption[String]()
```
_Note that the last parentheses couples is mandatory due to [Scala implicit limitations](#Why-the-last-parentheses?)_

Witch just that, you have stated that a property exist at some path. __This is enought to read or write.__ But restruct does not provide a `Reader` or `Writer` of any type, you are free to choose from [our supported libraries](). 

For the example, let's say you want to write `JSON` using [Play-Json]()

```scala
import io.github.methrat0n.restruct.writers.json._
import play.api.libs.json.Writes

val nameSchema = (Path \ "name").as[String]()
val nameWrites: Writes[String] = nameSchema.bind[Writes]

// Normal play-json from now on

val name = "Methrat0n"
nameWrites.writes(name) // Json.obj("name" -> "Methrat0n")
```

As you can see, we can derive a `Writes` from our `nameSchema`, therefore gaining the ability to write a `String` to a `JsValue`. This writing will follow the rules we gave, for now it's only the path.

_Note that we need the first import to be able to derive a `Writes`._

## case class (Product types)

Because `restruct` see types as set of properties with a name, it follow that you just have to describe all properties of a `class` and use the provided properties to build the actual `class`.

```scala
// Type is called 'User' and has
// two properies 'name' as String and 'age' an Int.
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

`nameAndAge` is the schema of two properties. This is near `User`, technicaly it's a `(String, Int)` schema. The last part is building the actual `User`:

```scala
... 
val userSchema = nameAndAge.inmap {
    case (name, age) => User(name, age)
  } {
    case User(name, age) => (name, age)
  }
...
```

`inmap` let us access the eventual value of the schema and return a new one, as long as we can do the opposite, see [Adding support for new types](). At the end `userSchema` describe a user, or more precisely where to find/put data to read/write about a `User`.

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

To follow our example, we can derive a `Writes` instance from `schema` which will write `JSON` as describe in `schema`.

```scala
//Json Writer
implicit val writes: Writes[User] = {
  import io.github.methrat0n.restruct.writers.json._
  schema.bind[Writes]
}
```

Or we could also derive a `ConfigLoader`, which allow to read from the typesafe config:

```scala
implicit val configLoader: ConfigLoader[User] = {
  import io.github.methrat0n.restruct.readers.configLoader._
  schema.bind[ConfigLoader]
}

```

## sealed trait (Coproduct types)

Sealed trait are essentialy unions. In this new example, an `Animal` is essentialy just a `Cat` or a `Dog`.

```scala
sealed trait Animal

final case class Cat(numberOfLife: Int) extends Animal
final case class Dog(name: String) extends Animal
```

Because we can reduce an `Animal` to just that, `Cat` __or__ `Dog`, that's exactly how `restruct` support sealed trait:

```scala

sealed trait Animal

object Animal {
  implicit val schema = (Cat.schema or Dog.schema).inmap {
    case Right(badUser) => badUser
    case Left(goodUser) => goodUser
  } {
    case badUser: BadUser   => Right(badUser)
    case goodUser: GoodUser => Left(goodUser)
  }
}

// Assuming Cat.schema and Dog.schema to be define as seen before
...
```

## Complexe Path
Path let you point `restruct` to your data. There is mostly two type of step inside a path: either it's an `Int` or it's a `String`. The fist case correspond to an index inside some sequence, for example an `Array`, a `Seq` or any other type for which a number can be considered a unique key to some value (yes, a `Map<Int, T>` can work). The second case is simpler, it's the name of the field you want to actualy point to. For example, inside a `JSON`, object are made of field which are named.

This two kind of steps can be mixed together freely to describe where to read or write.

#### Just a single `String` step

```scala
(Path \ "name")
```

Most simple path, it prescribe to read or write data inside the "name" field.

Working `JSON` example:
```json
{
  "name": "Methrat0n"
}
```
Working query-string example:
```
name="Methrat0n"
```

<hr />

#### Just a single `Int` step

```scala
(Path \ 0)
```

`Int` step mean to read at 'index' 0. Most of the time this is link to arrays, but specific format may change this meaning.

Working `JSON` example:
```json
[ "Methrat0n" ]
```

<hr />

#### Mutliple `String` steps

```scala
(Path \ "names" \ "firstname")
```

Now we do not just read a field value, we go deeper, inside the value itself.

Working `JSON` example:
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

This is a tricky case, multiple `Int` steps most of the times means imbricated arrays.

Working `JSON` example:
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

That look like a real world example, we select the first element of the value inside the field "firstnames" which itself is inside the field "names".

Working `JSON` example:
```json
{
  "names": {
    "firstnames": ["Wismerhill", "Methrat0n"]
  }
}
```

### Not all format support all path

As we've seen, `Int` and `String` steps are for indexed structure. But Not all format aren't indexed by `Int` __and__ `String`, sometimes it's just one of them. We've also said that path can be compose of multiple steps, but why should all format support that? Maybe it just support three levels, maybe just one ?
How can we be sure ?

An example is the query-string. Simple query-string does not support composite fields and cannot be an array. Which means the path ` (Path \ 0) ` or `(Path \ "names" \ "firstname")` would not work. Actualy, you can only pass one `String` step for this format.
So, do you have to remember it? How does it work in this cases?

`restruct` was redesign partly for this problem and since `2.0.0` it simply does __not__ work. To be clearer, if you try to bind a schema which contains path (or other features, see [Compile time errors]()) it simply does not compile because of a missing implicits.

Let's see an example:
```scala
final case class User(name: String, age: Int)

object User {
  implicit val schema = (
    //Note the two level path
    (Path \ "names" \ "firstname").as[String]() and
    (Path \ "age").as[Int]()
  ).inmap {
    case (name, age) => User(name, age)
  } {
    case User(name, age) => (name, age)
  }
}
```

This example compile fine by itslef. It define a schema for `User`, as long it's syntaxicly correct, nothing can be wrong.

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

You can see the compile error is very specific, depending on the format we want to derive from our schema.

_Note that even if your IDE may complain about a missing implicit, the compiler error clearly point you to a possible path error._

### Format interpret Path

Most of the times, `Int` step will means the data is store inside an array and, most of the time, `String` steps will point to fields of some kind of object.
But that's just for "most of the time" cases. Because you may have cases where you need to read by lexicographical order, or maybe you want to write to the reverse path that is actually written ? Because we all have weird and very personal use cases.

`restruct-core` only define how to write a path, it does not make any assumtion on how this path should actually be used when reading or writing data. Basically this was to allow each format to be independent on how they handle there path, if they want to. As an end user, you can still change how your paths are interpreted by defining local implicits. If you are a library writer and want to learn how to implement a new format for `restruct`, see [Contributing]().

We'll take one example on how to override path interpretation.

#### Play-Json

By default, play-json `Reads` will support any path. In the other hand, `Writes` does not accept `Int` steps. Let's say you actually want to support `Int` steps, you decide your way of doing it: `Int` step will not be put inside an array but instead a `0` will become `"0"` and serve as field name. That means:
```scala
(Path \ "names" \ "firstnames" \ 1)
```
will be the same as writing
```scala
(Path \ "names" \ "firstnames" \ "1")
```
We will not judge you, you must have a very good reason. You can achieve it by looking inside the "restruct-play-json-writes" package. You will see that the path is implicitly handle by the type class `WritesPathBuilder`. Only `String` case exist by default, but you can create the `Int` one.

```scala
implicit def intStep2JsPath[RemainingPath <: Path](implicit remainingPath: WritesPathBuilder[RemainingPath]) = new WritesPathBuilder[RemainingPath \ Int] {
  override def toJsPath(path: RemainingPath \ Int): JsPath = JsPath(remainingPath.toJsPath(path.previousSteps).path :+ KeyPathNode(path.step.toString))
}
```

That's no simple code, but it will suffice to define it once and import it below `restruct`.

## Compile time errors
_This is an explanation on the current design of `restruct`, you do not need this to use the library_

`restruct-2.0.0` was re-design to answer a problem: how can we avoid having to `throw` in each format that does not support all `restruct` features ? The most visible case, but not the only one, was about the path. We want to be able to describe complexe paths, but the more complexe the path syntax would become, the less format would totaly support it. At the end, each format will have "holes" into it's implementation that each user would have to know before using `restruct`.
That's unacceptable, and yet, pretty logical. The more general we will try to be, the more each case will not be able to follow all the rules. That's pretty basic logic.
After many tries, the final answser was to add a "compilation-phase" to the library. `restruct` define it's own syntax, it's own DSL, in a word it's own language and it's pretty common in computer science to check against syntax error during the compilation. But yet, we aren't writing a new language, just a scala library, so how? The answer was the same as always in scala: __implicits__.
Implicits can be considered proofs. If the languages does find an implcits it means it was able to build it, therefore the meaning we put in that implicit is true for the current case. Realising so and implementing it was another story, but we did it by spliting our tagless final pattern in a hierarchy of smaller type classes and therefore encoding into the type-level of our schema the needed features (interpreters) for it. When binding a schema to a format, the type level encoded interpreter is implicitly requested for this format. If the format has all the definitions needed to build it, it means it support all needed features for the schema, if not it just means a features is missing and the schema cannot be bind to this format.

This new design allow us to "elevate" runtime error to compilation errors, therefore detecting incompatibility beteween a schema and a format sooner. Most importantly, users do __not__ have to know each "holes" in the implementation, the compiler will tell them.

## Writing your own format implementation

`restruct` is made of small modules. Each one of them has the responsability to support one format. A format may just Read or just Write data, or it may do both. In `restruct` all modules depends on `restruct-core` that provide the type class hierarchy of interpreters for libraries and the syntax for users.
If you want to add the support for another format, you can create a new module, add the dependency to `restruct-core` and begin writing your implementation of the interpreters. If you need help, we will be happy to answer any questions. Don't be afraid and post yours in the issue tracker.

To help you, here is a basic explanation on how to do it:
 - Decide on which library/format you whant to add
 - Does it read ? write ? both ?
   - If both is the answser, can you split the read and write part or is it made of a whole ? To help you, play-json is made of multiple type (Reads, Writes and Format) on the other side QueryStringBindalbe is one lonely type.
   - You should create a module for each format, one for the reader, one for the writer and one for the handler. If possible.
 - Place the new format inside the right package: io.github.methrat0n.restruct.<readers | writers | handlers>
 - Create an object inside a file of the name of your format, then implement all the interpreters you can inside it.
- Write a little documentation about what is supported and what isn't. Even if the compiler will tell them, your users will appreciate a little explanation.

## Adding support for new types

## Constraining types

# FAQ

### Why only 2.13 ?

### Should I wrote the schema types ?

### Why the last parentheses?

# What's next ?

## Restore all v1 features (macro, no inamp for case class definitions, default value support)

## Adding more formats (XML, BSON, JSON-Schema ...)

## Adding more complexe examples

# List of supported libraries