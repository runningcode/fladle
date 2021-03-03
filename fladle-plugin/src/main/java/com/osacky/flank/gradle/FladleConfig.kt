package com.osacky.flank.gradle

import com.osacky.flank.gradle.validation.SinceFlank
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import kotlin.reflect.full.memberProperties

interface FladleConfig {
  // Project id is automatically discovered by default. Use this to override the project id.
  @get:Input
  @get:Optional
  val projectId: Property<String>

  @get:InputFile
  @get:Optional
  val serviceAccountCredentials: RegularFileProperty

  /**
   * debugApk and instrumentationApk are [Property<String>] and not [RegularFileProperty] because we support wildcard characters.
   */
  @get:Input
  @get:Optional
  val debugApk: Property<String>

  @get:Input
  @get:Optional
  val instrumentationApk: Property<String>

  @get:Input
  val sanityRobo: Property<Boolean>

  @get:Input
  val useOrchestrator: Property<Boolean>

  @get:Input
  val autoGoogleLogin: Property<Boolean>

  @get:Input
  val devices: ListProperty<Map<String, String>>

  // https://cloud.google.com/sdk/gcloud/reference/firebase/test/android/run
  @get:Input
  val testTargets: ListProperty<String>

  @Deprecated(
    message = "testShards is deprecated. Use maxTestShards instead",
    replaceWith = ReplaceWith("maxTestShards")
  )
  /**
   * The maximum number of shards. Value will be overwritten by [maxTestShards] if both used in configuration
   */
  @get:Input
  @get:Optional
  val testShards: Property<Int>

  /**
   * The maximum number of shards
   */
  @get:Input
  @get:Optional
  val maxTestShards: Property<Int>

  /**
   * shardTime - the amount of time tests within a shard should take
   * when set to > 0, the shard count is dynamically set based on time up to the maximmum limit defined by maxTestShards
   * 2 minutes (120) is recommended.
   * default: -1 (unlimited)
   */
  @get:Input
  @get:Optional
  val shardTime: Property<Int>

  @get:SinceFlank("8.0.0")
  @get:Input
  @get:Optional
  val repeatTests: Property<Int>

  @get:Input
  @get:Optional
  val smartFlankGcsPath: Property<String>

  @get:Input
  @get:Optional
  val resultsHistoryName: Property<String>

  @get:Input
  val directoriesToPull: ListProperty<String>

  @get:Input
  val filesToDownload: ListProperty<String>

  @get:Input
  val environmentVariables: MapProperty<String, String>

  @get:Input
  val recordVideo: Property<Boolean>

  @get:Input
  val performanceMetrics: Property<Boolean>

  // The number of times to retry failed tests. Default is 0. Max is 10.
  @get:SinceFlank("8.0.0")
  @get:Input
  val flakyTestAttempts: Property<Int>

  @get:Input
  @get:Optional
  val resultsBucket: Property<String>

  @get:SinceFlank("8.1.0", hasDefaultValue = true)
  @get:Input
  val keepFilePath: Property<Boolean>

  /**
   * The name of a unique Google Cloud Storage object within the results bucket where raw test results will be stored
   * (default: a timestamp with a random suffix).
   */
  @get:Input
  @get:Optional
  val resultsDir: Property<String>

  @get:SinceFlank("6.1.0")
  @get:Input
  val additionalTestApks: ListProperty<String>

  /**
   * The max time this test run can execute before it is cancelled (default: unlimited).
   */
  @get:SinceFlank("20.05.0")
  @get:Input
  @get:Optional
  val runTimeout: Property<String>

  /**
   * Terminate with exit code 0 when there are failed tests.
   * Useful for Fladle and other gradle plugins that don't expect the process to have a non-zero exit code.
   * The JUnit XML is used to determine failure. (default: false)
   */
  @get:SinceFlank("20.05.0", hasDefaultValue = true)
  @get:Input
  @get:Optional
  val ignoreFailedTests: Property<Boolean>

  /**
   * Disables sharding. Useful for parameterized tests. (default: false)
   */
  @get:Input
  val disableSharding: Property<Boolean>

  /**
   * Disables smart flank JUnit XML uploading. Useful for preventing timing data from being updated. (default: false)
   */
  @get:Input
  val smartFlankDisableUpload: Property<Boolean>

  /**
   * The fully-qualified Java class name of the instrumentation test runner
   * (default: the last name extracted from the APK manifest).
   */
  @get:SinceFlank("6.2.0")
  @get:Input
  @get:Optional
  val testRunnerClass: Property<String>

  /**
   * Local folder to store the test result.
   * Folder is DELETED before each run to ensure only artifacts from the new run are saved.
   */
  @get:Input
  @get:Optional
  val localResultsDir: Property<String>

  /**
   * Specifies the number of shards into which you want to evenly distribute test cases.
   * The shards are run in parallel on separate devices. For example,
   * if your test execution contains 20 test cases and you specify four shards, each shard executes five test cases.
   * The number of shards should be less than the total number of test cases.
   * The number of shards specified must be >= 1 and <= 50.
   * This option cannot be used along max-test-shards and is not compatible with smart sharding.
   * If you want to take benefits of smart sharding use max-test-shards instead.
   * (default: null)
   */
  @get:SinceFlank("20.05.0")
  @get:Input
  @get:Optional
  val numUniformShards: Property<Int>

