package com.osacky.flank.gradle

import groovy.lang.Closure
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project

open class FlankGradleExtension(project: Project) : FladleConfig {
  override var flankVersion: String = "v4.2.0"
  // Project id is automatically discovered by default. Use this to override the project id.
  override var projectId: String? = null
  override var serviceAccountCredentials: String? = null
  override var useOrchestrator: Boolean = false
  override var autoGoogleLogin: Boolean = false
  override var clearPackageData: Boolean = false
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

  val configs: NamedDomainObjectContainer<FladleConfigImpl> = project.container(FladleConfigImpl::class.java) {
    FladleConfigImpl(it, flankVersion, projectId, serviceAccountCredentials, useOrchestrator, autoGoogleLogin, clearPackageData, devices, testTargets, testShards, repeatTests, smartFlankGcsPath, resultsHistoryName, flakyTestAttempts)
  }

  fun configs(closure: Closure<*>) {
    configs.configure(closure)
  }
}