package com.osacky.flank.gradle

import org.gradle.internal.impldep.com.google.common.annotations.VisibleForTesting

internal class YamlWriter {

  internal fun createConfigProps(config: FladleConfig, base: FlankGradleExtension): String {
    if (base.projectId.orNull == null) {
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

    val deviceString = createDeviceString(config.devices.getOrElse(listOf(mapOf("model" to "NexusLowRes", "version" to "28"))))
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
    val testShards = config.testShards.orNull
    val shardTime = config.shardTime.orNull
    val repeatTests = config.repeatTests.orNull
    val smartFlankGcsPath = config.smartFlankGcsPath.orNull
    val filesToDownload = config.filesToDownload.getOrElse(emptyList())
    val projectId = config.projectId.orNull
    val runTimeout = config.runTimeout.orNull
    val ignoreFailedTests = config.ignoreFailedTests.getOrElse(false)
    val disableSharding = config.disableSharding.getOrElse(false)
    val smartFlankDisableUpload = config.smartFlankDisableUpload.getOrElse(false)
    val localResultsDir = config.localResultsDir.orNull
    val testTargetsAlwaysRun = config.testTargetsAlwaysRun.getOrElse(emptyList())

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
    appendln("  keep-file-path: ${config.keepFilePath.getOrElse(false)}")
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
    appendln("  use-orchestrator: ${config.useOrchestrator.getOrElse(false)}")
    appendln("  auto-google-login: ${config.autoGoogleLogin.getOrElse(false)}")
    appendln("  record-video: ${config.recordVideo.getOrElse(true)}")
    appendln("  performance-metrics: ${config.performanceMetrics.getOrElse(true)}")
    appendln("  timeout: ${config.testTimeout.getOrElse(15)}m")

    config.resultsHistoryName.orNull?.let {
      appendln("  results-history-name: $it")
    }
    config.resultsBucket.orNull?.let {
      appendln("  results-bucket: $it")
    }
    val environmentVariables = config.environmentVariables.getOrElse(emptyMap())
    if (environmentVariables.isNotEmpty()) {
      appendln("  environment-variables:")
      environmentVariables.forEach { key, value ->
        appendln("    $key: $value")
      }
    }
    val testTargets = config.testTargets.getOrElse(emptyList())
    if (testTargets.isNotEmpty()) {
      appendln("  test-targets:")
      testTargets.forEach { target ->
        appendln("  - $target")
      }
    }
    val directoriesToPull = config.directoriesToPull.getOrElse(emptyList())
    if (directoriesToPull.isNotEmpty()) {
      appendln("  directories-to-pull:")
      directoriesToPull.forEach { dir ->
        appendln("  - $dir")
      }
    }
    appendln(flakyTestAttemptsLine(config.flakyTestAttempts.getOrElse(0)))
    config.resultsDir.orNull?.let {
      appendln("  results-dir: $it")
    }

    config.testRunnerClass.orNull?.let {
      appendln("  test-runner-class: $it")
    }

    config.numUniformShards.orNull?.let {
      appendln("  num-uniform-shards: $it")
    }

    config.clientDetails.getOrElse(emptyMap()).also { details ->
      if (details.isNotEmpty()) {
        appendln("  client-details:")
        details.forEach { appendln("    ${it.key}: ${it.value}") }
      }
    }

    config.otherFiles.getOrElse(emptyMap()).also { files ->
      if (files.isNotEmpty()) {
        appendln("  other-files:")
        files.forEach { appendln("    ${it.key}: ${it.value}") }
      }
    }

    config.networkProfile.orNull?.let {
      appendln("  network-profile: $it")
    }

    config.roboScript.orNull?.let {
      appendln("  robo-script: $it")
    }

    config.roboDirectives.getOrElse(emptyList()).also { directives ->
      if (directives.isNotEmpty()) {
        appendln("  robo-directives:")
        directives.forEach {
          val value = it.getOrElse(2) { "" }.let { stringValue -> if (stringValue.isBlank()) "\"\"" else stringValue }
          appendln("    ${it[0]}:${it[1]}: $value")
        }
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
