# Quick Start

Using Fladle takes 3 steps:

1. Apply the Fladle plugin. Follow instructions [here](https://plugins.gradle.org/plugin/com.osacky.fladle)
``` groovy
buildscript {
  dependencies {
    classpath "com.osacky.flank.gradle:fladle:0.9.1"
  }
}

apply plugin: "com.osacky.fladle"
```
2. Configure the Fladle extension.
``` groovy
fladle {
    serviceAccountCredentials = project.layout.projectDirectory.file("flank-gradle-service-account.json")
}
```
3. Run the Flank Gradle task.
``` bash
./gradlew runFlank
`
