package com.osacky.flank.gradle.integration

import com.google.common.truth.Truth.assertThat
import org.gradle.testkit.runner.GradleRunner
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class FulladlePluginIntegrationTest {
  @get:Rule
  var testProjectRoot = TemporaryFolder()

  fun writeBuildGradle(build: String) {
    val file = testProjectRoot.newFile("build.gradle")
    file.writeText(build)
  }

  @Test
  fun fladleSmokeTest() {
    writeBuildGradle(
      """plugins {
             |  id "com.osacky.fulladle"
             |}""".trimMargin()
    )
    val result = GradleRunner.create()
      .withProjectDir(testProjectRoot.root)
      .withPluginClasspath()
      .withGradleVersion("6.0")
      .build()
    assertThat(result.output).contains("SUCCESS")
  }
}
