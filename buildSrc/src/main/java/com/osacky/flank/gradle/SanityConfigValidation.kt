package com.osacky.flank.gradle

import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import java.lang.IllegalArgumentException

fun checkIfSanityAndValidateConfigs(config: FladleConfig) = when (config) {
  is FlankGradleExtension -> config.checkAndValidateConfig { option, name ->
    "Incorrect [$name] configuration. [$option] can't be used together with sanityRobo."
  }
  is FladleConfigImpl -> config.checkAndValidateConfig(config.name) { option, name ->
    "Incorrect [$name] configuration. [$option] can't be used together with sanityRobo. " +
      "To configure sanityRobo, add clearPropertiesForSanityRobo() to the [$name] configuration"
  }
  else -> throw IllegalArgumentException("Unexpected configuration when validating parameters. Did not expect: $config.")
}

private fun FladleConfig.checkAndValidateConfig(name: String = "base", message: (String, String) -> String) {
  if (sanityRobo.get()) when {
    roboDirectives.isNotPresentOrEmpty -> throw IllegalStateException(message("roboDirectives", name))
    roboScript.isNotPresentOrBlank -> throw IllegalStateException(message("roboScript", name))
    instrumentationApk.isNotPresentOrBlank -> throw IllegalStateException(message("instrumentationApk", name))
    additionalTestApks.isNotPresentOrEmpty -> throw IllegalStateException(message("additionalTestApks", name))
  }
}

val Property<String>.isNotPresentOrBlank
  get() = orNull.isNullOrBlank().not()

private val <T> ListProperty<T>.isNotPresentOrEmpty
  get() = getOrElse(emptyList()).isNotEmpty()
