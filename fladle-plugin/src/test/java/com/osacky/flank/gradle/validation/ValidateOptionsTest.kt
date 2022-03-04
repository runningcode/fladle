package com.osacky.flank.gradle.validation

import com.google.common.truth.Truth.assertThat
import com.osacky.flank.gradle.FladleConfig
import com.osacky.flank.gradle.FlankGradleExtension
import com.osacky.flank.gradle.integration.gradleRunner
import com.osacky.flank.gradle.integration.writeBuildDotGradle
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class ValidateOptionsTest {
  @get:Rule
  var testProjectRoot = TemporaryFolder()

  private val objects = ProjectBuilder.builder().withName("project").build().objects
  private lateinit var config: FladleConfig

  @Before
  fun setUp() {
    testProjectRoot.newFile("flank-gradle-service.json").writeText("{}")
    config = FlankGradleExtension(objects)
  }

  @Test
  fun `should throw an error when unavailable option used`() {
    config.useAverageTestTimeForNewTests.set(true)

    assertThrows(IllegalStateException::class.java) { validateOptionsUsed(config, "20.05.0") }.run {
      assertThat(message).containsMatch("Option useAverageTestTimeForNewTests is available since flank 20.8.4, which is higher than used 20.5.0")
    }
  }

  @Test
  fun `should not throw an error when available option used`() {
    config.testRunnerClass.set("any")

    validateOptionsUsed(config, "20.09.10")
  }

  @Test
  fun `ListProperty check also checks for empty list`() {
    config.testTargetsForShard.set(listOf())
    validateOptionsUsed(config, "20.09.10")
  }

  @Test
  fun `ListProperty isPresent when set`() {
    config.testTargetsForShard.set(listOf("class com.example.Bar"))
    try {
      validateOptionsUsed(config, "20.09.10")
    } catch (e: IllegalStateException) {
      assertThat(e).hasMessageThat().contains("Option testTargetsForShard is available since flank 20.12.0, which is higher than used 20.9.10")
    }
  }

  @Test
  fun `should throw an error when unavailable option used -- multi config`() {
    testProjectRoot.writeBuildDotGradle(
      """
      |plugins {
      |  id "com.osacky.fladle"
      |}
      |
      |fladle {
      |  serviceAccountCredentials = layout.projectDirectory.file("flank-gradle-service.json")
      |  debugApk = "foo.apk"
      |  instrumentationApk = "test.apk"      
      |  flankVersion.set("20.05.0")
      |  configs {
      |    newNetwork {
      |      useAverageTestTimeForNewTests.set(true)
      |    }
      |    noSharding {
      |      disableSharding.set(true)
      |    }
      |  }
      |}
      """.trimMargin()
    )

    val runner = testProjectRoot.gradleRunner()

    runner.withArguments("printYml").build().run {
      assertThat(output).contains("SUCCESS")
    }

    runner.withArguments("printYmlNewNetwork").buildAndFail().run {
      assertThat(output).contains("FAILED")
      assertThat(output).contains("Option useAverageTestTimeForNewTests is available since flank 20.8.4, which is higher than used 20.5.0")
    }

    runner.withArguments("printYmlNoSharding").build().run {
      assertThat(output).contains("SUCCESS")
    }
  }

  @Test
  fun `should not throw an error if none unavailable option used`() {
    testProjectRoot.writeBuildDotGradle(
      """
      |plugins {
      |  id "com.osacky.fladle"
      |}
      |
      |fladle {
      |  serviceAccountCredentials = layout.projectDirectory.file("flank-gradle-service.json")
      |  debugApk = "foo.apk"
      |  instrumentationApk = "test.apk"
      |  flankVersion.set("20.20.20")
      |  configs {
      |    noRecord {
      |      recordVideo.set(false)
      |    }
      |  }
      |}
      """.trimMargin()
    )

    val runner = testProjectRoot.gradleRunner()
    val result = runner.withArguments("printYml").build()

    assertThat(result.output).contains("BUILD SUCCESSFUL")
    assertThat(result.output).contains(
      """
      |gcloud:
      |  app: foo.apk
      |  test: test.apk
      |  device:
      |  - model: NexusLowRes
      |    version: 28
      |
      |  use-orchestrator: false
      |  auto-google-login: false
      |  record-video: true
      |  performance-metrics: true
      |  timeout: 15m
      |  num-flaky-test-attempts: 0
      |
      |flank:
      |  keep-file-path: false
      |  ignore-failed-tests: false
      |  disable-sharding: false
      |  smart-flank-disable-upload: false
      |  legacy-junit-result: false
      |  full-junit-result: false
      |  output-style: single
      """.trimMargin()
    )

    val resultOrange = runner.withArguments("printYmlNoRecord").build()

    assertThat(resultOrange.output).contains("BUILD SUCCESSFUL")
    assertThat(resultOrange.output).contains(
      """
      |gcloud:
      |  app: foo.apk
      |  test: test.apk
      |  device:
      |  - model: NexusLowRes
      |    version: 28
      |
      |  use-orchestrator: false
      |  auto-google-login: false
      |  record-video: false
      |  performance-metrics: true
      |  timeout: 15m
      |  num-flaky-test-attempts: 0
      |
      |flank:
      |  keep-file-path: false
      |  ignore-failed-tests: false
      |  disable-sharding: false
      |  smart-flank-disable-upload: false
      |  legacy-junit-result: false
      |  full-junit-result: false
      |  output-style: single
      """.trimMargin()
    )
  }
}
