package com.osacky.flank.gradle

import groovy.lang.Closure
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.kotlin.dsl.listProperty
import org.gradle.kotlin.dsl.mapProperty
import org.gradle.kotlin.dsl.property
import javax.inject.Inject

open class FlankGradleExtension @Inject constructor(objects: ObjectFactory) : FladleConfig {
  @get:Input
  val flankCoordinates: Property<String> = objects.property(String::class.java).convention("com.github.flank:flank")

  override val sanityRobo: Property<Boolean> = objects.property<Boolean>().convention(false)

  @get:Input
  val flankVersion: Property<String> = objects.property(String::class.java).convention("21.01.1")
  // Project id is automatically discovered by default. Use this to override the project id.
  override val projectId: Property<String> = objects.property()
  override val serviceAccountCredentials: RegularFileProperty = objects.fileProperty()
  override val useOrchestrator: Property<Boolean> = objects.property<Boolean>().convention(false)
  override val autoGoogleLogin: Property<Boolean> = objects.property<Boolean>().convention(false)
  override val devices: ListProperty<Map<String, String>> = objects.listProperty<Map<String, String>>().convention(listOf(mapOf("model" to "NexusLowRes", "version" to "28")))

  // https://cloud.google.com/sdk/gcloud/reference/firebase/test/android/run
  override val testTargets: ListProperty<String> = objects.listProperty()

  override val testShards: Property<Int> = objects.property()
  override val shardTime: Property<Int> = objects.property()
  override val repeatTests: Property<Int> = objects.property()

  // Shard Android tests by time using historical run data. The amount of shards used is set by `testShards`.
  override val smartFlankGcsPath: Property<String> = objects.property()

  override val resultsHistoryName: Property<String> = objects.property()

  override val flakyTestAttempts: Property<Int> = objects.property<Int>().convention(0)

  // Variant to use for configuring output APK.
  @get:Input
  @get:Optional
  val variant: Property<String> = objects.property()

  /**
   * debugApk and instrumentationApk are [Property<String>] and not [RegularFileProperty] because we support wildcard characters.
   */
  override val debugApk: Property<String> = objects.property()

  override val instrumentationApk: Property<String> = objects.property()

  override val directoriesToPull: ListProperty<String> = objects.listProperty()

  override val filesToDownload: ListProperty<String> = objects.listProperty()

  override val environmentVariables: MapProperty<String, String> = objects.mapProperty()

  override val recordVideo: Property<Boolean> = objects.property<Boolean>().convention(true)

  override val performanceMetrics: Property<Boolean> = objects.property<Boolean>().convention(true)

  override val resultsBucket: Property<String> = objects.property()

  override val keepFilePath: Property<Boolean> = objects.property<Boolean>().convention(false)

  override val resultsDir: Property<String> = objects.property()

  override val additionalTestApks: ListProperty<String> = objects.listProperty()

  override val runTimeout: Property<String> = objects.property()

  override val ignoreFailedTests: Property<Boolean> = objects.property<Boolean>().convention(false)

  override val disableSharding: Property<Boolean> = objects.property<Boolean>().convention(false)

  override val smartFlankDisableUpload: Property<Boolean> = objects.property<Boolean>().convention(false)

  override val testRunnerClass: Property<String> = objects.property()

  override val localResultsDir: Property<String> = objects.property()

  override val numUniformShards: Property<Int> = objects.property()

  override val clientDetails: MapProperty<String, String> = objects.mapProperty()

  override val testTargetsAlwaysRun: ListProperty<String> = objects.listProperty()

  override val otherFiles: MapProperty<String, String> = objects.mapProperty()

  override val networkProfile: Property<String> = objects.property()

  override val roboScript: Property<String> = objects.property()

  override val roboDirectives: ListProperty<List<String>> = objects.listProperty()

  override val testTimeout: Property<String> = objects.property<String>().convention("15m")

  override val outputStyle: Property<String> = objects.property<String>().convention("single")

  override val legacyJunitResult: Property<Boolean> = objects.property<Boolean>().convention(false)

  override val fullJunitResult: Property<Boolean> = objects.property<Boolean>().convention(false)

  override val additionalApks: ListProperty<String> = objects.listProperty()

  override val defaultTestTime: Property<Double> = objects.property()

  override val useAverageTestTimeForNewTests: Property<Boolean> = objects.property()

  override val defaultClassTestTime: Property<Double> = objects.property()

  override val disableResultsUpload: Property<Boolean> = objects.property()

  override val testTargetsForShard: ListProperty<String> = objects.listProperty()

  override val grantPermissions: Property<String> = objects.property()

  override val type: Property<String> = objects.property()

  override val scenarioLabels: ListProperty<String> = objects.listProperty()

