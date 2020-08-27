package com.osacky.flank.gradle

import groovy.lang.Closure
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.kotlin.dsl.listProperty
import org.gradle.kotlin.dsl.property
import javax.inject.Inject

open class FlankGradleExtension @Inject constructor(objects: ObjectFactory) : FladleConfig {
  @get:Input
  val flankCoordinates: Property<String> = objects.property(String::class.java).convention("com.github.flank:flank")
  @get:Input
  val flankVersion: Property<String> = objects.property(String::class.java).convention("20.08.4")
  // Project id is automatically discovered by default. Use this to override the project id.
  override var projectId: String? = null
  override val serviceAccountCredentials: RegularFileProperty = objects.fileProperty()
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
  @get:Input
  @get:Optional
  val variant: Property<String> = objects.property()

  /**
   * debugApk and instrumentationApk are [Property<String>] and not [RegularFileProperty] because we support wildcard characters.
   */
  @get:Input
  @get:Optional
  val debugApk: Property<String> = objects.property()
  @get:Input
  @get:Optional
  val instrumentationApk: Property<String> = objects.property()

  override var directoriesToPull: List<String> = emptyList()

  override var filesToDownload: List<String> = emptyList()

  override var environmentVariables: Map<String, String> = emptyMap()

  override var recordVideo: Boolean = true

  override var performanceMetrics: Boolean = true

  override var resultsBucket: String? = null

  override var keepFilePath: Boolean = false

  override var resultsDir: String? = null

  override val additionalTestApks: ListProperty<String> = objects.listProperty()

  override val runTimeout: Property<String> = objects.property()

  override val ignoreFailedTests: Property<Boolean> = objects.property()

  override var disableSharding: Boolean = false

  override var smartFlankDisableUpload: Boolean = false

  override var testRunnerClass: String? = null

  override val localResultsDir: Property<String> = objects.property()

  override var numUniformShards: Int? = null

  override var clientDetails: Map<String, String> = emptyMap()

  override var testTargetsAlwaysRun: List<String> = emptyList()

  override var otherFiles: Map<String, String> = emptyMap()

  override var networkProfile: String? = null

  override var roboScript: String? = null

  override var roboDirectives: List<List<String>> = emptyList()

  override var testTimeout: String = "15m"

  override val outputStyle: Property<String> = objects.property<String>().convention("single")

  @Internal
  val configs: NamedDomainObjectContainer<FladleConfigImpl> = objects.domainObjectContainer(FladleConfigImpl::class.java) {
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
      roboDirectives = roboDirectives,
      testTimeout = testTimeout,
      outputStyle = outputStyle
    )
  }

  fun configs(closure: Closure<*>) {
    configs.configure(closure)
  }
}
