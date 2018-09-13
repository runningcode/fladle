package com.osacky.flank.gradle

import org.gradle.internal.impldep.com.google.common.annotations.VisibleForTesting

internal class YamlWriter {

  internal fun createConfigProps(extension: FlankGradleExtension): String {
    val deviceString = createDeviceString(extension.devices)

    checkNotNull(extension.debugApk) { "debugApk cannot be null" }
    checkNotNull(extension.instrumentationApk) { "instrumentationApk cannot be null" }
    return """gcloud:
      |  app: ${extension.debugApk}
      |  test: ${extension.instrumentationApk}
      |  use-orchestrator: ${extension.useOrchestrator}
      |  auto-google-login: ${extension.autoGoogleLogin}
      |${createProjectIdString(extension)}
      |$deviceString
    """.trimMargin()
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