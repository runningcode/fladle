package com.osacky.flank.gradle

import org.gradle.internal.impldep.com.google.common.annotations.VisibleForTesting

internal class YamlWriter {

  internal fun createConfigProps(extension: FlankGradleExtension): String {
    val deviceString = createDeviceString(extension.devices)

    checkNotNull(extension.debugApk) { "debugApk cannot be null" }
    checkNotNull(extension.instrumentationApk) { "instrumentationApk cannot be null" }
    checkNotNull(extension.projectId) { "projectId cannot be null" }
    return """gcloud:
      |  app: ${extension.debugApk}
      |  test: ${extension.instrumentationApk}
      |  project: ${extension.projectId}
      |  use-orchestrator: ${extension.useOrchestrator}
      |  auto-google-login: ${extension.autoGoogleLogin}
      |$deviceString
    """.trimMargin()
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