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
        try {
            GradleRunner.create()
                .withProjectDir(testProjectRoot.root)
                .withPluginClasspath()
                .withGradleVersion("4.8")
                .build()
        } catch (expected: UnexpectedBuildFailure) {
            assertThat(expected).hasMessageThat().contains("Fladle requires at minimum version Gradle 4.9. Detected version Gradle 4.8")
        }
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
            .withGradleVersion("4.9")
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
        .withGradleVersion("5.3.1")
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
        try {

            GradleRunner.create()
                .withProjectDir(testProjectRoot.root)
                .withPluginClasspath()
                .withGradleVersion("5.3.1")
                .withArguments("printYml")
                .build()
        } catch (expected: UnexpectedBuildFailure) {
            assertThat(expected).hasMessageThat().contains("ServiceAccountCredentials in fladle extension not set. https://github.com/runningcode/fladle#serviceaccountcredentials")
        }
    }

    @Test
    fun testMissingApkFailsBuild() {
        writeBuildGradle(
            """plugins {
             |  id "com.osacky.fladle"
             |}
             |fladle {
             |  serviceAccountCredentials = "foo"
             |}
             |""".trimMargin()
        )
        try {

            GradleRunner.create()
                .withProjectDir(testProjectRoot.root)
                .withPluginClasspath()
                .withGradleVersion("5.3.1")
                .withArguments("printYml")
                .build()
        } catch (expected: UnexpectedBuildFailure) {
            assertThat(expected).hasMessageThat().contains("debugApk cannot be null")
        }
    }
}