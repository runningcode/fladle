package com.osacky.flank.gradle

import groovy.lang.Closure
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.listProperty
import org.gradle.kotlin.dsl.mapProperty
import org.gradle.kotlin.dsl.property
import javax.inject.Inject

open class FlankGradleExtension @Inject constructor(objects: ObjectFactory) : FladleConfig {
  val flankCoordinates: Property<String> = objects.property(String::class.java).convention("com.github.flank:flank")
  val flankVersion: Property<String> = objects.property(String::class.java).convention("20.07.0")
  // Project id is automatically discovered by default. Use this to override the project id.
  override var projectId: Property<String> = objects.property()
  override val serviceAccountCredentials: RegularFileProperty = objects.fileProperty()
  override var useOrchestrator: Property<Boolean> = objects.property()
  override var autoGoogleLogin: Property<Boolean> = objects.property()
  override var devices: ListProperty<Map<String, String>> = objects.listProperty()

  // https://cloud.google.com/sdk/gcloud/reference/firebase/test/android/run
  override var testTargets: ListProperty<String> = objects.listProperty()

  override var testShards: Property<Int> = objects.property()
  override var shardTime: Property<Int> = objects.property()
  override var repeatTests: Property<Int> = objects.property()

  // Shard Android tests by time using historical run data. The amount of shards used is set by `testShards`.
  override var smartFlankGcsPath: Property<String> = objects.property()

  override var resultsHistoryName: Property<String> = objects.property()

  override var flakyTestAttempts: Property<Int> = objects.property()

  // Variant to use for configuring output APK.
  var variant: Property<String> = objects.property()

  /**
   * debugApk and instrumentationApk are [Property<String>] and not [RegularFileProperty] because we support wildcard characters.
   */
  val debugApk: Property<String> = objects.property()
  val instrumentationApk: Property<String> = objects.property()

  override var directoriesToPull: ListProperty<String> = objects.listProperty()

  override var filesToDownload: ListProperty<String> = objects.listProperty()

  override var environmentVariables: MapProperty<String, String> = objects.mapProperty()

  override var recordVideo: Property<Boolean> = objects.property()

  override var performanceMetrics: Property<Boolean> = objects.property()

  override var resultsBucket: Property<String> = objects.property()

  override var keepFilePath: Property<Boolean> = objects.property()

  override var resultsDir: Property<String> = objects.property()

  override var additionalTestApks: ListProperty<String> = objects.listProperty()

  override var runTimeout: Property<String> = objects.property()

  override var ignoreFailedTests: Property<Boolean> = objects.property()

  override var disableSharding: Property<Boolean> = objects.property()

  override var smartFlankDisableUpload: Property<Boolean> = objects.property()

  override var testRunnerClass: Property<String> = objects.property()

  override var localResultsDir: Property<String> = objects.property()

  override var numUniformShards: Property<Int> = objects.property()

  override var clientDetails: MapProperty<String, String> = objects.mapProperty()

  override var testTargetsAlwaysRun: ListProperty<String> = objects.listProperty()

  override var otherFiles: MapProperty<String, String> = objects.mapProperty()

  override var networkProfile: Property<String> = objects.property()

  override var roboScript: Property<String> = objects.property()

  override var roboDirectives: ListProperty<List<String>> = objects.listProperty()

  override var testTimeout: String = "15m"

  override var outputStyle: Property<String> = objects.property<String>().convention("single")

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
