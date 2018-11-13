package io.github.methrat0n.restruct.writers

import play.api.libs.json.{ JsArray, JsObject, Writes }

import scala.collection.immutable.ListMap

package object jsonschema {
  type JsonSchemaWriter[A] = (JsObject, Writes[A])

  private[jsonschema] def deepMerge(current: JsObject, other: JsObject): JsObject = {
      def merge(existingObject: JsObject, otherObject: JsObject): JsObject = {
        val result = otherObject.fields.foldLeft(ListMap(existingObject.fields: _*)) {
          case (acc, (otherKey, otherValue)) =>
            val maybeExistingValue = existingObject.value.get(otherKey)

            val newValue = (maybeExistingValue, otherValue) match {
              case (Some(e: JsObject), o: JsObject) => merge(e, o)
              case (Some(e: JsArray), o: JsArray)   => e ++ o
              case _                                => otherValue
            }
            acc + (otherKey -> newValue)
        }

        JsObject(result)
      }
    merge(current, other)
  }
}
