import org.gradle.api.tasks.testing.logging.TestLogEvent

group = "com.osacky.flank.gradle"
version = "0.16.0"
description = "Easily Scale your Android Instrumentation Tests with Firebase Test Lab with Flank"

repositories {
  google()
  mavenCentral()
  jcenter()
}

plugins {
  `kotlin-dsl`
  `java-gradle-plugin`
  id("com.gradle.plugin-publish") version "0.14.0"
  id("org.jmailen.kotlinter") version "3.4.0"
  `maven-publish`
  signing
}

apply(from = "../gradle/scans-quality-check.gradle")

// See https://github.com/slackhq/keeper/pull/11#issuecomment-579544375 for context
val isReleaseMode : Boolean = hasProperty("fladle.releaseMode")

dependencies {
  compileOnly(gradleApi())
  if (isReleaseMode) {
    compileOnly("com.android.tools.build:gradle:4.1.3")
  } else {
    implementation("com.android.tools.build:gradle:4.1.3")
  }

  testImplementation(gradleTestKit())
  testImplementation("junit:junit:4.13.2")
  testImplementation("com.google.truth:truth:1.1.2")
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
    groupId = project.group.toString()
  }
}

gradlePlugin {
  plugins {
    create("fladle") {
      id = "com.osacky.fladle"
      displayName = "Fladle"
      description = project.description
      implementationClass = "com.osacky.flank.gradle.FlankGradlePlugin"
    }
    create("fulladle") {
      id = "com.osacky.fulladle"
      displayName = "Fulladle"
      description = project.description
      implementationClass = "com.osacky.flank.gradle.FulladlePlugin"
    }
  }
}

kotlinDslPluginOptions {
  experimentalWarning.set(false)
}

tasks.register<Jar>("sourcesJar") {
  from(sourceSets.main.get().allSource)
  archiveClassifier.set("sources")
}

tasks.register<Jar>("javadocJar") {
  from(tasks.javadoc)
  archiveClassifier.set("javadoc")
}

val isReleaseBuild : Boolean = !version.toString().endsWith("SNAPSHOT")

val sonatypeUsername : String? by project
val sonatypePassword : String? by project

publishing {
  repositories {
    repositories {
      maven {
        val releasesRepoUrl = uri("https://oss.sonatype.org/service/local/staging/deploy/maven2/")
        val snapshotsRepoUrl = uri("https://oss.sonatype.org/content/repositories/snapshots/")
        url = if (isReleaseBuild) releasesRepoUrl else snapshotsRepoUrl
        credentials {
          username = sonatypeUsername
          password = sonatypePassword
        }
      }
    }
  }
  publications {
    afterEvaluate {
      named<MavenPublication>("fladlePluginMarkerMaven") {
        signing.sign(this)
        pom.configureForFladle("Fladle")
      }

      named<MavenPublication>("pluginMaven") {
        artifact(tasks["sourcesJar"])
        artifact(tasks["javadocJar"])
        signing.sign(this)
        pom.configureForFladle("Fladle")
      }
      named<MavenPublication>("fulladlePluginMarkerMaven") {
        signing.sign(this)
        pom.configureForFladle("Fulladle")
      }
    }
  }
}

signing {
  isRequired = isReleaseBuild
}

fun org.gradle.api.publish.maven.MavenPom.configureForFladle(pluginName: String) {
  name.set(pluginName)
  description.set(project.description)
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
  kotlinOptions.jvmTarget = "1.8"
}

java {
  toolchain {
    languageVersion.set(JavaLanguageVersion.of(8))
  }
}
