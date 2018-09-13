# Fladle - The Gradle Plugin for Flank

More information about Flank can be found [here](https://github.com/testArmada/flank).

# Usage

Apply the plugin to an android application project.
```
plugins {
  id "com.osacky.fladle" version '0.2.0'
}
```

Specify the configuration using the fladle extension:
```
fladle {
    projectId("flank-gradle")
    serviceAccountCredentials("${project.file("flank-gradle-5cf02dc90531.json")}")
    debugApk("$buildDir/outputs/apk/debug/sample-debug.apk")
    instrumentationApk("$buildDir/outputs/apk/androidTest/debug/sample-debug-androidTest.apk")
    useOrchestrator = true
    devices = [
            new Device("NexusLowRes", 28, null, null),
            new Device("Nexus5", 23, null, null)
    ]
}

```

## Required Fields

### projectId
The projectId is a unique identifier which can be found in this project's URL: https://console.firebase.google.com/project/<projectId>

### serviceAccountCredentials 
The service account credential is a file which contains authentication credentials for a service account created in the Google Cloud Platform Console. It can be created with the editor role [here](https://console.cloud.google.com/iam-admin/serviceaccounts/).

### debugApk
This is the path to the app's debug apk.

### instrumentationApk
This is the path to the app's instrumentation apk.


See the sample module for an example usage.


### Publishing

`./gradlew -b buildSrc/build.gradle.kts publishPlugins`
