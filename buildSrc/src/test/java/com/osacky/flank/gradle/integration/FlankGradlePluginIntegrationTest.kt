package com.osacky.flank.gradle.integration

import com.google.common.truth.Truth.assertThat
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.UnexpectedBuildFailure
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class FlankGradlePluginIntegrationTest {

  @get:Rule
  var testProjectRoot = TemporaryFolder()

  val minSupportGradleVersion = "5.5"
  val oldVersion = "5.3.1"

  fun writeBuildGradle(build: String) {
    val file = testProjectRoot.newFile("build.gradle")
    file.writeText(build)
  }

  @Test
  fun testLowGradleVersionFailsBuild() {
    writeBuildGradle(
        """plugins {
             |  id "com.osacky.fladle"
             |}""".trimMargin()
    )
    val result = GradleRunner.create()
        .withProjectDir(testProjectRoot.root)
        .withPluginClasspath()
        .withGradleVersion(oldVersion)
        .buildAndFail()
    assertThat(result.output).contains("Fladle requires at minimum version Gradle 5.4. Detected version Gradle 5.3.1")
  }

  @Test
  fun testGradleSixZero() {
    writeBuildGradle(
        """plugins {
             |  id "com.osacky.fladle"
             |}""".trimMargin()
    )
    val result = GradleRunner.create()
        .withProjectDir(testProjectRoot.root)
        .withPluginClasspath()
        .withGradleVersion("6.0")
        .build()

    assertThat(result.output).contains("SUCCESS")
  }

  @Test
  fun testMinSupportedGradleVersionWorks() {
    writeBuildGradle(
        """plugins {
             |  id "com.osacky.fladle"
             |}""".trimMargin()
    )
    GradleRunner.create()
        .withProjectDir(testProjectRoot.root)
        .withPluginClasspath()
        .withGradleVersion(minSupportGradleVersion)
        .build()
  }

  @Test
  fun testMissingServiceAccountWithProjectId() {
    writeBuildGradle(
        """plugins {
             |  id "com.osacky.fladle"
             |}
             |
             |fladle {
             |  projectId = "foo-project"
             |  debugApk = "foo"
             |  instrumentationApk = "fakeInstrument.apk"
             |}""".trimMargin()
    )
    GradleRunner.create()
        .withProjectDir(testProjectRoot.root)
        .withPluginClasspath()
        .withGradleVersion(minSupportGradleVersion)
        .withArguments("printYml")
        .build()
  }

  @Test
  fun testMissingServiceAccountFailsBuild() {
    writeBuildGradle(
        """plugins {
             |  id "com.osacky.fladle"
             |}
             |
             |fladle {
             |  debugApk = "foo"
             |}""".trimMargin()
    )
    val result = GradleRunner.create()
        .withProjectDir(testProjectRoot.root)
        .withPluginClasspath()
        .withGradleVersion(minSupportGradleVersion)
        .withArguments("printYml")
        .buildAndFail()
    assertThat(result.output).contains("ServiceAccountCredentials in fladle extension not set. https://github.com/runningcode/fladle#serviceaccountcredentials")
  }

  @Test
  fun testMissingApkFailsBuild() {
    writeBuildGradle(
        """plugins {
             |  id "com.osacky.fladle"
             |}
             |fladle {
             |  serviceAccountCredentials = project.layout.projectDirectory.file("foo")
             |}
             |""".trimMargin()
    )
    try {

      GradleRunner.create()
          .withProjectDir(testProjectRoot.root)
          .withPluginClasspath()
          .withGradleVersion(minSupportGradleVersion)
          .withArguments("printYml")
          .build()
    } catch (expected: UnexpectedBuildFailure) {
      assertThat(expected).hasMessageThat().contains("debugApk must be specified")
    }
  }
}
