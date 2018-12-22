group = "com.osacky.flank.gradle"
version = "0.3.3"

repositories {
  google()
  jcenter()
  maven {
    url = uri("https://oss.sonatype.org/content/repositories/snapshots/")
  }
}

plugins {
  `kotlin-dsl`
  id("java-gradle-plugin")
  id("com.gradle.plugin-publish") version "0.10.0"
  id("org.jmailen.kotlinter") version "1.17.0"
}

dependencies {
  compileOnly(gradleApi())
  implementation("com.android.tools.build:gradle:3.2.1")

  implementation("de.undercouch:gradle-download-task:4.0.0-SNAPSHOT")

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
  tags = listOf("flank", "testing", "android")

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