  /**
   * A key-value map of additional details to attach to the test matrix.
   * Arbitrary key-value pairs may be attached to a test matrix to provide additional context about the tests being run.
   * When consuming the test results, such as in Cloud Functions or a CI system,
   * these details can add additional context such as a link to the corresponding pull request.
   */
  @get:SinceFlank("20.05.0")
  @get:Input
  val clientDetails: MapProperty<String, String>

  /**
   * Always run - these tests are inserted at the beginning of every shard
   * useful if you need to grant permissions or login before other tests run
   */
  @get:Input
  val testTargetsAlwaysRun: ListProperty<String>

  /**
   * A list of device-path: file-path pairs that indicate the device paths to push files to the device before starting tests, and the paths of files to push.
   * Device paths must be under absolute, whitelisted paths (${EXTERNAL_STORAGE}, or ${ANDROID_DATA}/local/tmp).
   * Source file paths may be in the local filesystem or in Google Cloud Storage (gs://…).
   */
  @get:SinceFlank("20.05.0")
  @get:Input
  val otherFiles: MapProperty<String, String>

  /**
   * The name of the network traffic profile, for example LTE, HSPA, etc,
   * which consists of a set of parameters to emulate network conditions when running the test
   * (default: no network shaping; see available profiles listed by the `flank test network-profiles list` command).
   * This feature only works on physical devices.
   */
  @get:SinceFlank("20.05.0")
  @get:Input
  @get:Optional
  val networkProfile: Property<String>

  /**
   * The path to a Robo Script JSON file.
   * The path may be in the local filesystem or in Google Cloud Storage using gs:// notation.
   * You can guide the Robo test to perform specific actions by recording a Robo Script in Android Studio and then specifying this argument.
   * Learn more at [https://firebase.google.com/docs/test-lab/robo-ux-test#scripting].
   */
  @get:SinceFlank("20.05.0")
  @get:Input
  @get:Optional
  val roboScript: Property<String>

  /**
   * List of robo_directives that you can use to customize the behavior of Robo test.
   * The type specifies the action type of the directive, which may take on values click, text or ignore.
   * Each directive is list of String = [type, key, value]
   * Each key should be the Android resource name of a target UI element and each value should be the text input for that element.
   * Values are only permitted for text type elements, so no value should be specified for click and ignore type elements.
   */
  @get:SinceFlank("20.05.0")
  @get:Input
  val roboDirectives: ListProperty<List<String>>

  /**
   * The max time test execution can run before it is cancelled (default: 15m).
   * It does not include any time necessary to prepare and clean up the target device.
   * The maximum possible testing time is 45m on physical devices and 60m on virtual devices.
   * The TIMEOUT units can be h, m, or s. If no unit is given, seconds are assumed.
   *
   * Examples:
   * * 1h   -> 1 hour
   * * 5m   -> 5 minutes
   * * 200s -> 200 seconds
   * * 100  -> 100 seconds
   */
  @get:Input
  val testTimeout: Property<String>

  /**
   * Output style of execution status. May be one of [verbose, multi, single].
   * For runs with only one test execution the default value is 'verbose', in other cases
   * 'multi' is used as the default. The output style 'multi' is not displayed correctly on consoles
   * which don't support ansi codes, to avoid corrupted output use single or verbose.
   */
  @get:SinceFlank("20.06.0", hasDefaultValue = true)
  @get:Input
  val outputStyle: Property<String>

  /**
   * Flank provides two ways for parsing junit xml results.
   * New way uses google api instead of merging xml files, but can generate slightly different output format.
   * This flag allows fallback for legacy xml junit results parsing
   */
  @get:SinceFlank("20.05.0", hasDefaultValue = true)
  @get:Input
  val legacyJunitResult: Property<Boolean>

  /**
   * Enables creating an additional local junit result on local storage with failure nodes on passed flaky tests.
   */
  @get:SinceFlank("20.06.0", hasDefaultValue = true)
  @get:Input
  val fullJunitResult: Property<Boolean>

  /**
   * A list of up to 100 additional APKs to install, in addition to those being directly tested.
   * The path may be in the local filesystem or in Google Cloud Storage using gs:// notation.
   */
  @get:SinceFlank("20.05.0")
  @get:Input
  @get:Optional
  val additionalApks: ListProperty<String>

  /**
   * Enable using average time from previous tests duration when using SmartShard and tests did not run before.
   * (default: false)
   */
  @get:SinceFlank("20.08.4")
  @get:Input
  @get:Optional
  val useAverageTestTimeForNewTests: Property<Boolean>

  /**
   * Set default test time expressed in seconds, used for calculating shards.
   * (default: 120.0s)
   */
  @get:SinceFlank("20.08.4")
  @get:Input
  @get:Optional
  val defaultTestTime: Property<Double>

