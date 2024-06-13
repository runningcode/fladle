package com.osacky.flank.gradle

import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import java.lang.IllegalArgumentException

fun checkIfSanityAndValidateConfigs(config: FladleConfig) =
  when (config) {
    is FlankGradleExtension ->
      config.checkAndValidateConfig { option, name ->
        "Incorrect [$name] configuration. [$option] can't be used together with sanityRobo."
      }
    is FladleConfigImpl ->
      config.checkAndValidateConfig(config.name) { option, name ->
        "Incorrect [$name] configuration. [$option] can't be used together with sanityRobo. " +
          "To configure sanityRobo, add clearPropertiesForSanityRobo() to the [$name] configuration"
      }
    else -> throw IllegalArgumentException("Unexpected configuration when validating parameters. Did not expect: $config.")
  }

private fun FladleConfig.checkAndValidateConfig(
  name: String = "base",
  message: (String, String) -> String,
) {
  if (sanityRobo.get()) {
    when {
      roboDirectives.hasValue -> throw IllegalStateException(message("roboDirectives", name))
      roboScript.hasValue -> throw IllegalStateException(message("roboScript", name))
      instrumentationApk.hasValue -> throw IllegalStateException(message("instrumentationApk", name))
      additionalTestApks.hasValue -> throw IllegalStateException(message("additionalTestApks", name))
    }
  }
}

val Property<String>.hasValue
  get() = orNull.isNullOrBlank().not()

private val <T> ListProperty<T>.hasValue
  get() = getOrElse(emptyList()).isNotEmpty()
