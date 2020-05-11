# Testing Snapshot Releases

To test the Fladle snapshot release you have two options:


# Traditional
Root `build.gradle`
```groovy

buildscript {
  repositories {
    maven {
      url "https://oss.sonatype.org/content/repositories/snapshots/"
    }
  }
  dependencies {
    classpath "com.osacky.flank.gradle:fladle:{{ fladle.next_release }}-SNAPSHOT"
  }
}
```

Project `build.gradle`
```groovy
apply plugin: "com.osacky.fladle"
```



# Plugin Management
`settings.gradle`
```groovy
pluginManagement {
    repositories {
        maven {
            url "https://oss.sonatype.org/content/repositories/snapshots/"
        }
        gradlePluginPortal()
    }
}
```

Android application `build.gradle`
```groovy
plugins {
    id "com.osacky.fladle" version "{{ fladle.next_release }}-SNAPSHOT"
}
```
