group = "com.osacky.flank.gradle"
version = "0.6.4"

repositories {
  google()
  mavenCentral()
}

plugins {
  `kotlin-dsl`
  id("java-gradle-plugin")
  id("com.gradle.plugin-publish") version "0.10.1"
  id("org.jmailen.kotlinter") version "1.25.2"
}

dependencies {
  compileOnly(gradleApi())
  implementation("com.android.tools.build:gradle:3.4.2")

  testImplementation(gradleTestKit())
  testImplementation("junit:junit:4.12")
  testImplementation("com.google.truth:truth:1.0")
}

kotlinter {
  indentSize = 2
}

pluginBundle {
  website = "https://github.com/runningcode/fladle"
  vcsUrl = "https://github.com/runningcode/fladle"
  tags = listOf("flank", "testing", "android", "fladle")

  mavenCoordinates {
    artifactId = "fladle"
    groupId = group
  }
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

kotlinDslPluginOptions {
  experimentalWarning.set(false)
}
