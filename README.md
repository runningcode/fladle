# Fladle - The Gradle Plugin for Firebase Test Lab and Flank

Fladle is a Gradle plugin which simplifies the configuration necessary to use Firebase Test Lab and Flank.


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
    classpath "gradle.plugin.com.osacky.flank.gradle:fladle:0.3.4"
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


## Optional Configuration

``` groovy
fladle {
    // Required parameters
    serviceAccountCredentials("${project.file("flank-gradle-5cf02dc90531.json")}")

    // Optional parameters
    useOrchestrator = false
    testTargets = [
        "class com.osacky.flank.gradle.sample.ExampleInstrumentedTest#seeView"
    ]
    devices = [
        new Device("NexusLowRes", 28, null, null),
        new Device("Nexus5", 23, null, null)
    ]
    projectId("flank-gradle")
    flankVersion("v3.1.1")
    debugApk("$buildDir/outputs/apk/debug/sample-debug.apk")
    instrumentationApk("$buildDir/outputs/apk/androidTest/debug/sample-debug-androidTest.apk"
    autoGoogleLogin = true
    testShards = 5
    smartFlankGcsPath = gs://tmp_flank/flank/test_app_android.xml
    configs {
        oranges {
            useOrchestrator = false
            testTargets = [
                "class com.osacky.flank.gradle.sample.ExampleInstrumentedTest#runAndFail"
            ]
        }
    }
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

### projectId
The projectId is a unique identifier which can be found in the project's URL: `https://console.firebase.google.com/project/<projectId>`
This is automatically discovered based on the service credential by default.

### flankVersion
`flankVersion("flank_snapshot")` to specify a Flank snapshot.

`flankVersion("v3.1.1")` to specify a specific Flank version.


### debugApk
This is the path to the app's debug apk.

### instrumentationApk
This is the path to the app's instrumentation apk.

### autoGoogleLogin
Whether or not to automatically log in using a preconfigured google account. [More Info](https://cloud.google.com/sdk/gcloud/reference/firebase/test/android/run#--auto-google-login)

### testShards
Overrides the number of automatically determined test shards for Flank to use. Uses Flanks default value when not specified.

### repeatTests
The number of times to repeat each test. Uses Flanks default value when not specified.

### configs
Give a name to a custom flank task and configure its options. The name is appended to the end of the flank task. For example `runFlank` becomes `runFlank<name>`.

### smartFlankGcsPath
Shard Android tests by time using historical run data. The amount of shards used is set by `testShards`.

### variant
Which variant and buildType to use for testing. For example: 'debug' or 'devDebug'.

---

# Publishing

``` bash
./gradlew -b buildSrc/build.gradle.kts publishPlugins
```
