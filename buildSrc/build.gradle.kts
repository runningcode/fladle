group = "com.osacky.flank.gradle"
version = "0.1-SNAPSHOT"

repositories {
  jcenter()
}

plugins {
  `kotlin-dsl`
  id("java-gradle-plugin")
  id("com.gradle.plugin-publish") version "0.10.0"
  id("org.jmailen.kotlinter") version "1.17.0"
}

dependencies {
  compileOnly(gradleApi())

  implementation("de.undercouch:gradle-download-task:3.4.3")

  testImplementation(gradleTestKit())
}

kotlinter {
  indentSize = 2
}

pluginBundle {
  website = "https://github.com/runningcode/fladle"
  vcsUrl = "https://github.com/runningcode/fladle"
  tags = listOf("flank", "testing", "android")
}

gradlePlugin {
  plugins {
    create("fladle") {
      id = "com.osacky.fladle"
      displayName = "Fladle"
      description = "The Gradle Plugin for Flank"
      implementationClass = "com.osacky.flank.gradle.FlankGradlePlugin"
    }
  }
}
