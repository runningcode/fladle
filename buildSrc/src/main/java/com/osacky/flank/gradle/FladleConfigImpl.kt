package com.osacky.flank.gradle

import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property

data class FladleConfigImpl(
  internal val name: String,
  override var projectId: Property<String>,
  override val serviceAccountCredentials: RegularFileProperty,
  override var useOrchestrator: Property<Boolean>,
  override var autoGoogleLogin: Property<Boolean>,
  override var devices: ListProperty<Map<String, String>>,
  override var testTargets: ListProperty<String>,
  override var shardTime: Property<Int>,
  override var testShards: Property<Int>,
  override var repeatTests: Property<Int>,
  override var smartFlankGcsPath: Property<String>,
  override var resultsHistoryName: Property<String>,
  override var flakyTestAttempts: Property<Int>,
  override var directoriesToPull: ListProperty<String>,
  override var filesToDownload: ListProperty<String>,
  override var environmentVariables: MapProperty<String, String>,
  override var recordVideo: Property<Boolean>,
  override var performanceMetrics: Property<Boolean>,
  override var resultsBucket: Property<String>,
  override var keepFilePath: Property<Boolean>,
  override var resultsDir: Property<String>,
  override var additionalTestApks: ListProperty<String>,
  override var runTimeout: Property<String>,
  override var ignoreFailedTests: Property<Boolean>,
  override var disableSharding: Property<Boolean>,
  override var smartFlankDisableUpload: Property<Boolean>,
  override var testRunnerClass: Property<String>,
  override var localResultsDir: Property<String>,
  override var numUniformShards: Property<Int>,
  override var clientDetails: MapProperty<String, String>,
  override var testTargetsAlwaysRun: ListProperty<String>,
  override var otherFiles: MapProperty<String, String>,
  override var networkProfile: Property<String>,
  override var roboScript: Property<String>,
  override var roboDirectives: ListProperty<List<String>>,
  override var testTimeout: Property<String>,
  override var outputStyle: Property<String>
) : FladleConfig
