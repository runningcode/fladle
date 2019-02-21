package com.osacky.flank.gradle

import groovy.lang.Closure
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project

open class FlankGradleExtension(project: Project) : FladleConfig {
  override var flankVersion: String = "v4.3.1"
  // Project id is automatically discovered by default. Use this to override the project id.
  override var projectId: String? = null
  override var serviceAccountCredentials: String? = null
  override var useOrchestrator: Boolean = false
  override var autoGoogleLogin: Boolean = false
  override var devices: List<Device> = listOf(Device("NexusLowRes", 28))

  // https://cloud.google.com/sdk/gcloud/reference/firebase/test/android/run
  override var testTargets: List<String> = emptyList()

  override var testShards: Int? = null
  override var repeatTests: Int? = null

  // Shard Android tests by time using historical run data. The amount of shards used is set by `testShards`.
  override var smartFlankGcsPath: String? = null

  override var resultsHistoryName: String? = null

  override var flakyTestAttempts = 0

  // Variant to use for configuring output APK.
  var variant: String? = null

  var debugApk: String? = null
  var instrumentationApk: String? = null

  override var directoriesToPull: List<String> = emptyList()

  override var filesToDownload: List<String> = emptyList()

  override var environmentVariables: Map<String, String> = emptyMap()

  override var timeoutMin: Int = 15

  override var recordVideo: Boolean = true

  override var performanceMetrics: Boolean = true

  val configs: NamedDomainObjectContainer<FladleConfigImpl> = project.container(FladleConfigImpl::class.java) {
    FladleConfigImpl(
      name = it,
      flankVersion = flankVersion,
      projectId = projectId,
      serviceAccountCredentials = serviceAccountCredentials,
      useOrchestrator = useOrchestrator,
      autoGoogleLogin = autoGoogleLogin,
      devices = devices,
      testTargets = testTargets,
      testShards = testShards,
      repeatTests = repeatTests,
      smartFlankGcsPath = smartFlankGcsPath,
      resultsHistoryName = resultsHistoryName,
      flakyTestAttempts = flakyTestAttempts,
      directoriesToPull = directoriesToPull,
      filesToDownload = filesToDownload,
      environmentVariables = environmentVariables,
      timeoutMin = timeoutMin,
      recordVideo = recordVideo,
      performanceMetrics = performanceMetrics
    )
  }

  fun configs(closure: Closure<*>) {
    configs.configure(closure)
  }
}