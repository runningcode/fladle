# Fladle - The Gradle Plugin for Flank

Fladle is a Gradle plugin which reduces the configuration necessary to use Flank.


Flank is a parallel test runner for Firebase Test Lab.

More information about Flank can be found [here](https://github.com/testArmada/flank).
Also read this [medium post](https://medium.com/walmartlabs/flank-smart-test-runner-for-firebase-cf65e1b1eca7).

# Usage

Using Fladle takes 3 steps:

1. Apply the Fladle plugin. Follow instructions [here](https://plugins.gradle.org/plugin/com.osacky.fladle)
``` groovy
buildscript {
  dependencies {
    classpath "gradle.plugin.com.osacky.flank.gradle:fladle:0.2.5"
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
### debugApk
This is the path to the app's debug apk.

### instrumentationApk
This is the path to the app's instrumentation apk.


See the sample module for an example usage.

---

# Publishing

``` bash
./gradlew -b buildSrc/build.gradle.kts publishPlugins
```
