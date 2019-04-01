group = "com.osacky.flank.gradle"
version = "0.5.1"

repositories {
  google()
  jcenter()
}

plugins {
  `kotlin-dsl`
  id("java-gradle-plugin")
  id("com.gradle.plugin-publish") version "0.10.1"
  id("org.jmailen.kotlinter") version "1.21.0"
}

dependencies {
  compileOnly(gradleApi())
  implementation("com.android.tools.build:gradle:3.3.2")

  testImplementation(gradleTestKit())
  testImplementation("junit:junit:4.12")
  testImplementation("com.google.truth:truth:0.42")
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
