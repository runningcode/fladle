package com.osacky.flank.gradle

import org.gradle.internal.impldep.com.google.common.annotations.VisibleForTesting

internal class YamlWriter {

  internal fun createConfigProps(extension: FlankGradleExtension): String {
    val deviceString = createDeviceString(extension.devices)
    val additionalProperties = writeAdditionalProperties(extension)
    val flankProperties = writeFlankProperties(extension)

    checkNotNull(extension.debugApk) { "debugApk cannot be null" }
    checkNotNull(extension.instrumentationApk) { "instrumentationApk cannot be null" }
    return """gcloud:
      |  app: ${extension.debugApk}
      |  test: ${extension.instrumentationApk}
      |  use-orchestrator: ${extension.useOrchestrator}
      |  auto-google-login: ${extension.autoGoogleLogin}
      |${createProjectIdString(extension)}
      |$deviceString
      |$additionalProperties
      |$flankProperties
    """.trimMargin()
  }

  internal fun writeFlankProperties(extension: FlankGradleExtension): String {
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

  internal fun writeAdditionalProperties(extension: FlankGradleExtension): String {
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

  internal fun createProjectIdString(extension: FlankGradleExtension): String {
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