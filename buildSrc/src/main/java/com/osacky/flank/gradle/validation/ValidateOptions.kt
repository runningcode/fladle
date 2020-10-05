package com.osacky.flank.gradle.validation

import com.osacky.flank.gradle.FladleConfig
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import kotlin.reflect.full.memberProperties

fun FladleConfig.validateOptionsUsed() = this::class.memberProperties
  .asSequence()
  .filter {
    when (val prop = it.call(this)) {
      is Property<*> -> prop.isPresent
      is MapProperty<*, *> -> prop.isPresent
      is ListProperty<*> -> prop.isPresent
      else -> false
    }
  }
  .mapNotNull { property -> properties[property.name]?.let { property to it } }
  .forEach { (property, version) ->
    val configFlankVersion = flankVersion.get().toVersion()
    if (version > configFlankVersion) throw IllegalStateException("Option ${property.name} is available since flank $version, which is higher than used $configFlankVersion")
  }

private data class FlankVersion(
  val year: Int,
  val month: Int,
  val minor: Int
) {
  operator fun compareTo(second: FlankVersion): Int {
    if (year > second.year) return 1
    if (month > second.month) return 1
    return if (minor > second.minor) 1
    else -1
  }

  override fun toString() = "$year.$month.$minor"
}

private fun String.toVersion(): FlankVersion {
  val numbers = split(".")
  return FlankVersion(Integer.valueOf(numbers[0]), Integer.valueOf(numbers[1]), Integer.valueOf(numbers[2]))
}

private val properties = FladleConfig::class.memberProperties
  .asSequence()
  .map { it to it.getter.annotations }
  .filter { it.second.any { annotation -> annotation is SinceFlank } }
  .map { it.first.name to it.second.filterIsInstance<SinceFlank>().first().version.toVersion() }
  .toMap()
