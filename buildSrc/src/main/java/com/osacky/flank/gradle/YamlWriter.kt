package com.osacky.flank.gradle

import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.internal.impldep.com.google.common.annotations.VisibleForTesting

internal class YamlWriter {

  internal fun createConfigProps(config: FladleConfig, base: FlankGradleExtension): String {
    if (base.projectId.orNull == null) {
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

    val additionalProperties = writeAdditionalProperties(config)
    val flankProperties = writeFlankProperties(config)

    return buildString {
      appendln("gcloud:")
      appendln("  app: ${base.debugApk.get()}")
      if (base.instrumentationApk.isPresent) {
        appendln("  test: ${base.instrumentationApk.get()}")
      }
      if (config.devices.isPresentAndNotEmpty) appendln(createDeviceString(config.devices.get()))
      appendln(additionalProperties)
      append(flankProperties)
    }
  }

  internal fun writeFlankProperties(config: FladleConfig): String = buildString {
    appendln("flank:")

    appendProperty(config.testShards, name = "max-test-shards")
    appendProperty(config.shardTime, name = "shard-time")
    appendProperty(config.repeatTests, name = "num-test-runs")
    appendProperty(config.smartFlankGcsPath, name = "smart-flank-gcs-path")
    appendProperty(config.projectId, name = "project")
    appendProperty(config.keepFilePath, name = "keep-file-path")

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

    appendProperty(config.runTimeout, name = "run-timeout")
    appendProperty(config.ignoreFailedTests, name = "ignore-failed-tests")
    appendProperty(config.disableSharding, name = "disable-sharding")
    appendProperty(config.smartFlankDisableUpload, name = "smart-flank-disable-upload")
    appendProperty(config.localResultsDir, name = "local-result-dir")

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
    appendProperty(config.useOrchestrator, name = "use-orchestrator")
    appendProperty(config.autoGoogleLogin, name = "auto-google-login")
    appendProperty(config.recordVideo, name = "record-video")
    appendProperty(config.performanceMetrics, name = "performance-metrics")
    appendProperty(config.testTimeout, name = "timeout")
    appendProperty(config.resultsHistoryName, name = "results-history-name")
    appendProperty(config.resultsBucket, name = "results-bucket")

    if (config.environmentVariables.isPresentAndNotEmpty) {
      val environmentVariables = config.environmentVariables.get()
      appendln("  environment-variables:")
      environmentVariables.forEach { (key, value) ->
        appendln("    $key: $value")
      }
    }
    if (config.testTargets.isPresentAndNotEmpty) {
      val testTargets = config.testTargets.get()
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
    appendProperty(config.flakyTestAttempts, name = "num-flaky-test-attempts")
    appendProperty(config.resultsDir, name = "results-dir")
    appendProperty(config.testRunnerClass, name = "test-runner-class")
    appendProperty(config.numUniformShards, name = "num-uniform-shards")

    appendMapProperty(config.clientDetails, name = "client-details") {
      appendln("    ${it.key}: ${it.value}")
    }

    if (config.otherFiles.isPresentAndNotEmpty) {
      val otherFiles = config.otherFiles.get()
      appendln("  other-files:")
      otherFiles.forEach {
        appendln("    ${it.key}: ${it.value}")
      }
    }

    appendProperty(config.networkProfile, name = "network-profile")
    appendProperty(config.roboScript, name = "robo-script")

    if (config.roboDirectives.isPresentAndNotEmpty) {
      val roboDirectives = config.roboDirectives.get()
      appendln("  robo-directives:")
      roboDirectives.forEach {
        val value = it.getOrElse(2) { "" }.let { stringValue -> if (stringValue.isBlank()) "\"\"" else stringValue }
        appendln("    ${it[0]}:${it[1]}: $value")
      }
    }
  }

  private fun <T> StringBuilder.appendProperty(prop: Property<T>, name: String) {
    if (prop.isPresent) appendln("  $name: ${prop.get()}")
  }

  private fun <T, K> StringBuilder.appendMapProperty(
    prop: MapProperty<T, K>,
    name: String,
    custom: StringBuilder.(Map.Entry<T, K>) -> Unit
  ) {
    if (prop.isPresentAndNotEmpty) {
      appendln("  $name:")
      prop.get().forEach { custom(it) }
    }
  }

  private fun <T> StringBuilder.appendListProperty(
    prop: ListProperty<T>,
    name: String,
    custom: StringBuilder.(T) -> Unit
  ) {
    if (prop.isPresentAndNotEmpty) {
      appendln("  $name:")
      prop.get().forEach { custom(it) }
    }
  }

  private val <T> ListProperty<T>.isPresentAndNotEmpty
    get() = isPresent && get().isEmpty().not()

  private val <T, K> MapProperty<T, K>.isPresentAndNotEmpty
    get() = isPresent && get().isEmpty().not()

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
