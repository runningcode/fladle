package com.osacky.flank.gradle

data class FladleConfigImpl(
  internal val name: String,
  override var flankVersion: String,
  override var projectId: String? = null,
  override var serviceAccountCredentials: String? = null,
  override var useOrchestrator: Boolean = false,
  override var autoGoogleLogin: Boolean = false,
  override var devices: List<Map<String, String>> = listOf(mapOf("model" to "NexusLowRes", "version" to "28")),
  override var testTargets: List<String> = emptyList(),
  override var shardTime: Int? = null,
  override var testShards: Int? = null,
  override var repeatTests: Int? = null,
  override var smartFlankGcsPath: String? = null,
  override var resultsHistoryName: String? = null,
  override var flakyTestAttempts: Int,
  override var directoriesToPull: List<String> = emptyList(),
  override var filesToDownload: List<String> = emptyList(),
  override var environmentVariables: Map<String, String> = emptyMap(),
  override var timeoutMin: Int = 15,
  override var recordVideo: Boolean = true,
  override var performanceMetrics: Boolean = true,
  override var resultsBucket: String? = null,
  override var keepFilePath: Boolean = false
) : FladleConfig
