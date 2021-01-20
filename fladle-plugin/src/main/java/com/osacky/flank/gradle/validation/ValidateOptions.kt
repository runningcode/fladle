package com.osacky.flank.gradle.validation

import com.osacky.flank.gradle.FladleConfig
import org.gradle.util.VersionNumber
import kotlin.reflect.full.memberProperties

fun validateOptionsUsed(config: FladleConfig, flank: String) = config
  .getPresentProperties()
  .mapNotNull { property -> properties[property.name]?.let { property to it } }
  .forEach { (property, version) ->
    val configFlankVersion = flank.toVersion()
    if (version > configFlankVersion) throw IllegalStateException("Option ${property.name} is available since flank $version, which is higher than used $configFlankVersion")
  }

private fun String.toVersion() = VersionNumber.parse(this)

private val properties = FladleConfig::class.memberProperties
  .asSequence()
  .map { it to it.getter.annotations }
  // we also need to exclude properties with default values to preserve backward compatibility
  // to be fixed
  .filter { it.second.any { annotation -> annotation is SinceFlank && !annotation.hasDefaultValue } }
  .map { it.first.name to it.second.filterIsInstance<SinceFlank>().first().version.toVersion() }
  .toMap()