  override val scenarioNumbers: ListProperty<Int> = objects.listProperty()

  override val obbFiles: ListProperty<String> = objects.listProperty()

  override val obbNames: ListProperty<String> = objects.listProperty()

  override val failFast: Property<Boolean> = objects.property()

  override val maxTestShards: Property<Int> = objects.property()

  @Internal
  val configs: NamedDomainObjectContainer<FladleConfigImpl> = objects.domainObjectContainer(FladleConfigImpl::class.java) {
    FladleConfigImpl(
      name = it,
      projectId = objects.property<String>().convention(projectId),
      serviceAccountCredentials = objects.fileProperty().convention(serviceAccountCredentials),
      debugApk = objects.property<String>().convention(debugApk),
      instrumentationApk = objects.property<String>().convention(instrumentationApk),
      sanityRobo = objects.property<Boolean>().convention(sanityRobo),
      useOrchestrator = objects.property<Boolean>().convention(useOrchestrator),
      autoGoogleLogin = objects.property<Boolean>().convention(autoGoogleLogin),
      devices = objects.listProperty<Map<String, String>>().convention(devices),
      testTargets = objects.listProperty<String>().convention(testTargets),
      testShards = objects.property<Int>().convention(testShards),
      shardTime = objects.property<Int>().convention(shardTime),
      repeatTests = objects.property<Int>().convention(repeatTests),
      smartFlankGcsPath = objects.property<String>().convention(smartFlankGcsPath),
      resultsHistoryName = objects.property<String>().convention(resultsHistoryName),
      flakyTestAttempts = objects.property<Int>().convention(flakyTestAttempts),
      directoriesToPull = objects.listProperty<String>().convention(directoriesToPull),
      filesToDownload = objects.listProperty<String>().convention(filesToDownload),
      environmentVariables = objects.mapProperty<String, String>().convention(environmentVariables),
      recordVideo = objects.property<Boolean>().convention(recordVideo),
      performanceMetrics = objects.property<Boolean>().convention(performanceMetrics),
      resultsBucket = objects.property<String>().convention(resultsBucket),
      keepFilePath = objects.property<Boolean>().convention(keepFilePath),
      resultsDir = objects.property<String>().convention(resultsDir),
      additionalTestApks = objects.listProperty<String>().convention(additionalTestApks),
      runTimeout = objects.property<String>().convention(runTimeout),
      ignoreFailedTests = objects.property<Boolean>().convention(ignoreFailedTests),
      disableSharding = objects.property<Boolean>().convention(disableSharding),
      smartFlankDisableUpload = objects.property<Boolean>().convention(smartFlankDisableUpload),
      testRunnerClass = objects.property<String>().convention(testRunnerClass),
      localResultsDir = objects.property<String>().convention(localResultsDir),
      numUniformShards = objects.property<Int>().convention(numUniformShards),
      clientDetails = objects.mapProperty<String, String>().convention(clientDetails),
      testTargetsAlwaysRun = objects.listProperty<String>().convention(testTargetsAlwaysRun),
      otherFiles = objects.mapProperty<String, String>().convention(otherFiles),
      networkProfile = objects.property<String>().convention(networkProfile),
      roboScript = objects.property<String>().convention(roboScript),
      roboDirectives = objects.listProperty<List<String>>().convention(roboDirectives),
      testTimeout = objects.property<String>().convention(testTimeout),
      outputStyle = objects.property<String>().convention(outputStyle),
      legacyJunitResult = objects.property<Boolean>().convention(legacyJunitResult),
      fullJunitResult = objects.property<Boolean>().convention(fullJunitResult),
      additionalApks = objects.listProperty<String>().convention(additionalApks),
      useAverageTestTimeForNewTests = objects.property<Boolean>().convention(useAverageTestTimeForNewTests),
      defaultTestTime = objects.property<Double>().convention(defaultTestTime),
      defaultClassTestTime = objects.property<Double>().convention(defaultClassTestTime),
      disableResultsUpload = objects.property<Boolean>().convention(disableResultsUpload),
      testTargetsForShard = objects.listProperty<String>().convention(testTargetsForShard),
      grantPermissions = objects.property<String>().convention(grantPermissions),
      type = objects.property<String>().convention(type),
      scenarioLabels = objects.listProperty<String>().convention(scenarioLabels),
      scenarioNumbers = objects.listProperty<Int>().convention(scenarioNumbers),
      obbFiles = objects.listProperty<String>().convention(obbFiles),
      obbNames = objects.listProperty<String>().convention(obbNames),
      failFast = objects.property<Boolean>().convention(failFast),
      maxTestShards = objects.property<Int>().convention(maxTestShards)
    )
  }

  fun configs(closure: Closure<*>) {
    configs.configure(closure)
  }
}
