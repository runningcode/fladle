package com.osacky.flank.gradle

import groovy.lang.Closure
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.listProperty
import org.gradle.kotlin.dsl.property

open class FlankGradleExtension(project: Project) : FladleConfig {
  val flankCoordinates: Property<String> = project.objects.property(String::class.java).convention("com.github.flank:flank")
  val flankVersion: Property<String> = project.objects.property(String::class.java).convention("20.05.1")
  // Project id is automatically discovered by default. Use this to override the project id.
  override var projectId: String? = null
  override val serviceAccountCredentials: RegularFileProperty = project.objects.fileProperty()
  override var useOrchestrator: Boolean = false
  override var autoGoogleLogin: Boolean = false
  override var devices: List<Map<String, String>> = listOf(mapOf("model" to "NexusLowRes", "version" to "28"))

  // https://cloud.google.com/sdk/gcloud/reference/firebase/test/android/run
  override var testTargets: List<String> = emptyList()

  override var testShards: Int? = null
  override var shardTime: Int? = null
  override var repeatTests: Int? = null

  // Shard Android tests by time using historical run data. The amount of shards used is set by `testShards`.
  override var smartFlankGcsPath: String? = null

  override var resultsHistoryName: String? = null

  override var flakyTestAttempts = 0

  // Variant to use for configuring output APK.
  var variant: String? = null

  /**
   * debugApk and instrmentationApk are [Property<String>] and not [RegularFileProperty] because we support wildcard characters.
   */
  val debugApk: Property<String> = project.objects.property()
  val instrumentationApk: Property<String> = project.objects.property()

  override var directoriesToPull: List<String> = emptyList()

  override var filesToDownload: List<String> = emptyList()

  override var environmentVariables: Map<String, String> = emptyMap()

  override var timeoutMin: Int = 15

  override var recordVideo: Boolean = true

  override var performanceMetrics: Boolean = true

  override var resultsBucket: String? = null

  override var keepFilePath: Boolean = false

  override var resultsDir: String? = null

  override var additionalTestApks: ListProperty<String> = project.objects.listProperty()

  override var runTimeout: Property<String> = project.objects.property()

  override var ignoreFailedTests: Property<Boolean> = project.objects.property()

  override var disableSharding: Boolean = false

  override var smartFlankDisableUpload: Boolean = false

  override var testRunnerClass: String? = null

  override var localResultsDir: Property<String> = project.objects.property()

  override var numUniformShards: Int? = null

  override var clientDetails: Map<String, String> = emptyMap()

  override var testTargetsAlwaysRun: List<String> = emptyList()

  override var otherFiles: Map<String, String> = emptyMap()

  override var networkProfile: String? = null

  override var roboScript: String? = null

  override var roboDirectives: List<List<String>> = emptyList()

  val configs: NamedDomainObjectContainer<FladleConfigImpl> = project.container(FladleConfigImpl::class.java) {
    FladleConfigImpl(
      name = it,
      projectId = projectId,
      serviceAccountCredentials = serviceAccountCredentials,
      useOrchestrator = useOrchestrator,
      autoGoogleLogin = autoGoogleLogin,
      devices = devices,
      testTargets = testTargets,
      testShards = testShards,
      shardTime = shardTime,
      repeatTests = repeatTests,
      smartFlankGcsPath = smartFlankGcsPath,
      resultsHistoryName = resultsHistoryName,
      flakyTestAttempts = flakyTestAttempts,
      directoriesToPull = directoriesToPull,
      filesToDownload = filesToDownload,
      environmentVariables = environmentVariables,
      timeoutMin = timeoutMin,
      recordVideo = recordVideo,
      performanceMetrics = performanceMetrics,
      resultsBucket = resultsBucket,
      keepFilePath = keepFilePath,
      resultsDir = resultsDir,
      additionalTestApks = additionalTestApks,
      runTimeout = runTimeout,
      ignoreFailedTests = ignoreFailedTests,
      disableSharding = disableSharding,
      smartFlankDisableUpload = smartFlankDisableUpload,
      testRunnerClass = testRunnerClass,
      localResultsDir = localResultsDir,
      numUniformShards = numUniformShards,
      clientDetails = clientDetails,
      testTargetsAlwaysRun = testTargetsAlwaysRun,
      otherFiles = otherFiles,
      networkProfile = networkProfile,
      roboScript = roboScript,
      roboDirectives = roboDirectives
    )
  }

  fun configs(closure: Closure<*>) {
    configs.configure(closure)
  }
}
