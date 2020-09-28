package com.osacky.flank.gradle.integration

import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class SanityRoboCheck {
  @get:Rule
  var testProjectRoot = TemporaryFolder()

  @Before
  fun setUp() = testProjectRoot.newFile("flank-gradle-service.json").writeText("{}")

  @Test
  fun checkSanityRoboRunWithProjectProperty() {
    makeGradleFile(
      where = testProjectRoot,
      buildScript =
        """
      |plugins {
      |  id "com.osacky.fladle"
      |}
      |
      |fladle {
      |  sanityRobo = project.hasProperty('sanityRobo')
      |  serviceAccountCredentials = layout.projectDirectory.file("flank-gradle-service.json")
      |  debugApk = "foo.apk"
      |}
    """
    )

    val result = gradleRun(
      arguments = listOf("printYml", "-PsanityRobo"),
      projectDir = testProjectRoot.root
    )

    assertThat(result.output).contains("SUCCESS")
    assertThat(result.output).contains(
      """
      |gcloud:
      |  app: foo.apk
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
  }

  @Test
  fun checkSanityRoboRunWithProjectPropertySetAsExtensionProperty() {
    makeGradleFile(
      where = testProjectRoot,
      buildScript =
        """
      |plugins {
      |  id "com.osacky.fladle"
      |}
      |
      |fladle {
      |  sanityRobo = true
      |  serviceAccountCredentials = layout.projectDirectory.file("flank-gradle-service.json")
      |  debugApk = "foo.apk"
      |}
    """
    )

    val result = gradleRun(
      arguments = listOf("printYml", "-PsanityRobo"),
      projectDir = testProjectRoot.root
    )

    assertThat(result.output).contains("SUCCESS")
    assertThat(result.output).contains(
      """
      |gcloud:
      |  app: foo.apk
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
  }

  @Test
  fun checkSanityRoboRunWithApksAdded() {
    makeGradleFile(
      where = testProjectRoot,
      buildScript =
        """
      |plugins {
      |  id "com.osacky.fladle"
      |}
      |
      |fladle {
      |  sanityRobo = project.hasProperty('sanityRobo')
      |  serviceAccountCredentials = layout.projectDirectory.file("flank-gradle-service.json")
      |  debugApk = "foo.apk"
      |  instrumentationApk = "test.apk"
      |  additionalTestApks = [
      |    "- app: debug2.apk",
      |    "  test: test2.apk",
      |    "- test: test3.apk"
      |  ]
      |}
    """
    )

    val result = gradleRun(
      arguments = listOf("printYml", "-PsanityRobo"),
      projectDir = testProjectRoot.root
    )

    assertThat(result.output).contains("SUCCESS")
    assertThat(result.output).contains(
      """
      |gcloud:
      |  app: foo.apk
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
  }

  @Test
  fun checkSanityRoboRunMultipleConfigs() {
    makeGradleFile(
      where = testProjectRoot,
      buildScript =
        """
      |plugins {
      |  id "com.osacky.fladle"
      |}
      |
      |fladle {
      |  sanityRobo = project.hasProperty('sanityRobo')
      |  serviceAccountCredentials = layout.projectDirectory.file("flank-gradle-service.json")
      |  debugApk = "foo.apk"
      |  instrumentationApk = "test.apk"
      |  additionalTestApks = [
      |    "- app: debug2.apk",
      |    "  test: test2.apk",
      |    "- test: test3.apk"
      |  ]
      |  configs {
      |    orange {
      |      testTargets.set(project.provider { ['override'] })
      |      localResultsDir.set('overrideDir')
      |    }
      |  }
      |}
    """
    )

    val result = gradleRun(
      arguments = listOf("printYml", "-PsanityRobo"),
      projectDir = testProjectRoot.root
    )

    assertThat(result.output).contains("SUCCESS")
    assertThat(result.output).contains(
      """
      |gcloud:
      |  app: foo.apk
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

    val resultOrange = gradleRun(
      arguments = listOf("printYmlOrange", "-PsanityRobo"),
      projectDir = testProjectRoot.root
    )

    assertThat(resultOrange.output).contains("SUCCESS")
    assertThat(resultOrange.output).contains(
      """
      |gcloud:
      |  app: foo.apk
      |  device:
      |  - model: NexusLowRes
      |    version: 28
      |
      |  use-orchestrator: false
      |  auto-google-login: false
      |  record-video: true
      |  performance-metrics: true
      |  timeout: 15m
      |  test-targets:
      |  - override
      |  num-flaky-test-attempts: 0
      |
      |flank:
      |  keep-file-path: false
      |  ignore-failed-tests: false
      |  disable-sharding: false
      |  smart-flank-disable-upload: false
      |  local-result-dir: overrideDir
      |  legacy-junit-result: false
      |  full-junit-result: false
      |  output-style: single
    """.trimMargin()
    )
  }

  @Test
  fun checkSanityRoboRunRoboScript() {
    makeGradleFile(
      where = testProjectRoot,
      buildScript =
        """
      |plugins {
      |  id "com.osacky.fladle"
      |}
      |
      |fladle {
      |  sanityRobo = project.hasProperty('sanityRobo')
      |  serviceAccountCredentials = layout.projectDirectory.file("flank-gradle-service.json")
      |  debugApk = "foo.apk"
      |  roboScript = "some/path/script.json"
      |}
    """
    )

    val result = gradleRun(
      arguments = listOf("printYml", "-PsanityRobo"),
      projectDir = testProjectRoot.root
    )

    assertThat(result.output).contains("SUCCESS")
    assertThat(result.output).contains(
      """
      |gcloud:
      |  app: foo.apk
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
  }

  @Test
  fun checkSanityRoboRunRoboDirectives() {
    makeGradleFile(
      where = testProjectRoot,
      buildScript =
        """
      |plugins {
      |  id "com.osacky.fladle"
      |}
      |
      |fladle {
      |  sanityRobo = project.hasProperty('sanityRobo')
      |  serviceAccountCredentials = layout.projectDirectory.file("flank-gradle-service.json")
      |  debugApk = "foo.apk"
      |  roboDirectives = [
      |    ["click", "button1", ""],
      |    ["ignore", "button2"],
      |    ["text", "field1", "my text"],
      |  ]
      |}
    """
    )

    val result = gradleRun(
      arguments = listOf("printYml", "-PsanityRobo"),
      projectDir = testProjectRoot.root
    )

    assertThat(result.output).contains("SUCCESS")
    assertThat(result.output).contains(
      """
      |gcloud:
      |  app: foo.apk
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
  }
}
