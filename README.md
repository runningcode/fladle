# Fladle - The Gradle Plugin for Firebase Test Lab and Flank

Fladle is a Gradle plugin which simplifies the configuration necessary to use Firebase Test Lab and Flank.

[![CircleCI](https://circleci.com/gh/runningcode/fladle.svg?style=svg)](https://circleci.com/gh/runningcode/fladle)

Flank is a parallel test runner for Firebase Test Lab.

More information about Flank can be found [here](https://github.com/testArmada/flank).
Also read this [medium post](https://medium.com/walmartlabs/flank-smart-test-runner-for-firebase-cf65e1b1eca7).

Fladle's feature list is bare and doesn't yet support all the options that Flank does as this project is quite new. We welcome feature requests and contributions. We'd like to adapt it to more use cases to make it helpful for everyone.

# Usage

Using Fladle takes 3 steps:

1. Apply the Fladle plugin. Follow instructions [here](https://plugins.gradle.org/plugin/com.osacky.fladle)
``` groovy
buildscript {
  dependencies {
    classpath "com.osacky.flank.gradle:fladle:0.6.7"
  }
}

apply plugin: "com.osacky.fladle"
```
2. Configure the Fladle extension.
``` groovy
fladle {
    serviceAccountCredentials("${project.file("flank-gradle-service-account.json")}")
}
```
3. Run the flank gradle task.
``` bash
./gradlew runFlank
```

## Required Fields

### serviceAccountCredentials
The service account credential is a file which contains authentication credentials for a service account created in the Google Cloud Platform Console.
It can be created with the editor role [here](https://console.cloud.google.com/iam-admin/serviceaccounts/).
Instructions on how to create this account can be found [here](https://firebase.google.com/docs/test-lab/android/continuous).
  Optionally, the serviceAccountCredentials can be set with [environment variables](https://github.com/TestArmada/flank#authenticate-with-a-service-account) but then the projectId parameter must be set.


## Optional Configuration

``` groovy
fladle {
    // Required parameters
    serviceAccountCredentials("${project.file("flank-gradle-5cf02dc90531.json")}")

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
    timeoutMin = 15
    recordVideo = false
    performanceMetrics = false
//    // Use device list based on Device class
//    devices = [
//        new Device("NexusLowRes", 28, null, null),
//        new Device("Nexus5", 23, null, null)
    ]
    // Or, use map devices
    mapDevices = [
        [ "model": NexusLowRes", "version": "28" ],
        [ "model": Nexus5", "version": "23" ]
    ]
    projectId("flank-gradle")
    flankVersion("4.4.0")
    debugApk("$buildDir/outputs/apk/debug/sample-debug.apk")
    instrumentationApk("$buildDir/outputs/apk/androidTest/debug/sample-debug-androidTest.apk"
    autoGoogleLogin = true
    testShards = 5
    smartFlankGcsPath = "gs://tmp_flank/flank/test_app_android.xml"
    configs {
        oranges {
            useOrchestrator = false
            testTargets = [
                "class com.osacky.flank.gradle.sample.ExampleInstrumentedTest#runAndFail"
            ]
        }
    }
    resultsBucket("my-results-bucket-name")
    keepFilePath = true
}
```


### useOrchestrator
Whether or not we should use the android test orchestrator to run this tests.
Set this to true when the build.gradle file includes `testOptions.execution 'ANDROID_TEST_ORCHESTRATOR'`

### testTargets
Set multiple testTargets to be run by flank.
See [Google Cloud Firebase docs](https://cloud.google.com/sdk/gcloud/reference/firebase/test/android/run) for more information.

### devices
A list of devices to run the tests against. When list is empty, a default device will be used. When the Device parameter is null, a default value will be used.

### mapDevices
Similar to `devices` but instead is a list of `HashMap` values correlating to devices to run the tests against. When list is empty, a default device will be used. The keys in each map are `model`, `version`, `orientation`, and `locale`.  When the a key is not set or is null, a default value will be used.

### projectId
The projectId is a unique identifier which can be found in the project's URL: `https://console.firebase.google.com/project/<projectId>`
This is automatically discovered based on the service credential by default.

### flankVersion
`flankVersion("flank_snapshot")` to specify a Flank snapshot.

`flankVersion("4.4.0")` to specify a specific Flank version.


### debugApk
This is the path to the app's debug apk.

### instrumentationApk
This is the path to the app's instrumentation apk.

### autoGoogleLogin
Whether or not to automatically log in using a preconfigured google account. [More Info](https://cloud.google.com/sdk/gcloud/reference/firebase/test/android/run#--auto-google-login)

### environmentVariables
Environment variables are mirrored as extra options to the am instrument -e KEY1 VALUE1 … command and passed to your test runner (typically AndroidJUnitRunner). Examples
```
environmentVariables = [
    "clearPackageData": "true" // Whether or not to remove all shared state from your device's CPU and memory after each test. [More info](https://developer.android.com/training/testing/junit-runner)
]
```

### testShards
Overrides the number of automatically determined test shards for Flank to use. Uses Flanks default value when not specified.

### shardTime
The amount of time tests within a shard should take.
When set to > 0, the shard count is dynamically set based on time up to the maximmum limit defined by maxTestShards
2 minutes (120) is recommended.
default: -1 (unlimited)

### repeatTests
The number of times to repeat each test. Uses Flanks default value when not specified.

### configs
Give a name to a custom flank task and configure its options. The name is appended to the end of the flank task. For example `runFlank` becomes `runFlank<name>`.

### smartFlankGcsPath
Shard Android tests by time using historical run data. The amount of shards used is set by `testShards`.

### variant
Which variant and buildType to use for testing. For example: 'debug' or 'devDebug'.

### flakyTestAttempts
The number of times to retry failed tests. Default is 0. Max is 10.

### directoriesToPull
A list of paths that will be copied from the device's storage to the designated results bucket after the test is complete. These must be absolute paths under /sdcard or /data/local/tmp. Path names are restricted to the characters a-zA-Z0-9_-./+. The paths /sdcard and /data will be made available and treated as implicit path substitutions. E.g. if /sdcard on a particular device does not map to external storage, the system will replace it with the external storage path prefix for that device.

### filesToDownload
List of regex that is matched against bucket paths (for example: `2019-01-09_00:13:06.106000_YCKl/shard_0/NexusLowRes-28-en-portrait/bugreport.txt`) for files to be downloaded after a flank run.

### timeoutMin
The max time in minutes this test execution can run before it is cancelled (default: 15 min). It does not include any time necessary to prepare and clean up the target device. The maximum possible testing time is 30m on physical devices and 60m on virtual devices. 

### recordVideo
Enable video recording during the test. Enabled by default.

### performanceMetrics
Monitor and record performance metrics: CPU, memory, network usage, and FPS (game-loop only). Enabled by default.

### resultsBucket
The name of a Google Cloud Storage bucket where raw test results will be stored.

### keepFilePath
Keeps the full path of downloaded files from a Google Cloud Storage bucket. Required when file names are not unique. Disabled by default.

---
### Error APK file not found
The app APK and the instrumentation apk are expected to have already been generated before calling runFlank.
If you would like the flank task to automatically create the APKs, you can add the following to your application's build.gradle.
```
afterEvaluate {
    tasks.named("execFlank").configure {
        dependsOn("assembleDebugAndroidTest")
    }
}
```

---

Fladle is mentioned in the [Fragmented Podcast #163](https://fragmentedpodcast.com/episodes/163/). Skip to ~29 minutes.

You can point Fladle to whatever version of Flank you like using the [flankVersion](#flankVersion) argument. You can even point to snapshot builds.

# Publishing

## Gradle Plugin Portal
``` bash
./gradlew -b buildSrc/build.gradle.kts publishPlugins
```
## Maven Central
``` bash
./gradlew -b buildSrc/build.gradle.kts publishMavenJavaPublicationToMavenRepository
```
