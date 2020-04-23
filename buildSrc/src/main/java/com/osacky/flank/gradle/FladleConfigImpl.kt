package com.osacky.flank.gradle

data class FladleConfigImpl(
  internal val name: String,
  override var flankCoordinates: String,
  override var flankVersion: String,
  override var projectId: String?,
  override var serviceAccountCredentials: String?,
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
  override var timeoutMin: Int,
  override var recordVideo: Boolean,
  override var performanceMetrics: Boolean,
  override var resultsBucket: String?,
  override var keepFilePath: Boolean,
  override var resultsDir: String?,
  override var additionalTestApks: Map<String, List<String>>
) : FladleConfig
