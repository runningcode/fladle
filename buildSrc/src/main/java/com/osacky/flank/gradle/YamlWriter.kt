package com.osacky.flank.gradle

import org.gradle.internal.impldep.com.google.common.annotations.VisibleForTesting

internal class YamlWriter {

  internal fun createConfigProps(config: FladleConfig, base: FlankGradleExtension): String {
    val deviceString = createDeviceString(config.devices)
    val additionalProperties = writeAdditionalProperties(config)
    val flankProperties = writeFlankProperties(config)

    checkNotNull(base.debugApk) { "debugApk cannot be null" }
    checkNotNull(base.instrumentationApk) { "instrumentationApk cannot be null" }
    return """gcloud:
      |  app: ${base.debugApk}
      |  test: ${base.instrumentationApk}
      |$deviceString
      |$additionalProperties
      |$flankProperties
    """.trimMargin()
  }

  internal fun writeFlankProperties(config: FladleConfig): String = buildString {
    val testShards = config.testShards
    val repeatTests = config.repeatTests
    val smartFlankGcsPath = config.smartFlankGcsPath
    val filesToDownload = config.filesToDownload
    val projectId = config.projectId
    if (testShards != null || repeatTests != null || smartFlankGcsPath != null || filesToDownload.isNotEmpty() || projectId != null) {
      appendln("flank:")
    }
    testShards?.let {
      appendln("  max-test-shards: $testShards")
    }
    repeatTests?.let {
      appendln("  repeat-tests: $repeatTests")
    }
    smartFlankGcsPath?.let {
      appendln("  smart-flank-gcs-path: $it")
    }
    projectId?.let {
      appendln("    project: $it")
    }
    if (filesToDownload.isNotEmpty()) {
      appendln("  files-to-download:")
      filesToDownload.forEach { file ->
        appendln("  - $file")
      }
    }
  }

  internal fun writeAdditionalProperties(config: FladleConfig): String = buildString {
    appendln("  use-orchestrator: ${config.useOrchestrator}")
    appendln("  auto-google-login: ${config.autoGoogleLogin}")
    appendln("  record-video: ${config.recordVideo}")
    appendln("  performance-metrics: ${config.performanceMetrics}")
    appendln("  timeout: ${config.timeoutMin}m")

    config.resultsHistoryName?.let {
      appendln("  results-history-name: $it")
    }
    val environmentVariables = config.environmentVariables
    if (environmentVariables.isNotEmpty()) {
      appendln("  environment-variables:")
      environmentVariables.forEach { key, value ->
        appendln("    $key: $value")
      }
    }
    val testTargets = config.testTargets
    if (testTargets.isNotEmpty()) {
      appendln("  test-targets:")
      testTargets.forEach { target ->
        appendln("  - $target")
      }
    }
    val directoriesToPull = config.directoriesToPull
    if (directoriesToPull.isNotEmpty()) {
      appendln("  directories-to-pull:")
      directoriesToPull.forEach { dir ->
        appendln("  - $dir")
      }
    }
    appendln("  flaky-test-attempts: ${config.flakyTestAttempts}")
  }

  @VisibleForTesting
  internal fun createDeviceString(devices: List<Device>): String = buildString {
    appendln("  device:")
    for (device in devices) {
      appendln("  - model: ${device.model}")
      appendln("    version: ${device.version}")
      device.orientation?.let {
        appendln("    orientation: $it")
      }
      device.locale?.let {
        appendln("    locale: $it")
      }
    }
  }
}