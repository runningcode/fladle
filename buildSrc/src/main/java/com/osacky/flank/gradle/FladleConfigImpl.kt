package com.osacky.flank.gradle

import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property

data class FladleConfigImpl(
  internal val name: String,
  override var projectId: String?,
  override val serviceAccountCredentials: RegularFileProperty,
  override var useOrchestrator: Boolean,
  override var autoGoogleLogin: Boolean,
  override var devices: List<Map<String, String>>,
  override var testTargets: List<String>,
  override var shardTime: Int?,
  override var testShards: Int?,
  override var repeatTests: Int?,
  override var smartFlankGcsPath: String?,
  override var resultsHistoryName: String?,
  override var flakyTestAttempts: Int,
  override var directoriesToPull: List<String>,
  override var filesToDownload: List<String>,
  override var environmentVariables: Map<String, String>,
  override var recordVideo: Boolean,
  override var performanceMetrics: Boolean,
  override var resultsBucket: String?,
  override var keepFilePath: Boolean,
  override var resultsDir: String?,
  override var additionalTestApks: ListProperty<String>,
  override var runTimeout: Property<String>,
  override var ignoreFailedTests: Property<Boolean>,
  override var disableSharding: Boolean,
  override var smartFlankDisableUpload: Boolean,
  override var testRunnerClass: String?,
  override var localResultsDir: Property<String>,
  override var numUniformShards: Int?,
  override var clientDetails: Map<String, String>,
  override var testTargetsAlwaysRun: List<String>,
  override var otherFiles: Map<String, String>,
  override var networkProfile: String?,
  override var roboScript: String?,
  override var roboDirectives: List<List<String>>,
  override var timeout: String
) : FladleConfig
