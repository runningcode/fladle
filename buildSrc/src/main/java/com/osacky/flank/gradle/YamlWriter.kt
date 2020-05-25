package com.osacky.flank.gradle

import org.gradle.internal.impldep.com.google.common.annotations.VisibleForTesting

internal class YamlWriter {

  internal fun createConfigProps(config: FladleConfig, base: FlankGradleExtension): String {
    if (base.projectId.isPresent.not()) {
      check(base.serviceAccountCredentials.isPresent) { "ServiceAccountCredentials in fladle extension not set. https://github.com/runningcode/fladle#serviceaccountcredentials" }
    }
    check(base.debugApk.isPresent) { "debugApk must be specified" }
    check(base.instrumentationApk.isPresent xor base.roboScript.isPresent) { """
     Either instrumentationApk file or roboScript file must be specified but not both.
     instrumentationApk=${base.instrumentationApk.orNull}
     roboScript=${base.roboScript.orNull}
    """.trimIndent() }

    val deviceString = createDeviceString(config.devices.get())
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
    appendln("flank:")
    // without default values
    val testShards = config.testShards
    val shardTime = config.shardTime
    val repeatTests = config.repeatTests
    val smartFlankGcsPath = config.smartFlankGcsPath
    val projectId = config.projectId
    val runTimeout = config.runTimeout
    val localResultsDir = config.localResultsDir

    if (testShards.isPresent) appendln("  max-test-shards: ${testShards.get()}")
    if (shardTime.isPresent) appendln("  shard-time: ${shardTime.get()}")
    if (repeatTests.isPresent) appendln(repeatTestsLine(repeatTests.get()))
    if (smartFlankGcsPath.isPresent) appendln("  smart-flank-gcs-path: ${smartFlankGcsPath.get()}")
    if (projectId.isPresent) appendln("  project: ${projectId.get()}")
    if (runTimeout.isPresent) appendln("  run-timeout: ${runTimeout.get()}")
    if (localResultsDir.isPresent) appendln("  local-result-dir: ${localResultsDir.get()}")

    // with default values
    val filesToDownload = config.filesToDownload.get()
    val ignoreFailedTests = config.ignoreFailedTests.get()
    val disableSharding = config.disableSharding.get()
    val smartFlankDisableUpload = config.smartFlankDisableUpload.get()
    val testTargetsAlwaysRun = config.testTargetsAlwaysRun.get()
    val keepFilePath = config.keepFilePath.get()
    val additionalTestApks = config.additionalTestApks.get()

    appendln("  keep-file-path: $keepFilePath")

    if (filesToDownload.isNotEmpty()) {
      appendln("  files-to-download:")
      filesToDownload.forEach { file ->
        appendln("  - $file")
      }
    }

    if (additionalTestApks.isNotEmpty()) {
      appendln("  additional-app-test-apks:")
      additionalTestApks.forEach {
        appendln("    $it")
      }
    }

    appendln("  ignore-failed-tests: $ignoreFailedTests")
    appendln("  disable-sharding: $disableSharding")
    appendln("  smart-flank-disable-upload: $smartFlankDisableUpload")
    if (testTargetsAlwaysRun.isNotEmpty()) {
      appendln("  test-targets-always-run:")
      testTargetsAlwaysRun.forEach {
        appendln("  - class $it")
      }
    }

    appendln("  output-style: ${config.outputStyle.get()}")
  }

  internal fun writeAdditionalProperties(config: FladleConfig): String = buildString {
    // without default values
    val resultsHistoryName = config.resultsHistoryName
    val resultsBucket = config.resultsBucket
    val resultsDir = config.resultsDir
    val testRunnerClass = config.testRunnerClass
    val numUniformShards = config.numUniformShards
    val networkProfile = config.networkProfile
    val roboScript = config.roboScript

    if (resultsHistoryName.isPresent) appendln("  results-history-name: ${resultsHistoryName.get()}")
    if (resultsBucket.isPresent) appendln("  results-bucket: ${resultsBucket.get()}")
    if (resultsDir.isPresent) appendln("  results-dir: ${resultsDir.get()}")
    if (testRunnerClass.isPresent) appendln("  test-runner-class: ${testRunnerClass.get()}")
    if (numUniformShards.isPresent) appendln("  num-uniform-shards: ${numUniformShards.get()}")
    if (networkProfile.isPresent) appendln("  network-profile: ${networkProfile.get()}")
    if (roboScript.isPresent) appendln("  robo-script: ${roboScript.get()}")

    // with default values
    val useOrchestrator = config.useOrchestrator.get()
    val autoGoogleLogin = config.autoGoogleLogin.get()
    val recordVideo = config.recordVideo.get()
    val performanceMetrics = config.performanceMetrics.get()
    val timeoutMin = config.timeoutMin.get()
    val environmentVariables = config.environmentVariables.get()
    val testTargets = config.testTargets.get()
    val directoriesToPull = config.directoriesToPull.get()
    val flakyTestAttempts = config.flakyTestAttempts.get()
    val clientDetails = config.clientDetails.get()
    val otherFiles = config.otherFiles.get()
    val roboDirectives = config.roboDirectives.get()

    appendln("  use-orchestrator: $useOrchestrator")
    appendln("  auto-google-login: $autoGoogleLogin")
    appendln("  record-video: $recordVideo")
    appendln("  performance-metrics: $performanceMetrics")
    appendln("  timeout: ${timeoutMin}m")

    if (environmentVariables.isNotEmpty()) {
      appendln("  environment-variables:")
      environmentVariables.forEach { (key, value) ->
        appendln("    $key: $value")
      }
    }
    if (testTargets.isNotEmpty()) {
      appendln("  test-targets:")
      testTargets.forEach { target ->
        appendln("  - $target")
      }
    }
    if (directoriesToPull.isNotEmpty()) {
      appendln("  directories-to-pull:")
      directoriesToPull.forEach { dir ->
        appendln("  - $dir")
      }
    }

    appendln(flakyTestAttemptsLine(flakyTestAttempts))

    if (clientDetails.isNotEmpty()) {
      appendln("  client-details:")
      clientDetails.forEach { appendln("    ${it.key}: ${it.value}") }
    }

    if (otherFiles.isNotEmpty()) {
      appendln("  other-files:")
      otherFiles.forEach { appendln("    ${it.key}: ${it.value}") }
    }

    if (roboDirectives.isNotEmpty()) {
      appendln("  robo-directives:")
      roboDirectives.forEach {
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
