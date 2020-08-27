package com.osacky.flank.gradle

import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property

interface FladleConfig {
  // Project id is automatically discovered by default. Use this to override the project id.
  var projectId: String?
  val serviceAccountCredentials: RegularFileProperty
  var useOrchestrator: Boolean
  var autoGoogleLogin: Boolean
  var devices: List<Map<String, String>>

  // https://cloud.google.com/sdk/gcloud/reference/firebase/test/android/run
  var testTargets: List<String>

  var testShards: Int?

  /**
   * shardTime - the amount of time tests within a shard should take
   * when set to > 0, the shard count is dynamically set based on time up to the maximmum limit defined by maxTestShards
   * 2 minutes (120) is recommended.
   * default: -1 (unlimited)
   */
  var shardTime: Int?

  var repeatTests: Int?

  var smartFlankGcsPath: String?

  var resultsHistoryName: String?

  var directoriesToPull: List<String>

  var filesToDownload: List<String>

  var environmentVariables: Map<String, String>

  var recordVideo: Boolean

  var performanceMetrics: Boolean

  // The number of times to retry failed tests. Default is 0. Max is 10.
  var flakyTestAttempts: Int

  var resultsBucket: String?

  var keepFilePath: Boolean

  /**
   * The name of a unique Google Cloud Storage object within the results bucket where raw test results will be stored
   * (default: a timestamp with a random suffix).
   */
  var resultsDir: String?

  var additionalTestApks: ListProperty<String>

  /**
   * The max time this test run can execute before it is cancelled (default: unlimited).
   */
  var runTimeout: Property<String>

  /**
   * Terminate with exit code 0 when there are failed tests.
   * Useful for Fladle and other gradle plugins that don't expect the process to have a non-zero exit code.
   * The JUnit XML is used to determine failure. (default: false)
   */
  var ignoreFailedTests: Property<Boolean>

  /**
   * Disables sharding. Useful for parameterized tests. (default: false)
   */
  var disableSharding: Boolean

  /**
   * Disables smart flank JUnit XML uploading. Useful for preventing timing data from being updated. (default: false)
   */
  var smartFlankDisableUpload: Boolean

  /**
   * The fully-qualified Java class name of the instrumentation test runner
   * (default: the last name extracted from the APK manifest).
   */
  var testRunnerClass: String?

  /**
   * Local folder to store the test result.
   * Folder is DELETED before each run to ensure only artifacts from the new run are saved.
   */
  var localResultsDir: Property<String>

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
  var numUniformShards: Int?

  /**
   * A key-value map of additional details to attach to the test matrix.
   * Arbitrary key-value pairs may be attached to a test matrix to provide additional context about the tests being run.
   * When consuming the test results, such as in Cloud Functions or a CI system,
   * these details can add additional context such as a link to the corresponding pull request.
   */
  var clientDetails: Map<String, String>

  /**
   * Always run - these tests are inserted at the beginning of every shard
   * useful if you need to grant permissions or login before other tests run
   */
  var testTargetsAlwaysRun: List<String>

  /**
   * A list of device-path: file-path pairs that indicate the device paths to push files to the device before starting tests, and the paths of files to push.
   * Device paths must be under absolute, whitelisted paths (${EXTERNAL_STORAGE}, or ${ANDROID_DATA}/local/tmp).
   * Source file paths may be in the local filesystem or in Google Cloud Storage (gs://…).
   */
  var otherFiles: Map<String, String>

  /**
   * The name of the network traffic profile, for example LTE, HSPA, etc,
   * which consists of a set of parameters to emulate network conditions when running the test
   * (default: no network shaping; see available profiles listed by the `flank test network-profiles list` command).
   * This feature only works on physical devices.
   */
  var networkProfile: String?

  /**
   * The path to a Robo Script JSON file.
   * The path may be in the local filesystem or in Google Cloud Storage using gs:// notation.
   * You can guide the Robo test to perform specific actions by recording a Robo Script in Android Studio and then specifying this argument.
   * Learn more at [https://firebase.google.com/docs/test-lab/robo-ux-test#scripting].
   */
  var roboScript: String?

  /**
   * List of robo_directives that you can use to customize the behavior of Robo test.
   * The type specifies the action type of the directive, which may take on values click, text or ignore.
   * Each directive is list of String = [type, key, value]
   * Each key should be the Android resource name of a target UI element and each value should be the text input for that element.
   * Values are only permitted for text type elements, so no value should be specified for click and ignore type elements.
   */
  var roboDirectives: List<List<String>>

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
  var testTimeout: String

  /**
   * Output style of execution status. May be one of [verbose, multi, single].
   * For runs with only one test execution the default value is 'verbose', in other cases
   * 'multi' is used as the default. The output style 'multi' is not displayed correctly on consoles
   * which don't support ansi codes, to avoid corrupted output use single or verbose.
   */
  var outputStyle: Property<String>

  /**
   * Flank provides two ways for parsing junit xml results.
   * New way uses google api instead of merging xml files, but can generate slightly different output format.
   * This flag allows fallback for legacy xml junit results parsing
   */
  var legacyJunitResult: Boolean

  /**
   * Enables creating an additional local junit result on local storage with failure nodes on passed flaky tests.
   */
  var fullJunitResult: Boolean
}
