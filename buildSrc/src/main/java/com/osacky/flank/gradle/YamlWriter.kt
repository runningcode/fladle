package com.osacky.flank.gradle

import org.gradle.internal.impldep.com.google.common.annotations.VisibleForTesting

internal class YamlWriter {

  internal fun createConfigProps(config: FladleConfig, base: FlankGradleExtension): String {
    if (base.projectId == null) {
      check(base.serviceAccountCredentials.isPresent) { "ServiceAccountCredentials in fladle extension not set. https://github.com/runningcode/fladle#serviceaccountcredentials" }
    }
    check(base.debugApk.isPresent) { "debugApk must be specified" }
    check(base.instrumentationApk.isPresent) { "instrumentationApk must be specified" }

    val deviceString = createDeviceString(config.devices)
    val additionalProperties = writeAdditionalProperties(config)
    val flankProperties = writeFlankProperties(config)
    return """gcloud:
      |  app: ${base.debugApk.get()}
      |  test: ${base.instrumentationApk.get()}
      |$deviceString
      |$additionalProperties
      |$flankProperties
    """.trimMargin()
  }

  internal fun writeFlankProperties(config: FladleConfig): String = buildString {
    val testShards = config.testShards
    val shardTime = config.shardTime
    val repeatTests = config.repeatTests
    val smartFlankGcsPath = config.smartFlankGcsPath
    val filesToDownload = config.filesToDownload
    val projectId = config.projectId

    appendln("flank:")

    testShards?.let {
      appendln("  max-test-shards: $testShards")
    }
    shardTime?.let {
      appendln("  shard-time: $shardTime")
    }
    repeatTests?.let {
      appendln(repeatTestsLine(repeatTests))
    }
    smartFlankGcsPath?.let {
      appendln("  smart-flank-gcs-path: $it")
    }
    projectId?.let {
      appendln("  project: $it")
    }
    appendln("  keep-file-path: ${config.keepFilePath}")
    if (filesToDownload.isNotEmpty()) {
      appendln("  files-to-download:")
      filesToDownload.forEach { file ->
        appendln("  - $file")
      }
    }
    val testApks = config.additionalTestApks.flatMap { (debugApk, instrumentationApks) ->
      instrumentationApks.map { debugApk to it }
    }

    if (testApks.isNotEmpty()) {
      appendln("  additional-app-test-apks:")
      testApks.forEach {
        appendln("    - app: ${it.first}")
        appendln("      test: ${it.second}")
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
    config.resultsBucket?.let {
      appendln("  results-bucket: $it")
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
    appendln(flakyTestAttemptsLine(config.flakyTestAttempts))
    config.resultsDir?.let {
      appendln("  results-dir: $it")
    }
  }

  private fun flakyTestAttemptsLine(flakyTestAttempts: Int): String {
    val label = "num-flaky-test-attempts"
    return "  $label: $flakyTestAttempts"
  }

  private fun repeatTestsLine(repeatTests: Int): String {
    val label = "num-test-runs"
    return "  $label: $repeatTests"
  }

  @VisibleForTesting
  internal fun createDeviceString(devices: List<Map<String, String>>): String = buildString {
    appendln("  device:")
    for (device in devices) {
      if (device["model"] == null) throw RequiredDeviceKeyMissingException("model")
      val model = device["model"]
      if (device["version"] == null) throw RequiredDeviceKeyMissingException("version")
      val version = device["version"]
      val orientation = device["orientation"]
      val locale = device["locale"]
      appendln("  - model: $model")
      appendln("    version: $version")
      orientation?.let {
        appendln("    orientation: $it")
      }
      locale?.let {
        appendln("    locale: $it")
      }
    }
  }
}