  /**
   * Set default parameterized class test time expressed in seconds, used for calculating shards.
   * (default: 2x [defaultTestTime] => 240s)
   */
  @get:SinceFlank("20.08.4")
  @get:Input
  @get:Optional
  val defaultClassTestTime: Property<Double>

  /**
   * Disable flank results upload on gcloud storage.
   * (default: false)
   */
  @get:SinceFlank("20.07.0")
  @get:Input
  @get:Optional
  val disableResultsUpload: Property<Boolean>

  /**
   * Specifies a group of packages, classes, and/or test cases to run in each shard (a group of test cases).
   * The shards are run in parallel on separate devices. You can use this option up to 50 times to specify multiple shards when one or more physical devices are selected,
   * or up to 500 times when no physical devices are selected.
   * Note: If you include the flags environment-variable or test-targets when running test-targets-for-shard, the flags are applied to all the shards you create.
   * You can also specify multiple packages, classes, or test cases in the same shard by separating each item with a comma.
   * To specify both package and class in the same shard, separate package and class with semi-colons.
   */
  @get:SinceFlank("20.12.0")
  @get:Input
  @get:Optional
  val testTargetsForShard: ListProperty<String>

  /**
   * Whether to grant runtime permissions on the device before the test begins.
   * By default, all permissions are granted. PERMISSIONS must be one of: all, none
   * (default: all)
   */
  @get:SinceFlank("20.12.0")
  @get:Input
  @get:Optional
  val grantPermissions: Property<String>

  /**
   * The type of test to run. TYPE must be one of: instrumentation, robo, game-loop.
   * Use if you want to be sure there is only one type of tests being run
   * (flank enables to run mixed types of test in one run).
   */
  @get:SinceFlank("20.12.0")
  @get:Input
  @get:Optional
  val type: Property<String>

  /**
   * A list of game-loop scenario labels (default: None). Each game-loop scenario may be labeled in the
   * APK manifest file with one or more arbitrary strings, creating logical groupings (e.g. GPU_COMPATIBILITY_TESTS).
   * If --scenario-numbers and --scenario-labels are specified together, Firebase Test Lab will first execute each scenario from --scenario-numbers.
   * It will then expand each given scenario label into a list of scenario numbers marked with that label, and execute those scenarios.
   */
  @get:SinceFlank("20.12.0")
  @get:Input
  @get:Optional
  val scenarioLabels: ListProperty<String>

  /**
   * A list of game-loop scenario numbers which will be run as part of the test (default: all scenarios).
   * A maximum of 1024 scenarios may be specified in one test matrix, but the maximum number may also be limited by the overall test --timeout setting.
   */
  @get:SinceFlank("20.12.0")
  @get:Input
  @get:Optional
  val scenarioNumbers: ListProperty<Int>

  /**
   * A list of one or two Android OBB file names which will be copied to each test device before the tests will run (default: None).
   * Each OBB file name must conform to the format as specified by Android (e.g. [main|patch].0300110.com.example.android.obb) and will be installed into <shared-storage>/Android/obb/<package-name>/ on the test device.
   */
  @get:SinceFlank("20.12.0")
  @get:Input
  @get:Optional
  val obbFiles: ListProperty<String>

  /**
   * A list of OBB required filenames. OBB file name must conform to the format as specified by Android e.g.
   * [main|patch].0300110.com.example.android.obb which will be installed into <shared-storage>/Android/obb/<package-name>/ on the device.
   */
  @get:SinceFlank("20.12.0")
  @get:Input
  @get:Optional
  val obbNames: ListProperty<String>

  /**
   * If true, only a single attempt at most will be made to run each execution/shard in the matrix.
   * Flaky test attempts are not affected. Normally, 2 or more attempts are made if a potential
   * infrastructure issue is detected. This feature is for latency sensitive workloads. The
   * incidence of execution failures may be significantly greater for fail-fast matrices and support
   * is more limited because of that expectation.
   */
  @get:SinceFlank("21.01.0")
  @get:Input
  @get:Optional
  val failFast: Property<Boolean>

  /**
   * Allow appending additional config to flank root yaml. This option is useful when you would like to test option
   * before it is available on Fladle. Supports both single and multiple properties.
   */
  @get:Input
  @get:Optional
  val additionalFlankOptions: Property<String>

  /**
   * When enabled, the execution of flank will depend on the Gradle tasks to assemble the debug and instrumentation APKs.
   * before flank runs
   */
  @get:Input
  @get:Optional
  val dependOnAssemble: Property<Boolean>

  /**
   * Allow appending additional config to gcloud root yaml. This option is useful when you would like to test option
   * before it is available on Fladle. Supports both single and multiple properties.
   */
  @get:Input
  @get:Optional
  val additionalGcloudOptions: Property<String>

  @Internal
  fun getPresentProperties() = this::class.memberProperties
    .filter {
      when (val prop = it.call(this)) {
        is Property<*> -> prop.isPresent
        is MapProperty<*, *> -> prop.isPresent && prop.get().isNotEmpty()
        is ListProperty<*> -> prop.isPresent && prop.get().isNotEmpty()
        else -> false
      }
    }
}
