package com.osacky.flank.gradle

import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property

data class FladleConfigImpl(
  internal val name: String,
  override val projectId: Property<String>,
  override val serviceAccountCredentials: RegularFileProperty,
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
  override val fullJunitResult: Property<Boolean>
) : FladleConfig
