package com.osacky.flank.gradle

import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property

fun <T> StringBuilder.appendProperty(prop: Property<T>, name: String) {
  if (prop.isPresent) appendln("  $name: ${prop.get()}")
}

fun <T, K> StringBuilder.appendMapProperty(
  prop: MapProperty<T, K>,
  name: String,
  custom: StringBuilder.(Map.Entry<T, K>) -> Unit
) {
  if (prop.isPresentAndNotEmpty) {
    appendln("  $name:")
    prop.get().forEach { custom(it) }
  }
}

fun <T> StringBuilder.appendListProperty(
  prop: ListProperty<T>,
  name: String,
  custom: StringBuilder.(T) -> Unit
) {
  if (prop.isPresentAndNotEmpty) {
    appendln("  $name:")
    prop.get().forEach { custom(it) }
  }
}

fun StringBuilder.appendAdditionalProperty(property: Property<String>) {
  if (property.isPresent) {
    property.get()
      .split("\n")
      .map { "  $it" }
      .forEach { appendln(it) }
  }
}

val <T> ListProperty<T>.isPresentAndNotEmpty
  get() = isPresent && get().isNotEmpty()

val <T, K> MapProperty<T, K>.isPresentAndNotEmpty
  get() = isPresent && get().isNotEmpty()
