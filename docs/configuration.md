# Configuration

The following configuration options must be set inside the fladle block. See the [sample configuration](/fladle/configuration#sample-configuration) below. There is also a [groovy sample](https://github.com/runningcode/fladle/blob/master/sample/build.gradle) and a [kotlin sample](https://github.com/runningcode/fladle/blob/master/sample-kotlin/build.gradle.kts).

### serviceAccountCredentials

!!! note ""
    User Authentication is also possible. See [Authentication.](../authentication)


=== "Groovy"
    ``` groovy
    serviceAccountCredentials = project.layout.projectDirectory.file("flank-gradle-5cf02dc90531.json")
    ```
=== "Kotlin"
    ``` kotlin
    serviceAccountCredentials.set("project.layout.projectDirectory.file("flank-gradle-5cf02dc90531.json")
    ```

Optionally, the serviceAccountCredentials can be set with [environment variables](https://flank.github.io/flank/#authenticate-with-a-service-account) but then the projectId parameter must be set.

See [Authentication] for more info.

[Authentication]: ../authentication


### variant

!!! note
    `variant` must be set if using buildFlavors in order to automatically configure the debugApk and testApk.

Set the variant to automatically configure for testing. A build variant is a combination of buildFlavor and buildType.
This must also be set when testing against a non-default variant.
For example: 'debug' or 'freeDebug'.
Put this inside your Fladle block.

=== "Groovy"
    ``` groovy
    variant = "freeDebug"
    ```
=== "Kotlin"
    ``` kotlin
    variant.set("freeDebug")
    ```

## Sample Configuration

``` groovy
fladle {
    // Required parameters
    serviceAccountCredentials = project.layout.projectDirectory.file("flank-gradle-5cf02dc90531.json")
    variant = "freeDebug"

    // Optional parameters
    useOrchestrator = false
    environmentVariables = [
        "clearPackageData": "true"
    ]
    directoriesToPull = [
        "/sdcard/screenshots"
    ]
    filesToDownload = [
        ".*/screenshots/.*"
    ]
    testTargets = [
        "class com.osacky.flank.gradle.sample.ExampleInstrumentedTest#seeView"
    ]
    testTimeout = "15m"
    recordVideo = false
    performanceMetrics = false
    devices = [
        [ "model": "NexusLowRes", "version": "28" ],
        [ "model": "Nexus5", "version": "23" ]
    ]
    projectId("flank-gradle")
    flankVersion = "{{ fladle.flank_version }}"
    debugApk = "$buildDir/outputs/apk/debug/sample-debug.apk"
    instrumentationApk = "$buildDir/outputs/apk/androidTest/debug/sample-debug-androidTest.apk"
    additionalTestApks = [
        "- app: "$buildDir/outputs/apk/debug/sample-debug.apk",
        "  test: $buildDir/outputs/apk/androidTest/debug/sample2-debug-androidTest.apk",
        "- test: ${rootProject.buildDir}/database/module/outputs/apk/database-module-androidTest.apk"
    ]
    autoGoogleLogin = true
    testShards = 5 //or numUniformShards=5 cannot use both. Deprecated use maxTestShards
    maxTestShards = 8
    shardTime = 120
    smartFlankGcsPath = "gs://tmp_flank/flank/test_app_android.xml"
    configs {
        oranges {
            useOrchestrator.set(false)
            testTargets.set(project.provider { [
                "class com.osacky.flank.gradle.sample.ExampleInstrumentedTest#runAndFail"
            ] })
        }
    }
    resultsBucket("my-results-bucket-name")
    keepFilePath = true
    runTimout = "45m"
    ignoreFailedTests = false
    disableSharding = false
    smartFlankDisableUpload = false
    testRunnerClass = "com.foo.TestRunner"
    localResultsDir = "flank"
    clientDetails = [
      "key1": "value1",
      "key2": "value2"
    ]
    testTargetsAlwaysRun = [
      "com.example.TestSuite#test1",
      "com.example.TestSuite#test2"
    ]
    otherFiles = [
      "/sdcard/dir1/file1.txt": "/my/example/path/file1.txt",
      "/sdcard/dir2/file2.txt": "/my/example/path/file2.txt"
    ]
    networkProfile = "LTE"
    roboDirectives = [
      ["click", "button1", ""],
      ["ignore", "button2"],
      ["text", "field1", "my text"],
    ]
    outputStyle = 'multi'
    legacyJunitResult = false
    fullJunitResult = false
    additionalApks = [
      "gs://path/to/app1.apk",
      "localPath/to/app2.apk"
    ]
    defaultTestTime = 5.3
    defaultClassTestTime = 180.5
    useAverageTestTimeForNewTests = true
    disableResultsUpload = true
    grantPermissions = "none"
    type = "game-loop"
    scenarioLabels = [
      "label1",
      "label2" 
    ]
    scenarioNumbers = [ 1, 23, 52 ]
    obbFiles = [
      "local/file/path/test1.obb",
      "local/file/path/test2.obb"
    ]
    obbNames = [
      "patch.0300110.com.example.android.obb",
      "patch.0300111.com.example.android.obb"
    ]
    testTargetsForShard = [
      "package com.package1.for.shard1, com.package2.for.shard1",
      "class com.foo.ClassForShard2#testMethod1, com.foo.ClassForShard2#testMethod2",
      "class com.foo.ClassForShard3; package com.package.for.shard3"
    ]
    failFast = true
}
```


### useOrchestrator
Whether or not we should use the android test orchestrator to run this tests.
Set this to true when the build.gradle file includes `testOptions.execution 'ANDROID_TEST_ORCHESTRATOR'`

=== "Groovy"
    ``` groovy
    useOrchestrator = true
    ```
=== "Kotlin"
    ``` kotlin
    useOrchestrator.set(true)
    ```

### testTargets
Set multiple testTargets to be run by Flank. These are used to whitelist or blacklist test classes, test cases and test annotations.
See [Google Cloud Firebase docs](https://cloud.google.com/sdk/gcloud/reference/firebase/test/android/run) for more information.

=== "Groovy"
    ``` groovy
    testTargets = [
            "class com.osacky.flank.gradle.sample.ExampleInstrumentedTest#seeView"
    ]
    ```
=== "Kotlin"
    ``` kotlin
    testTargets.set(listOf(
            "class com.osacky.flank.gradle.sample.ExampleInstrumentedTest#seeView"
    ))
    ```


### devices
A list of devices to run the tests against. When list is empty, a default device will be used. Each device entry is a map.  The valid keys in the map are `model`, `version`, `orientation`, and `locale`.  When a key is not set or is null, a default value will be used.

=== "Groovy"
    ``` groovy
    devices = [
            [ "model": "Pixel2", "version": "26" ],
            [ "model": "Nexus5", "version": "23" ]
    ]
    ```
=== "Kotlin"
    ``` kotlin
    devices.set(listOf(
        mapOf("model" to "Pixel2", "version" to "26" ),
        mapOf("model" to "Nexus5", "version" to "23" )
    ))
    ```

### projectId
The projectId is a unique identifier which can be found in the project's URL: `https://console.firebase.google.com/project/<projectId>`
This is automatically discovered based on the service credential by default.

=== "Groovy"
    ``` groovy
    projectId = "flank-gradle"
    ```
=== "Kotlin"
    ``` kotlin
    projectId.set("flank-gradle")
    ```

### flankVersion
Need a different Flank version? Specify it with `flankVersion`.

To use a snapshot:
=== "Groovy"
    ``` groovy
    flankVersion = "flank_snapshot"`
    ```
=== "Kotlin"
    ``` kotlin
    flankVersion.set("flank_snapshot")
    ```

Need more than 50 shards? Use Flank `8.1.0`.

To use a different version:

=== "Groovy"
    ``` groovy
    flankVersion = "{{ fladle.flank_version }}"
    ```
=== "Kotlin"
    ``` kotlin
    flankVersion.set("{{ fladle.flank_version }}")
    ```

### flankCoordinates
Specify custom flank maven coordinates.

=== "Groovy"
    ``` groovy
    flankCoordinates = "com.github.flank:flank"
    ```
=== "Kotlin"
    ``` kotlin
    flankCoordinates.set("com.github.flank:flank")
    ```

### debugApk
This is a string representing the path to the app's debug apk.
Supports wildcard characters.
Optional, prefer to set [variant](/configuration#variant).

=== "Groovy"
    ``` groovy
    debugApk = project.provider { "${buildDir.toString()}/outputs/apk/debug/*.apk" }
    ```
=== "Kotlin"
    ``` kotlin
    debugApk.set(project.provider { "${buildDir.toString()}/outputs/apk/debug/*.apk" })
    ```

### instrumentationApk
This is a string representing the path to the app's instrumentaiton apk.
Supports wildcard characters.
Optional, prefer to set [variant](/configuration#variant).
InstrumenationApk should not be set when using [roboScript](/configuration#roboscript).

=== "Groovy"
    ``` groovy
    instrumentationApk = project.provider { "${buildDir.toString()}/outputs/apk/androidTest/debug/*.apk" }
    ```
=== "Kotlin"
    ``` kotlin
    instrumentationApk.set(project.provider { "${buildDir.toString()}/outputs/apk/androidTest/debug/*.apk" })
    ```

### additionalTestApks
Paths to additional test configurations.
Order matters. A test apk is run with the nearest previous listed app apk.
For library modules, add them to the list with a `- test:` in front. For test apks which belong to an application module, add them with `  test:`.
It is not required to list an app apk here. If there is no app apk listed in additionalTestApks, the test apks are run against the [debugApk](#debugapk).

=== "Groovy"
    ```groovy
    additionalTestApks.value(project.provider { [
    "- app: ../main/app/build/output/apk/debug/app.apk",
    "  test: ../main/app/build/output/apk/androidTest/debug/app-test.apk",
    "- app: ../sample/app/build/output/apk/debug/sample-app.apk",
    "  test: ../sample/app/build/output/apk/androidTest/debug/sample-app-test.apk",
    "- test: ../feature/room/build/output/apk/androidTest/debug/feature-room-test.apk",
    "- test: ../library/databases/build/output/apk/androidTest/debug/sample-databases-test.apk"
    ]})
    ```
=== "Kotlin"
    ``` kotlin
    additionalTestApks.value(project.provider { listOf(
    "- app: ../main/app/build/output/apk/debug/app.apk",
    "  test: ../main/app/build/output/apk/androidTest/debug/app-test.apk",
    "- app: ../sample/app/build/output/apk/debug/sample-app.apk",
    "  test: ../sample/app/build/output/apk/androidTest/debug/sample-app-test.apk",
    "- test: ../feature/room/build/output/apk/androidTest/debug/feature-room-test.apk",
    "- test: ../library/databases/build/output/apk/androidTest/debug/sample-databases-test.apk"
    )})
    ```
### autoGoogleLogin
Whether or not to automatically log in using a preconfigured google account. [More Info](https://cloud.google.com/sdk/gcloud/reference/firebase/test/android/run#--auto-google-login)

=== "Groovy"
    ``` groovy
    autoGoogleLogin = false
    ```
=== "Kotlin"
    ``` kotlin
    autoGoogleLogin.set(false)
    ```

### environmentVariables
Environment variables are mirrored as extra options to the am instrument -e KEY1 VALUE1 … command and passed to your test runner (typically AndroidJUnitRunner). Examples

=== "Groovy"
    ``` groovy
    environmentVariables = [
        // Whether or not to remove all shared state from your device's CPU and memory after each test. [More info](https://developer.android.com/training/testing/junit-runner)
        "clearPackageData": "true"
    ]
    ```
=== "Kotlin"
    ``` kotlin
    environmentVariables = mapOf(
        // Whether or not to remove all shared state from your device's CPU and memory after each test. [More info](https://developer.android.com/training/testing/junit-runner)
        "clearPackageData" to "true"
    )
    ```
### testShards
#### deprecated
The maximum number of shards. Value will be overwritten by [maxTestShards](./configuration.md#maxtestshards) if both used in configuration

=== "Groovy"
    ``` groovy
    testShards = 5
    ```
=== "Kotlin"
    ``` kotlin
    testShards.set(5)
    ```

### maxTestShards
The maximum number of shards.

=== "Groovy"
``` groovy
maxTestShards = 8
```
=== "Kotlin"
``` kotlin
maxTestShards.set(8)
```

### shardTime
The amount of time tests within a shard should take.
When set to > 0, the shard count is dynamically set based on time up to the maximum limit defined by maxTestShards
2 minutes (120) is recommended.
default: -1 (unlimited)

=== "Groovy"
    ``` groovy
    shardTime = 120
    ```
=== "Kotlin"
    ``` kotlin
    shardTime.set(120)
    ```

### repeatTests
The number of times to repeat each test. Uses Flank's default value when not specified.

=== "Groovy"
    ``` groovy
    repeatTests = 1
    ```
=== "Kotlin"
    ``` kotlin
    repeatTests.set(1)
    ```

### configs
Give a name to a custom flank task and configure its options. The name is appended to the end of the flank task. For example `runFlank` becomes `runFlank<name>`.

=== "Groovy"
    ``` groovy
    configs {
      repeatOneHundred {
        // DSL sugar for container elements is missing (= syntax): https://github.com/gradle/gradle/issues/9987
        repeatTests.set(100)
      }
    }
    ```
=== "Kotlin"
    ``` kotlin
    configs {
      create("repeatOneHundred") {
        repeatTests.set(100)
      }
    }
    ```

In the above example, the configuration is inherited from the outer fladle config but with the repeatTests property set to 100. Running `runFlankRepeateOneHundred` will execute this custom configuration.

### smartFlankGcsPath
Shard Android tests by time using historical run data. The amount of shards used is set by `testShards`.

=== "Groovy"
    ``` groovy
    smartFlankGcsPath = 'gs://tmp_flank/tmp/JUnitReport.xml'
    ```
=== "Kotlin"
    ``` kotlin
    smartFlankGcsPath.set("gs://tmp_flank/tmp/JUnitReport.xml")
    ```

### resultsHistoryName
The history name for your test results (an arbitrary string label; default: the application's label from the APK manifest). All tests which use the same history name will have their results grouped together in the Firebase console in a time-ordered test history list.

=== "Groovy"
    ``` groovy
    resultsHistoryName = 'android-history'
    ```
=== "Kotlin"
    ``` kotlin
    resultsHistoryName.set("android-history")
    ```

### flakyTestAttempts
The number of times to retry failed tests. Default is 0. Max is 10.
Setting the value to 1 will mean that test are retried once. If the test fails then succeeds after the retry the run
will be marked as "successful". The matrix with a flaky test will be marked as flaky.

=== "Groovy"
    ``` groovy
    flakyTestAttempts = 0
    ```
=== "Kotlin"
    ``` kotlin
    flakyTestAttempts.set(0)
    ```
### directoriesToPull
A list of paths that will be copied from the device's storage to the designated results bucket after the test is complete. These must be absolute paths under `/sdcard` or `/data/local/tmp`.  Path names are restricted to the characters `a-zA-Z0-9_-./+`. The paths `/sdcard` and `/data` will be made available and treated as implicit path substitutions. E.g. if `/sdcard` on a particular device does not map to external storage, the system will replace it with the external storage path prefix for that device.

=== "Groovy"
    ``` groovy
    directoriesToPull = [
      '/sdcard/tempDir1', '/data/local/tmp/tempDir2'
    ]
    ```
=== "Kotlin"
    ``` kotlin
    directoriesToPull.set(listOf(
      "/sdcard/tempDir1", "/data/local/tmp/tempDir2"
    ))
    ```

### filesToDownload
List of regex that is matched against bucket paths (for example: `2019-01-09_00:13:06.106000_YCKl/shard_0/NexusLowRes-28-en-portrait/bugreport.txt`) for files to be downloaded after a flank run. The results are downloaded to the `APP_MODULE/build/fladle/RESULTS` directory where RESULTS can be set by [`localResultsDir`](#localresultsdir) var otherwise defaulting to `results/`.

=== "Groovy"
    ``` groovy
    filesToDownload = [
      '.*/sdcard/tempDir1/.*', '.*/data/local/tmp/tempDir2/.*'
    ]
    ```
=== "Kotlin"
    ``` kotlin
    filesToDownload.set(listOf(
      ".*/sdcard/tempDir1/.*", ".*/data/local/tmp/tempDir2/.*"
    ))
    ```

### testTimeout
The max time test execution can run before it is cancelled (default: 15m). It does not include any time necessary to prepare and clean up the target device. The maximum possible testing time is 45m on physical devices and 60m on virtual devices. The TIMEOUT units can be h, m, or s. If no unit is given, seconds are assumed.
Examples:
* 1h   -> 1 hour
* 5m   -> 5 minutes
* 200s -> 200 seconds
* 100  -> 100 seconds

=== "Groovy"
    ``` groovy
    testTimeout = "1h"
    ```
=== "Kotlin"
    ``` kotlin
    testTimeout.set("1h")
    ```

### recordVideo
Enable video recording during the test. Enabled by default.

=== "Groovy"
    ``` groovy
    recordVideo = true
    ```
=== "Kotlin"
    ``` kotlin
    recordVideo.set(true)
    ```

### performanceMetrics
Monitor and record performance metrics: CPU, memory, network usage, and FPS (game-loop only). Enabled by default.

=== "Groovy"
    ``` groovy
    performanceMetrics = true
    ```
=== "Kotlin"
    ``` kotlin
    performanceMetrics.set(true)
    ```

### resultsBucket
The name of a Google Cloud Storage bucket where raw test results will be stored.

=== "Groovy"
    ``` groovy
    resultsBucket = "my-gcs-bucket-name"
    ```
=== "Kotlin"
    ``` kotlin
    resultsBucket.set("my-gcs-bucket-name")
    ```

### keepFilePath
Keeps the full path of downloaded files from a Google Cloud Storage bucket. Required when file names are not unique. Disabled by default.

=== "Groovy"
    ``` groovy
    keepFilePath = false
    ```
=== "Kotlin"
    ``` kotlin
    keepFilePath.set(false)
    ```

### resultsDir
The name of a unique Google Cloud Storage object within the results bucket where raw test results will be stored. The default is a timestamp with a random suffix.

=== "Groovy"
    ``` groovy
    resultsDir = "result-dir-${getTimeStamp()}"
    ```
=== "Kotlin"
    ``` kotlin
    resultsDir.set("result-dir-${getTimeStamp()}")
    ```

### disableSharding
Disables sharding. All tests will run on the same device. Useful for parameterized tests which do not support sharding. (default: false)

=== "Groovy"
    ``` groovy
    disableSharding = false
    ```
=== "Kotlin"
    ``` kotlin
    disableSharding.set(false)
    ```

### smartFlankDisableUpload
Disables smart flank JUnit XML uploading. Useful for preventing timing data from being updated. (default: false)
[What is Smart Flank?](https://flank.github.io/flank/smart_flank/)

=== "Groovy"
    ``` groovy
    smartFlankDisableUpload = false
    ```
=== "Kotlin"
    ``` kotlin
    smartFlankDisableUpload.set(false)
    ```

### testRunnerClass
The fully-qualified Java class name of the instrumentation test runner (default: the test manifest is parsed to determine the class name).

=== "Groovy"
    ``` groovy
    testRunnerClass = "com.example.MyCustomTestRunner"
    ```
=== "Kotlin"
    ``` kotlin
    testRunnerClass.set("com.example.MyCustomTestRunner")
    ```

### localResultsDir
The local directory to store the test results. Folder is DELETED before each run to ensure only artifacts from the new run are saved.

=== "Groovy"
    ``` groovy
    localResultsDir = "my-results-dir"
    ```
=== "Kotlin"
    ``` kotlin
    localResultsDir.set("my-results-dir")
    ```

### testTargetsAlwaysRun
Always run - these tests are inserted at the beginning of every shard. Useful if you need to grant permissions or login before other tests run

=== "Groovy"
    ``` groovy
    testTargetsAlwaysRun = [
      'class com.example.MyTestClass'
    ]
    ```
=== "Kotlin"
    ``` kotlin
    testTargetsAlwaysRun.set(listOf(
      "class com.example.MyTestClass"
    ))
    ```

!!! note ""
    The flags below are only available with Flank 20.05.0 or higher.

### runTimeout
The max time this test run can execute before it is cancelled. s (seconds), m (minutes), h (hours) suffixes are acceptable, mixes like 1h45m are currently not supported (default: unlimited).
Examples:

* 20, 20s -> 20 seconds
* 30m -> 30 minutes
* 2h -> 2 hours

=== "Groovy"
    ``` groovy
    runTimeout = "15m"
    ```
=== "Kotlin"
    ``` kotlin
    runTimeout.set("15m")
    ```

### ignoreFailedTests
Always return successful task completion even when there are failed tests. Useful when parsing JUnit XML to determine failure. (default: false)

=== "Groovy"
    ``` groovy
    ignoreFailedTests = false
    ```
=== "Kotlin"
    ``` kotlin
    ignoreFailedTests.set(false)
    ```

### numUniformShards
Specifies the number of shards into which you want to evenly distribute test cases. The shards are run in parallel on separate devices. For example, if your test execution contains 20 test cases and you specify four shards, each shard executes five test cases. The number of shards should be less than the total number of test cases. The number of shards specified must be >= 1 and <= 50. This option cannot be used along max-test-shards and is not compatible with smart sharding ([Smart Flank](https://flank.github.io/flank/smart_flank/)). If you want to take benefits of smart sharding use max-test-shards instead. (default: null)

=== "Groovy"
    ``` groovy
    numUniformShards = 50
    ```
=== "Kotlin"
    ``` kotlin
    numUniformShards.set(50)
    ```

### clientDetails
A key-value map of additional details to attach to the test matrix.([clientDetails in Google Cloud Docs](https://cloud.google.com/sdk/gcloud/reference/beta/firebase/test/android/run#--client-details)) Arbitrary key-value pairs may be attached to a test matrix to provide additional context about the tests being run. When consuming the test results, such as in Cloud Functions or a CI system, these details can add additional context such as a link to the corresponding pull request. ([Access Client Details](https://firebase.google.com/docs/test-lab/extend-with-functions#access_client_details)).
These can be used to provide additional context about the environment where the tests are being run.

=== "Groovy"
    ``` groovy
    clientDetails = [
        "test-type": "PR",
        "build-number": "132"
    ]
    ```
=== "Kotlin"
    ``` kotlin
    clientDetails.set(mapOf(
        "test-type" to "PR",
        "build-number" to "132"
    ))
    ```

### otherFiles
A list of device-path: file-path pairs that indicate the device paths to push files to the device before starting tests, and the paths of files to push. Device paths must be under absolute, whitelisted paths (${EXTERNAL_STORAGE}, or ${ANDROID_DATA}/local/tmp). Source file paths may be in the local filesystem or in Google Cloud Storage (gs://…).

=== "Groovy"
    ``` groovy
    otherFiles = [
        "/sdcard/dir1/file1.txt": "local/file.txt",
        "/sdcard/dir2/file2.jpg": "gs://bucket/file.jpg",
    ]
    ```
=== "Kotlin"
    ``` kotlin
    otherFiles.set(mapOf(
        "/sdcard/dir1/file1.txt" to "local/file.txt",
        "/sdcard/dir2/file2.jpg" to "gs://bucket/file.jpg",
    ))
    ```

### networkProfile
The name of the network traffic profile, for example LTE, HSPA, etc, which consists of a set of parameters to emulate network conditions when running the test (default: no network shaping; see available profiles listed by the `flank test network-profiles list` command). This feature only works on physical devices.

=== "Groovy"
    ``` groovy
    networkProfile = "LTE"
    ```
=== "Kotlin"
    ``` kotlin
    networkProfile.set("LTE")
    ```

### roboScript
The path to a Robo Script JSON file. The path may be in the local filesystem or in Google Cloud Storage using gs:// notation. You can guide the Robo test to perform specific actions by recording a Robo Script in Android Studio and then specifying this argument. Learn more at [DOCS](https://firebase.google.com/docs/test-lab/robo-ux-test#scripting).

=== "Groovy"
    ``` groovy
    roboScript = "my-robo-script.json"
    ```
=== "Kotlin"
    ``` kotlin
    roboScript.set("my-robo-script.json")
    ```

### roboDirectives
List of robo_directives that you can use to customize the behavior of Robo test. The type specifies the action type of the directive, which may take on values click, text or ignore. Each directive is list of String = [type, key, value]. Each key should be the Android resource name of a target UI element and each value should be the text input for that element. Values are only permitted for text type elements, so no value should be specified for click and ignore type elements.


=== "Groovy"
    ``` groovy
    roboDirectives = [
        ["test:input_resource_name", "message"],
        ["click:button_resource_name", ""],
    ]
    ```
=== "Kotlin"
    ``` kotlin
    roboDirectives.set(listOf(
        listOf("test:input_resource_name", "message"),
        listOf("click:button_resource_name", ""),
    ))
    ```


### outputStyle
Output style of execution status. May be one of [`verbose`, `multi`, `single`].
For runs with only one test execution the default value is 'verbose', in other cases 'multi' is used as the default. The output style 'multi' is not displayed correctly on consoles which don't support ansi codes, to avoid corrupted output use single or verbose.

`multi` displays separated status for each shard execution in separated line, lines are updated over time. Similar to gradle output when running multiple tasks in parallel. Requires ANSI codes support.

`single` displays shortened status of all executions in single line. Similar to gcloud output when running with sharding. Should work on any console.

Default value is single.

=== "Groovy"
    ``` groovy
    outputSyle = "single"
    ```
=== "Kotlin"
    ``` kotlin
    outputStyle.set("single")
    ```

### legacyJunitResult
Flank provides two ways for parsing junit xml results.
New way uses google api instead of merging xml files, but can generate slightly different output format.
This flag allows fallback for legacy xml junit results parsing

=== "Groovy"
    ``` groovy
    legacyJunitResult = false
    ```
=== "Kotlin"
    ``` kotlin
    legacyJunitResult.set(false)
    ```

### fullJunitResult
Enables creating an additional local junit result on local storage with failure nodes on passed flaky tests.

=== "Groovy"
    ``` groovy
    fullJunitResult = false
    ```
=== "Kotlin"
    ``` kotlin
    fullJunitResult.set(false)
    ```

### sanityRobo
Runs a sanityRobo test.
`instrumentationApk`, `roboDirectives`, `roboScript` and `additionalTestApks` must be blank or empty.

=== "Groovy"
    ``` groovy
    sanityRobo = true
    ```
=== "Kotlin"
    ``` kotlin
    sanityRobo.set(true)
    ```

### defaultTestTime
Set default test time expressed in seconds, used for calculating shards. (default: 120.0s)

=== "Groovy"
    ``` groovy
    defaultTestTime = 1.2
    ```
=== "Kotlin"
    ``` kotlin
    defaultTestTime.set(1.2)
    ```

### defaultClassTestTime
Set default parameterized class test time expressed in seconds, used for calculating shards. (default: 2x [defaultTestTime](configuration.md#defaulttesttime) => 240s)

=== "Groovy"
    ``` groovy
    defaultClassTestTime = 245.5
    ```
=== "Kotlin"
    ``` kotlin
    defaultClassTestTime.set(245,5)
    ```

### additionalApks
A list of up to 100 additional APKs to install, in addition to those being directly tested. The path may be in the local filesystem or in Google Cloud Storage using gs:// notation.

=== "Groovy"
    ``` groovy
    additionalApks = [
      "gs://path/to/app1.apk",
      "localPath/to/app2.apk"
    ]
    ```
=== "Kotlin"
    ``` kotlin
    additionalApks.set(
      project.provider {
        listOf("gs://path/to/app1.apk", "localPath/to/app2.apk")
      }
    )
    ```

### useAverageTestTimeForNewTests
Enable using average time from previous tests duration when using SmartShard and tests did not run before. (default: false)

=== "Groovy"
    ``` groovy
    useAverageTestTimeForNewTests = true
    ```
=== "Kotlin"
    ``` kotlin
    useAverageTestTimeForNewTests.set(true)
    ```

### disableResultsUpload
Disable flank results upload on gcloud storage. (default: false)

=== "Groovy"
    ``` groovy
    disableResultsUpload = true
    ```
=== "Kotlin"
    ``` kotlin
    disableResultsUpload.set(true)
    ```

### testTargetsForShard
Specifies a group of packages, classes, and/or test cases to run in each shard (a group of test cases).
The shards are run in parallel on separate devices. You can use this option up to 50 times to specify multiple shards when one or more physical devices are selected,
or up to 500 times when no physical devices are selected.
Note: If you include the flags environment-variable or test-targets when running test-targets-for-shard, the flags are applied to all the shards you create.
You can also specify multiple packages, classes, or test cases in the same shard by separating each item with a comma.
To specify both package and class in the same shard, separate package and class with semi-colons.

=== "Groovy"
    ```
    testTargetsForShard = [
      "package com.package1.for.shard1, com.package2.for.shard1",
      "class com.foo.ClassForShard2#testMethod1, com.foo.ClassForShard2#testMethod2",
      "class com.foo.ClassForShard3; package com.package.for.shard3"
      ]
    ```
=== "Kotlin"
    ```
    testTargetsForShard.set(
      project.provider {
        listOf(
          "package com.package1.for.shard1, com.package2.for.shard1",
          "class com.foo.ClassForShard2#testMethod1, com.foo.ClassForShard2#testMethod2",
          "class com.foo.ClassForShard3; package com.package.for.shard3"
        )
      }
    )
    ```

### grantPermissions
Whether to grant runtime permissions on the device before the test begins. By default, all permissions are granted. PERMISSIONS must be one of: all, none

=== "Groovy"
    ``` groovy
    grantPermissions = "none"
    ```
=== "Kotlin"
    ``` kotlin
    grantPermissions.set("none")
    ```

### type
The type of test to run. TYPE must be one of: instrumentation, robo, game-loop. Use if you want to be sure there is only one type of tests being run (flank enables to run mixed types of test in one run).

=== "Groovy"
    ``` groovy
    type = "game-loop"
    ```
=== "Kotlin"
    ``` kotlin
    type.set("game-loop")
    ```

### scenarioLabels
A list of game-loop scenario labels (default: None). Each game-loop scenario may be labeled in the APK manifest file with one or more arbitrary strings, creating logical groupings (e.g. GPU_COMPATIBILITY_TESTS).
If --scenario-numbers and --scenario-labels are specified together, Firebase Test Lab will first execute each scenario from --scenario-numbers.
It will then expand each given scenario label into a list of scenario numbers marked with that label, and execute those scenarios.

=== "Groovy"
    ```
    scenarioLabels = [
      "label1",
      "label2" 
    ]
    ```
=== "Kotlin"
    ```
    scenarioLabels.set(
      project.provider {
        listOf("label1", "label2")
      }
    )
    ```

### scenarioNumbers
A list of game-loop scenario numbers which will be run as part of the test (default: all scenarios).
A maximum of 1024 scenarios may be specified in one test matrix, but the maximum number may also be limited by the overall test --timeout setting.

=== "Groovy"
    ```
    scenarioNumbers = [ 1, 23, 52 ]
    ```
=== "Kotlin"
    ```
    scenarioNumbers.set(
      project.provider {
        listOf(1, 23, 52)
      }
    )
    ```

### obbFiles
A list of one or two Android OBB file names which will be copied to each test device before the tests will run (default: None).
Each OBB file name must conform to the format as specified by Android (e.g. [main|patch].0300110.com.example.android.obb) and will be installed into <shared-storage>/Android/obb/<package-name>/ on the test device.

=== "Groovy"
    ```
    obbFiles = [
      "local/file/path/test1.obb",
      "local/file/path/test2.obb"
    ]
    ```
=== "Kotlin"
    ```
    obbFiles.set(
      project.provider {
        listOf(
          "local/file/path/test1.obb",
          "local/file/path/test2.obb"
        )
      }
    )
    ```

### obbNames
A list of OBB required filenames. OBB file name must conform to the format as specified by Android e.g.
[main|patch].0300110.com.example.android.obb which will be installed into <shared-storage>/Android/obb/<package-name>/ on the device.

=== "Groovy"
    ```
    obbNames = [
      "patch.0300110.com.example.android.obb",
      "patch.0300111.com.example.android.obb"
    ]
    ```
=== "Kotlin"
    ```
    obbNames.set(
      project.provider {
        listOf(
          "patch.0300110.com.example.android.obb",
          "patch.0300111.com.example.android.obb"
        )
      }
    )
    ```

### failFast
If true, only a single attempt at most will be made to run each execution/shard in the matrix. Flaky test attempts are not affected.
Normally, 2 or more attempts are made if a potential infrastructure issue is detected.
This feature is for latency sensitive workloads. The incidence of execution failures may be significantly greater for
fail-fast matrices and support is more limited because of that expectation.

=== "Groovy"
    ```
    failFast = true
    ```

=== "Kotlin"
    ```
    failFast.set(true)
    ```
