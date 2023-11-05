import com.vanniktech.maven.publish.SonatypeHost
import org.gradle.api.tasks.testing.logging.TestLogEvent

group = "com.osacky.flank.gradle"
version = "0.17.5-SNAPSHOT"
description = "Easily Scale your Android Instrumentation Tests with Firebase Test Lab with Flank"

repositories {
  google()
  mavenCentral()
  gradlePluginPortal()
}

plugins {
  `kotlin-dsl`
  `java-gradle-plugin`
  alias(libs.plugins.gradle.plugin.publish)
  alias(libs.plugins.vanniktech.maven.publish)
  alias(libs.plugins.kotlinter)
  `maven-publish`
  signing
}

// See https://github.com/slackhq/keeper/pull/11#issuecomment-579544375 for context
val isReleaseMode : Boolean = hasProperty("fladle.releaseMode")

dependencies {
  compileOnly(gradleApi())
  if (isReleaseMode) {
    compileOnly(libs.agp)
  } else {
    implementation(libs.agp)
  }
  compileOnly(libs.gradle.enterprise)

  testImplementation(gradleTestKit())
  testImplementation(libs.junit)
  testImplementation(libs.truth)
}

kotlinter {
  indentSize = 2
}

gradlePlugin {
  website.set("https://github.com/runningcode/fladle")
  vcsUrl.set("https://github.com/runningcode/fladle")
  plugins {
    create("fladle") {
      id = "com.osacky.fladle"
      displayName = "Fladle"
      description = project.description
      implementationClass = "com.osacky.flank.gradle.FlankGradlePlugin"
      tags.set(listOf("flank", "testing", "android", "fladle"))
    }
    create("fulladle") {
      id = "com.osacky.fulladle"
      displayName = "Fulladle"
      description = project.description
      implementationClass = "com.osacky.flank.gradle.FulladlePlugin"
      tags.set(listOf("flank", "testing", "android", "fladle"))
    }
  }
}

tasks.register<Jar>("sourcesJar") {
  from(sourceSets.main.get().allSource)
  archiveClassifier.set("sources")
}

tasks.register<Jar>("javadocJar") {
  from(tasks.javadoc)
  archiveClassifier.set("javadoc")
}

mavenPublishing {
    publishToMavenCentral(SonatypeHost.DEFAULT)
    signAllPublications()
    pom {
        url.set("https://github.com/runningcode/fladle")
        licenses {
            license {
                name.set("The Apache License, Version 2.0")
                url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
            }
        }
        developers {
            developer {
                id.set("runningcode")
                name.set("Nelson Osacky")
            }
        }
        scm {
            connection.set("scm:git:git://github.com/runningcode/fladle.git")
            developerConnection.set("scm:git:ssh://github.com/runningcode/fladle.git")
            url.set("https://github.com/runningcode/fladle")
        }
    }
}

tasks.withType(Test::class.java).configureEach {
  // Test fixtures are stored in here so we should re-run tests if the test projects change.
  inputs.dir("src/test/resources")
  testLogging {
    events = setOf(TestLogEvent.SKIPPED, TestLogEvent.FAILED, TestLogEvent.PASSED)
  }
}

tasks.withType(ValidatePlugins::class.java).configureEach {
  failOnWarning.set(true)
  enableStricterValidation.set(true)
}

// Ensure Java 8 Compatibility. See https://github.com/runningcode/fladle/issues/246
tasks.withType(org.jetbrains.kotlin.gradle.tasks.KotlinCompile::class.java).configureEach {
  kotlinOptions {
    jvmTarget = "1.8"
    languageVersion = "1.4"
    apiVersion = "1.4"
  }
}

java {
  toolchain {
    languageVersion.set(JavaLanguageVersion.of(8))
  }
}
