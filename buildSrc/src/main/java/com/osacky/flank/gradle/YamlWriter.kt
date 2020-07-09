package com.osacky.flank.gradle

import org.gradle.internal.impldep.com.google.common.annotations.VisibleForTesting

internal class YamlWriter {

  internal fun createConfigProps(config: FladleConfig, base: FlankGradleExtension): String {
    if (base.projectId == null) {
      check(base.serviceAccountCredentials.isPresent) { "ServiceAccountCredentials in fladle extension not set. https://github.com/runningcode/fladle#serviceaccountcredentials" }
    }
    check(base.debugApk.isPresent) { "debugApk must be specified" }
    check(base.instrumentationApk.isPresent xor !base.roboScript.isNullOrBlank()) {
      """
     Either instrumentationApk file or roboScript file must be specified but not both.
     instrumentationApk=${base.instrumentationApk.orNull}
     roboScript=${base.roboScript}
      """.trimIndent()
    }

    val deviceString = createDeviceString(config.devices)
    val additionalProperties = writeAdditionalProperties(config)
    val flankProperties = writeFlankProperties(config)

    return buildString {
      appendln("gcloud:")
      appendln("  app: ${base.debugApk.get()}")
      if (base.instrumentationApk.isPresent) {
        appendln("  test: ${base.instrumentationApk.get()}")
      }
      appendln(deviceString)
      appendln(additionalProperties)
      append(flankProperties)
    }
  }

  internal fun writeFlankProperties(config: FladleConfig): String = buildString {
    val testShards = config.testShards
    val shardTime = config.shardTime
    val repeatTests = config.repeatTests
    val smartFlankGcsPath = config.smartFlankGcsPath
    val filesToDownload = config.filesToDownload
    val projectId = config.projectId
    val runTimeout = config.runTimeout.orNull
    val ignoreFailedTests = config.ignoreFailedTests.getOrElse(false)
    val disableSharding = config.disableSharding
    val smartFlankDisableUpload = config.smartFlankDisableUpload
    val localResultsDir = config.localResultsDir.orNull
    val testTargetsAlwaysRun = config.testTargetsAlwaysRun

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
    val additionalTestApks = config.additionalTestApks.getOrElse(emptyList())

    if (additionalTestApks.isNotEmpty()) {
      appendln("  additional-app-test-apks:")
      additionalTestApks.forEach {
        appendln("    $it")
      }
    }

    runTimeout?.let {
      appendln("  run-timeout: $it")
    }
    appendln("  ignore-failed-tests: $ignoreFailedTests")
    appendln("  disable-sharding: $disableSharding")
    appendln("  smart-flank-disable-upload: $smartFlankDisableUpload")
    localResultsDir?.let {
      appendln("  local-result-dir: $localResultsDir")
    }
    if (testTargetsAlwaysRun.isNotEmpty()) {
      appendln("  test-targets-always-run:")
      testTargetsAlwaysRun.forEach {
        appendln("  - class $it")
      }
    }

    appendln("  output-style: ${config.outputStyle.get()}")
  }

  internal fun writeAdditionalProperties(config: FladleConfig): String = buildString {
    appendln("  use-orchestrator: ${config.useOrchestrator}")
    appendln("  auto-google-login: ${config.autoGoogleLogin}")
    appendln("  record-video: ${config.recordVideo}")
    appendln("  performance-metrics: ${config.performanceMetrics}")
    appendln("  timeout: ${config.testTimeout}")

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

    config.testRunnerClass?.let {
      appendln("  test-runner-class: $it")
    }

    config.numUniformShards?.let {
      appendln("  num-uniform-shards: $it")
    }

    if (config.clientDetails.isNotEmpty()) {
      appendln("  client-details:")
      config.clientDetails.forEach {
        appendln("    ${it.key}: ${it.value}")
      }
    }

    if (config.otherFiles.isNotEmpty()) {
      appendln("  other-files:")
      config.otherFiles.forEach {
        appendln("    ${it.key}: ${it.value}")
      }
    }

    config.networkProfile?.let {
      appendln("  network-profile: $it")
    }

    config.roboScript?.let {
      appendln("  robo-script: $it")
    }

    if (config.roboDirectives.isNotEmpty()) {
      appendln("  robo-directives:")
      config.roboDirectives.forEach {
        val value = it.getOrElse(2) { "" }.let { stringValue -> if (stringValue.isBlank()) "\"\"" else stringValue }
        appendln("    ${it[0]}:${it[1]}: $value")
      }
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
