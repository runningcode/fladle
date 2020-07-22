package com.osacky.flank.gradle.integration

import com.google.common.truth.Truth.assertThat
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.junit.Ignore
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
    val result = runConfigCachingBuild("help")

    assertThat(result.output).contains("SUCCESS")

    val secondResult = runConfigCachingBuild("help")

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
    val result = runConfigCachingBuild("writeConfigProps")

    assertThat(result.output).contains("SUCCESS")

    val secondResult = runConfigCachingBuild("writeConfigProps")

    assertThat(secondResult.output).contains("Reusing configuration cache.")
  }

  @Test
  @Ignore
  fun runFlank() {
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
    val result = runConfigCachingBuild("runFlank")

    assertThat(result.output).contains("SUCCESS")

    val secondResult = runConfigCachingBuild("runFlank")

    assertThat(secondResult.output).contains("Reusing configuration cache.")
  }

  private fun runConfigCachingBuild(arg: String): BuildResult {
    return GradleRunner.create()
      .withProjectDir(testProjectRoot.root)
      .withPluginClasspath()
      .forwardOutput()
      .withArguments(arg, "--configuration-cache")
      .build()
  }
}
