package com.osacky.flank.gradle.integration

import com.google.common.truth.Truth.assertThat
import org.gradle.testkit.runner.GradleRunner
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class ConfigurationCacheTest {
  @get:Rule
  var testProjectRoot = TemporaryFolder()

  val agpDependency: String = "com.android.tools.build:gradle:9.0.1"

  fun writeBuildGradle(build: String) {
    val file = testProjectRoot.newFile("build.gradle")
    file.writeText(build)
  }

  @Test
  fun testHelp() {
    writeBuildGradle(
      """plugins {
             |  id "com.osacky.fladle"
             |}
      """.trimMargin(),
    )
    val result = configCachingRunner("help").build()

    assertThat(result.output).contains("SUCCESS")

    val secondResult = configCachingRunner("help").build()

    assertThat(secondResult.output).contains("Reusing configuration cache.")
  }

  @Test
  fun testPrintYml() {
    writeBuildGradle(
      """|plugins {
           |  id "com.osacky.fladle"
           |}
           |
           |fladle {
           |  serviceAccountCredentials = project.layout.projectDirectory.file("flank-gradle-service-account.json")
           |  debugApk = "debug.apk"
           |  instrumentationApk = "test.apk"
           |  localResultsDir = "foo"
           |}
           |
      """.trimMargin(),
    )
    testProjectRoot.newFile("flank-gradle-service-account.json").writeText("{}")
    val result = configCachingRunner("writeConfigProps").build()

    assertThat(result.output).contains("SUCCESS")

    val secondResult = configCachingRunner("writeConfigProps").build()

    assertThat(secondResult.output).contains("Reusing configuration cache.")
  }

  @Test
  fun flankDoctor() {
    writeBuildGradle(
      """|plugins {
           |  id "com.osacky.fladle"
           |}
           |
           |repositories {
           |  mavenCentral()
           |}
           |
           |fladle {
           |  // Flank Version is pinned at 20.08.3 because 20.08.4 introduce a backward incompatible change that causes the doctor to fail.
           |  // We should eventually format the version code of the device as a string but that would make Fladle backward incompatible.
           |  flankVersion = "20.08.3"
           |  serviceAccountCredentials = project.layout.projectDirectory.file("flank-gradle-service-account.json")
           |  debugApk = "debug.apk"
           |  instrumentationApk = "test.apk"
           |}
           |
      """.trimMargin(),
    )
    testProjectRoot.newFile("flank-gradle-service-account.json").writeText("{}")
    val result = configCachingRunner("flankDoctor").build()

    assertThat(result.output).contains("SUCCESS")

    val secondResult = configCachingRunner("flankDoctor").build()

    assertThat(secondResult.output).contains("Reusing configuration cache.")
  }

  @Test
  fun runFlank() {
    writeBuildGradle(
      """|plugins {
           |  id "com.osacky.fladle"
           |}
           |
           |repositories {
           |  mavenCentral()
           |}
           |
           |fladle {
           |  serviceAccountCredentials = project.layout.projectDirectory.file("flank-gradle-service-account.json")
           |  debugApk = "debug.apk"
           |  instrumentationApk = "test.apk"
           |  localResultsDir = "foo"
           |}
           |
      """.trimMargin(),
    )

    val settings = testProjectRoot.newFile("settings.gradle")
    settings.writeText(
      """
      plugins {
        id 'com.gradle.develocity' version '4.3'
      }
      """.trimIndent(),
    )
    testProjectRoot.newFile("flank-gradle-service-account.json").writeText("{ \"project_id\": \"foo\" }")
    val result = configCachingRunner("runFlank").buildAndFail()

    assertThat(result.output).contains("Error: Failed to read service account credential.")
    assertThat(result.output).contains("Configuration cache entry stored.")

    val secondResult = configCachingRunner("runFlank").buildAndFail()

    assertThat(secondResult.output).contains("Error: Failed to read service account credential.")
    assertThat(secondResult.output).contains("Reusing configuration cache.")
  }

  @Test
  fun fulladleMultiModuleWithConfigurationCache() {
    val appFixture = "android-project"
    val libraryFixture = "android-library-project"
    testProjectRoot.newFile("settings.gradle").writeText(
      """
      include '$appFixture'
      include '$libraryFixture'

      dependencyResolutionManagement {
        repositories {
          mavenCentral()
          google()
        }
      }
      """.trimIndent(),
    )
    testProjectRoot.setupFixture(appFixture)
    testProjectRoot.setupFixture(libraryFixture)

    writeBuildGradle(
      """
      buildscript {
          repositories {
              google()
          }
          dependencies {
              classpath '$agpDependency'
          }
      }
      plugins {
        id "com.osacky.fulladle"
      }
      fladle {
        serviceAccountCredentials = project.layout.projectDirectory.file("android-project/flank-gradle-5cf02dc90531.json")
      }
      """.trimIndent(),
    )

    val result = configCachingRunner("printYml").build()

    assertThat(result.output).contains("SUCCESS")
    assertThat(result.output).contains("additional-app-test-apks:")

    val secondResult = configCachingRunner("printYml").build()

    assertThat(secondResult.output).contains("Reusing configuration cache.")
    assertThat(secondResult.output).contains("SUCCESS")
  }

  @Test
  fun fulladleSettingsPluginMultiModuleWithConfigurationCache() {
    val appFixture = "android-project"
    val libraryFixture = "android-library-project"
    testProjectRoot.newFile("settings.gradle").writeText(
      """
      plugins {
        id "com.osacky.fulladle.settings"
      }
      include '$appFixture'
      include '$libraryFixture'

      dependencyResolutionManagement {
        repositories {
          mavenCentral()
          google()
        }
      }
      """.trimIndent(),
    )
    testProjectRoot.setupFixture(appFixture)
    testProjectRoot.setupFixture(libraryFixture)

    writeBuildGradle(
      """
      buildscript {
          repositories {
              google()
          }
          dependencies {
              classpath '$agpDependency'
          }
      }
      plugins {
        id "com.osacky.fulladle"
      }
      fladle {
        serviceAccountCredentials = project.layout.projectDirectory.file("android-project/flank-gradle-5cf02dc90531.json")
      }
      """.trimIndent(),
    )

    val result = configCachingRunner("printYml").build()

    assertThat(result.output).contains("SUCCESS")
    assertThat(result.output).contains("additional-app-test-apks:")

    val secondResult = configCachingRunner("printYml").build()

    assertThat(secondResult.output).contains("Reusing configuration cache.")
    assertThat(secondResult.output).contains("SUCCESS")
  }

  private fun configCachingRunner(arg: String): GradleRunner =
    GradleRunner
      .create()
      .withProjectDir(testProjectRoot.root)
      .withPluginClasspath()
      .forwardOutput()
      .withArguments(arg, "--configuration-cache")
}
