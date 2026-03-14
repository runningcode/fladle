import org.gradle.api.tasks.testing.logging.TestLogEvent

group = "com.osacky.flank.gradle"
version = "0.20.0"
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
  alias(libs.plugins.kotlinter)
  alias(libs.plugins.vanniktech.publish)
}

dependencies {
  compileOnly(gradleApi())
  compileOnly(libs.agp) {
    exclude(group = "org.jetbrains.kotlin", module = "kotlin-compiler-embeddable")
    exclude(group = "org.jetbrains.kotlin", module = "kotlin-compiler-runner")
  }
  compileOnly(libs.gradle.enterprise)

  // AGP must be on the runtime classpath so GradleTestKit's withPluginClasspath()
  // can resolve the com.android.application and com.android.library plugins.
  runtimeOnly(libs.agp)

  testImplementation(gradleTestKit())
  testImplementation(libs.junit)
  testImplementation(libs.truth)
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

java {
  withJavadocJar()
  withSourcesJar()
}

mavenPublishing {
  publishToMavenCentral()
  signAllPublications()

  pom {
    name.set("Fladle")
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

// Ensure Java 17 Compatibility. See https://github.com/runningcode/fladle/issues/246
tasks.withType(org.jetbrains.kotlin.gradle.tasks.KotlinCompile::class.java).configureEach {
  compilerOptions {
    jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    languageVersion.set(org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_0)
    apiVersion.set(org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_0)
  }
}

java {
  toolchain {
    languageVersion.set(JavaLanguageVersion.of(17))
  }
}
