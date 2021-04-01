package com.osacky.flank.gradle.integration

import com.google.common.truth.Truth.assertThat
import org.gradle.testkit.runner.GradleRunner
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class ConfigurationCacheTest {

  @get:Rule
  var testProjectRoot = TemporaryFolder()

  fun writeBuildGradle(build: String) {
    val file = testProjectRoot.newFile("build.gradle")
    file.writeText(build)
  }

  @Test
  fun testHelp() {
    writeBuildGradle(
      """plugins {
             |  id "com.osacky.fladle"
             |}""".trimMargin()
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
           |}
           |""".trimMargin()
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
           |""".trimMargin()
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
           |}
           |""".trimMargin()
    )
    testProjectRoot.newFile("flank-gradle-service-account.json").writeText("{ \"project_id\": \"foo\" }")
    val result = configCachingRunner("runFlank").buildAndFail()

    assertThat(result.output).contains("Flank encountered a 403 error when running on project foo")
    assertThat(result.output).contains("Configuration cache entry stored.")

    val secondResult = configCachingRunner("runFlank").buildAndFail()

    assertThat(secondResult.output).contains("Flank encountered a 403 error when running on project foo")
    assertThat(secondResult.output).contains("Reusing configuration cache.")
  }

  private fun configCachingRunner(arg: String): GradleRunner {
    return GradleRunner.create()
      .withProjectDir(testProjectRoot.root)
      .withPluginClasspath()
      .forwardOutput()
      .withArguments(arg, "--configuration-cache")
  }
}
