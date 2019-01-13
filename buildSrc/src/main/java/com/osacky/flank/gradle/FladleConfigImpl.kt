package com.osacky.flank.gradle

data class FladleConfigImpl(
  internal val name: String,
  override var flankVersion: String,
  override var projectId: String? = null,
  override var serviceAccountCredentials: String? = null,
  override var useOrchestrator: Boolean = false,
  override var autoGoogleLogin: Boolean = false,
  override var clearPackageData: Boolean = false,
  override var devices: List<Device> = listOf(Device("NexusLowRes", 28)),
  override var testTargets: List<String> = emptyList(),
  override var testShards: Int? = null,
  override var repeatTests: Int? = null,
  override var smartFlankGcsPath: String? = null,
  override var resultsHistoryName: String? = null,
  override var flakyTestAttempts: Int
) : FladleConfig
