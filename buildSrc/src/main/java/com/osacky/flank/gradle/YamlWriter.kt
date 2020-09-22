package com.osacky.flank.gradle

import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.internal.impldep.com.google.common.annotations.VisibleForTesting

internal class YamlWriter {

  internal fun createConfigProps(config: FladleConfig, base: FlankGradleExtension): String {
    if (base.projectId == null) {
      check(base.serviceAccountCredentials.isPresent) { "ServiceAccountCredentials in fladle extension not set. https://github.com/runningcode/fladle#serviceaccountcredentials" }
    }
    check(base.debugApk.isPresent) { "debugApk must be specified" }
    check(base.instrumentationApk.isPresent xor base.roboScript.isPresent) {
      """
     Either instrumentationApk file or roboScript file must be specified but not both.
     instrumentationApk=${base.instrumentationApk.orNull}
     roboScript=${base.roboScript.orNull}
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

    appendIfPresent(config.smartFlankGcsPath, name = "smart-flank-gcs-path")

    projectId?.let {
      appendln("  project: $it")
    }
    appendIfPresent(config.keepFilePath, name = "keep-file-path")

    if (config.filesToDownload.isPresentAndNotEmpty) {
      val filesToDownload = config.filesToDownload.get()
      appendln("  files-to-download:")
      filesToDownload.forEach { file ->
        appendln("  - $file")
      }
    }

    if (config.additionalTestApks.isPresentAndNotEmpty) {
      val additionalTestApks = config.additionalTestApks.get()
      appendln("  additional-app-test-apks:")
      additionalTestApks.forEach {
        appendln("    $it")
      }
    }

    appendIfPresent(config.runTimeout, name = "run-timeout")
    appendIfPresent(config.ignoreFailedTests, name = "ignore-failed-tests")
    appendIfPresent(config.disableSharding, name = "disable-sharding")
    appendIfPresent(config.smartFlankDisableUpload, name = "smart-flank-disable-upload")
    appendIfPresent(config.localResultsDir, name = "local-result-dir")

    if (config.testTargetsAlwaysRun.isPresentAndNotEmpty) {
      val testTargetsAlwaysRun = config.testTargetsAlwaysRun.get()
      appendln("  test-targets-always-run:")
      testTargetsAlwaysRun.forEach {
        appendln("  - class $it")
      }
    }

    appendln("  legacy-junit-result: ${config.legacyJunitResult.get()}")
    appendln("  full-junit-result: ${config.fullJunitResult.get()}")
    appendln("  output-style: ${config.outputStyle.get()}")
  }

  internal fun writeAdditionalProperties(config: FladleConfig): String = buildString {
    appendln("  use-orchestrator: ${config.useOrchestrator}")
    appendln("  auto-google-login: ${config.autoGoogleLogin}")
    appendIfPresent(config.recordVideo, name = "record-video")
    appendIfPresent(config.performanceMetrics, name = "performance-metrics")
    appendIfPresent(config.testTimeout, name = "timeout")
    appendIfPresent(config.resultsHistoryName, name = "results-history-name")
    appendIfPresent(config.resultsBucket, name = "results-bucket")

    if (config.environmentVariables.isPresentAndNotEmpty) {
      val environmentVariables = config.environmentVariables.get()
      appendln("  environment-variables:")
      environmentVariables.forEach { (key, value) ->
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
    if (config.directoriesToPull.isPresentAndNotEmpty) {
      val directoriesToPull = config.directoriesToPull.get()
      appendln("  directories-to-pull:")
      directoriesToPull.forEach { dir ->
        appendln("  - $dir")
      }
    }
    appendIfPresent(config.flakyTestAttempts, name = "num-flaky-test-attempts")
    appendIfPresent(config.resultsDir, name = "results-dir")
    appendIfPresent(config.testRunnerClass, name = "test-runner-class")
    appendIfPresent(config.numUniformShards, name = "num-uniform-shards")

    if (config.clientDetails.isPresentAndNotEmpty) {
      val clientDetails = config.clientDetails.get()
      appendln("  client-details:")
      clientDetails.forEach {
        appendln("    ${it.key}: ${it.value}")
      }
    }

    if (config.otherFiles.isPresentAndNotEmpty) {
      val otherFiles = config.otherFiles.get()
      appendln("  other-files:")
      otherFiles.forEach {
        appendln("    ${it.key}: ${it.value}")
      }
    }

    appendIfPresent(config.networkProfile, name = "network-profile")
    appendIfPresent(config.roboScript, name = "robo-script")

    if (config.roboDirectives.isPresentAndNotEmpty) {
      val roboDirectives = config.roboDirectives.get()
      appendln("  robo-directives:")
      roboDirectives.forEach {
        val value = it.getOrElse(2) { "" }.let { stringValue -> if (stringValue.isBlank()) "\"\"" else stringValue }
        appendln("    ${it[0]}:${it[1]}: $value")
      }
    }
  }

  private fun <T> StringBuilder.appendIfPresent(prop: Property<T>, name: String) {
    if (prop.isPresent) appendln("  $name: ${prop.get()}")
  }

  private val <T> ListProperty<T>.isPresentAndNotEmpty
    get() = isPresent && get().isEmpty().not()

  private val <T, K> MapProperty<T, K>.isPresentAndNotEmpty
    get() = isPresent && get().isEmpty().not()

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
