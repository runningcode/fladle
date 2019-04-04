package com.osacky.flank.gradle

import org.gradle.internal.impldep.com.google.common.annotations.VisibleForTesting

internal class YamlWriter {

  internal fun createConfigProps(config: FladleConfig, base: FlankGradleExtension): String {
    checkNotNull(base.serviceAccountCredentials) { "ServiceAccountCredentials in fladle extension not set. https://github.com/runningcode/fladle#serviceaccountcredentials" }
    checkNotNull(base.debugApk) { "debugApk cannot be null" }
    checkNotNull(base.instrumentationApk) { "instrumentationApk cannot be null" }

    val deviceString = createDeviceString(config.devices)
    val additionalProperties = writeAdditionalProperties(config)
    val flankProperties = writeFlankProperties(config)
    return """gcloud:
      |  app: ${base.debugApk}
      |  test: ${base.instrumentationApk}
      |${createProjectIdString(config)}
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
    if (testShards != null || repeatTests != null || smartFlankGcsPath != null || filesToDownload.isNotEmpty()) {
      appendln("flank:")
    }
    testShards?.let {
      appendln("  testShards: $testShards")
    }
    repeatTests?.let {
      appendln("  repeatTests: $repeatTests")
    }
    smartFlankGcsPath?.let {
      appendln("  smartFlankGcsPath: $it")
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

  internal fun createProjectIdString(config: FladleConfig): String {
    return if (config.projectId != null) {
      "  project: ${config.projectId}"
    } else {
      "# projectId will be automatically discovered"
    }
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