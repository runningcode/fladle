package com.osacky.flank.gradle

import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal

data class FladleConfigImpl(
  @get:Internal internal val name: String,
  override val projectId: Property<String>,
  override val serviceAccountCredentials: RegularFileProperty,
  override val debugApk: Property<String>,
  override val instrumentationApk: Property<String>,
  override val sanityRobo: Property<Boolean>,
  override val useOrchestrator: Property<Boolean>,
  override val autoGoogleLogin: Property<Boolean>,
  override val devices: ListProperty<Map<String, String>>,
  override val testTargets: ListProperty<String>,
  override val shardTime: Property<Int>,
  override val testShards: Property<Int>,
  override val repeatTests: Property<Int>,
  override val smartFlankGcsPath: Property<String>,
  override val resultsHistoryName: Property<String>,
  override val flakyTestAttempts: Property<Int>,
  override val variant: Property<String>,
  override val directoriesToPull: ListProperty<String>,
  override val filesToDownload: ListProperty<String>,
  override val environmentVariables: MapProperty<String, String>,
  override val recordVideo: Property<Boolean>,
  override val performanceMetrics: Property<Boolean>,
  override val resultsBucket: Property<String>,
  override val keepFilePath: Property<Boolean>,
  override val resultsDir: Property<String>,
  override val additionalTestApks: ListProperty<String>,
  override val runTimeout: Property<String>,
  override val ignoreFailedTests: Property<Boolean>,
  override val disableSharding: Property<Boolean>,
  override val smartFlankDisableUpload: Property<Boolean>,
  override val testRunnerClass: Property<String>,
  override val localResultsDir: Property<String>,
  override val numUniformShards: Property<Int>,
  override val clientDetails: MapProperty<String, String>,
  override val testTargetsAlwaysRun: ListProperty<String>,
  override val otherFiles: MapProperty<String, String>,
  override val networkProfile: Property<String>,
  override val roboScript: Property<String>,
  override val roboDirectives: ListProperty<List<String>>,
  override val testTimeout: Property<String>,
  override val outputStyle: Property<String>,
  override val legacyJunitResult: Property<Boolean>,
  override val fullJunitResult: Property<Boolean>,
  override val additionalApks: ListProperty<String>,
  override val defaultTestTime: Property<Double>,
  override val useAverageTestTimeForNewTests: Property<Boolean>,
  override val defaultClassTestTime: Property<Double>,
  override val disableResultsUpload: Property<Boolean>,
  override val testTargetsForShard: ListProperty<String>,
  override val grantPermissions: Property<String>,
  override val type: Property<String>,
  override val scenarioLabels: ListProperty<String>,
  override val scenarioNumbers: ListProperty<Int>,
  override val obbFiles: ListProperty<String>,
  override val obbNames: ListProperty<String>,
  override val failFast: Property<Boolean>,
  override val maxTestShards: Property<Int>,
  override val additionalFlankOptions: Property<String>,
  override val additionalGcloudOptions: Property<String>,
  override val dependOnAssemble: Property<Boolean>,
  override val async: Property<Boolean>
) : FladleConfig {
  /**
   * Prepare config to run sanity robo.
   *
   * Sets [sanityRobo] property as `true`.
   *
   * Cleans [instrumentationApk], [additionalTestApks], [roboDirectives], [roboScript] properties.
   */
  fun clearPropertiesForSanityRobo() {
    sanityRobo.set(true)
    additionalTestApks.empty()
    // Must be set to an empty string to override `convention` value inherited from base.
    instrumentationApk.set("")
    roboDirectives.empty()
    // Must be set to an empty string to override `convention` value inherited from base.
    roboScript.set("")
  }
}
