package com.osacky.flank.gradle

import org.gradle.internal.impldep.com.google.common.annotations.VisibleForTesting

internal class YamlWriter {

  internal fun createConfigProps(config: FladleConfig, extension: FlankGradleExtension): String {
    val deviceString = createDeviceString(config.devices)
    val additionalProperties = writeAdditionalProperties(config)
    val flankProperties = writeFlankProperties(config)

    checkNotNull(extension.debugApk) { "debugApk cannot be null" }
    checkNotNull(extension.instrumentationApk) { "instrumentationApk cannot be null" }
    return """gcloud:
      |  app: ${extension.debugApk}
      |  test: ${extension.instrumentationApk}
      |  use-orchestrator: ${config.useOrchestrator}
      |  auto-google-login: ${config.autoGoogleLogin}
      |${createProjectIdString(config)}
      |$deviceString
      |$additionalProperties
      |$flankProperties
    """.trimMargin()
  }

  internal fun writeFlankProperties(extension: FladleConfig): String {
    val builder = StringBuilder()
    val testShards = extension.testShards
    val repeatTests = extension.repeatTests
    if (testShards != null || repeatTests != null) {
      builder.appendln("flank:")
    }
    if (testShards != null) {
      builder.appendln("  testShards: $testShards")
    }
    if (repeatTests != null) {
      builder.appendln("  repeatTests: $repeatTests")
    }
    return builder.toString()
  }

  internal fun writeAdditionalProperties(extension: FladleConfig): String {
    val builder = StringBuilder()
    val testTargets = extension.testTargets
    if (testTargets.isNotEmpty()) {
      builder.appendln("  test-targets:")
      testTargets.forEach { target ->
        builder.appendln("  - $target")
      }
    }

    return builder.toString()
  }

  internal fun createProjectIdString(extension: FladleConfig): String {
    return if (extension.projectId != null) {
      "  project: ${extension.projectId}"
    } else {
      "# projectId will be automatically discovered"
    }
  }

  @VisibleForTesting
  internal fun createDeviceString(devices: List<Device>): String {
    val builder = StringBuilder("  device:\n")
    for (device in devices) {
      builder.append("  - model: ${device.model}\n")
      builder.append("    version: ${device.version}\n")
      if (device.orientation != null) {
        builder.append("    orientation: ${device.orientation}\n")
      }
      if (device.locale != null) {
        builder.append("    locale: ${device.version}\n")
      }
    }
    return builder.toString()
  }
}