package com.osacky.flank.gradle

import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property

fun <T : Any> StringBuilder.appendProperty(
  prop: Property<T>,
  name: String,
) {
  if (prop.isPresent) appendLine("  $name: ${prop.get()}")
}

fun <T : Any, K : Any> StringBuilder.appendMapProperty(
  prop: MapProperty<T, K>,
  name: String,
  custom: StringBuilder.(Map.Entry<T, K>) -> Unit,
) {
  if (prop.isPresentAndNotEmpty) {
    appendLine("  $name:")
    prop.get().forEach { custom(it) }
  }
}

fun <T : Any> StringBuilder.appendListProperty(
  prop: ListProperty<T>,
  name: String,
  custom: StringBuilder.(T) -> Unit,
) {
  if (prop.isPresentAndNotEmpty) {
    appendLine("  $name:")
    prop.get().forEach { custom(it) }
  }
}

fun StringBuilder.appendAdditionalProperty(property: Property<String>) {
  if (property.isPresent) {
    property
      .get()
      .split("\n")
      .map { "  $it" }
      .forEach { appendLine(it) }
  }
}

val <T : Any> ListProperty<T>.isPresentAndNotEmpty
  get() = isPresent && get().isNotEmpty()

val <T : Any, K : Any> MapProperty<T, K>.isPresentAndNotEmpty
  get() = isPresent && get().isNotEmpty()
