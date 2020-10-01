package com.osacky.flank.gradle

import org.gradle.api.GradleException
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property

@Throws(GradleException::class)
fun checkIfSanityAndValidateConfigs(config: FladleConfig) = when (config) {
  is FlankGradleExtension -> config.checkAndValidateConfig() { option, _ ->
    "Incorrect [base] configuration. [$option] can't be used together with sanityRobo."
  }
  is FladleConfigImpl -> config.checkAndValidateConfig(config.name) { option, name ->
    "Incorrect [$name] configuration. [$option] can't be used together with sanityRobo. " +
      "If you want to launch robo test run without robo script place only clearPropertiesForSanityRobo() into [$name] configuration"
  }
  else -> throw GradleException("Unable to check for sanity, check config type")
}

private fun FladleConfig.checkAndValidateConfig(name: String = "base", message: (String, String) -> String) {
  if (sanityRobo.getOrElse(false)) when {
    roboDirectives.isNotPresentOrEmpty -> throw GradleException(message("roboDirectives", name))
    roboScript.isNotPresentOrBlank -> throw GradleException(message("roboScript", name))
    instrumentationApk.isNotPresentOrBlank -> throw GradleException(message("instrumentationApk", name))
    additionalTestApks.isNotPresentOrEmpty -> throw GradleException(message("additionalTestApks", name))
  }
}

private val Property<String>.isNotPresentOrBlank
  get() = orNull.isNullOrBlank().not()

private val <T> ListProperty<T>.isNotPresentOrEmpty
  get() = getOrElse(emptyList()).isEmpty().not()
