package com.osacky.flank.gradle

import org.gradle.api.GradleException
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property

@Throws(GradleException::class)
fun checkIfSanityAndValidateConfigs(config: FladleConfig) = when (config) {
  is FlankGradleExtension -> config.checkAndValidateExtension()
  is FladleConfigImpl -> config.checkAndValidateConfig()
  else -> throw GradleException("Unable to check for sanity, check config type")
}

private fun FlankGradleExtension.checkAndValidateExtension() {
  if (sanityRobo.getOrElse(false)) when {
    instrumentationApk.isNotPresentOrBlank -> throwBaseConfigError("instrumentationApk")
    additionalTestApks.isNotPresentOrEmpty -> throwBaseConfigError("additionalTestApks")
    roboDirectives.isNotPresentOrEmpty -> throwBaseConfigError("roboDirectives")
    roboScript.isNotPresentOrBlank -> throwBaseConfigError("roboScript")
  }
}

private fun FladleConfigImpl.checkAndValidateConfig() {
  if (sanityRobo.getOrElse(false)) when {
    roboDirectives.isNotPresentOrEmpty -> throwAdditionalConfigError("roboDirectives", name)
    roboScript.isNotPresentOrBlank -> throwAdditionalConfigError("roboScript", name)
    instrumentationApk.isNotPresentOrBlank -> throwAdditionalConfigError("instrumentationApk", name)
    additionalTestApks.isNotPresentOrEmpty -> throwAdditionalConfigError("additionalTestApks", name)
  }
}

private fun throwBaseConfigError(option: String): Nothing =
  throw GradleException("Incorrect [base] configuration. [$option] can't be used together with sanityRobo.")

private fun throwAdditionalConfigError(option: String, name: String): Nothing =
  throw GradleException(
    "Incorrect [$name] configuration. [$option] can't be used together with sanityRobo. " +
      "If you want to launch robo test run without robo script place only sanityRoboRun() into [$name] configuration"
  )

private val Property<String>.isNotPresentOrBlank
  get() = orNull.isNullOrBlank().not()

private val <T> ListProperty<T>.isNotPresentOrEmpty
  get() = getOrElse(emptyList()).isEmpty().not()
