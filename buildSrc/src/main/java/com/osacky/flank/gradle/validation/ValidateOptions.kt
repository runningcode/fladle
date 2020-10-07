package com.osacky.flank.gradle.validation

import com.osacky.flank.gradle.FladleConfig
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.util.VersionNumber
import kotlin.reflect.full.memberProperties

fun validateOptionsUsed(config: FladleConfig, flank: String) = config::class.memberProperties
  .asSequence()
  .filter {
    when (val prop = it.call(config)) {
      is Property<*> -> prop.isPresent
      is MapProperty<*, *> -> prop.isPresent
      is ListProperty<*> -> prop.isPresent
      else -> false
    }
  }
  .mapNotNull { property -> properties[property.name]?.let { property to it } }
  .forEach { (property, version) ->
    val configFlankVersion = flank.toVersion()
    if (version > configFlankVersion) throw IllegalStateException("Option ${property.name} is available since flank $version, which is higher than used $configFlankVersion")
  }

private data class FlankVersion(
  val year: Int,
  val month: Int,
  val minor: Int = 0
) {
  operator fun compareTo(second: FlankVersion) = when {
    year > second.year -> 1
    year == second.year && month > second.month -> 1
    year == second.year && month == second.month && minor > second.minor -> 1
    minor == second.minor -> 0
    else -> -1
  }

  override fun toString() = "$year.${if (month < 10) "0$month" else month}.$minor"
}

private fun String.toVersion() = VersionNumber.parse(this)

private val properties = FladleConfig::class.memberProperties
  .asSequence()
  .map { it to it.getter.annotations }
  .filter { it.second.any { annotation -> annotation is SinceFlank } }
  .map { it.first.name to it.second.filterIsInstance<SinceFlank>().first().version.toVersion() }
  .toMap()
