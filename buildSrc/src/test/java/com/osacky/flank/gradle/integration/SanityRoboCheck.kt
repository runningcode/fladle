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
  fun checkSanityRoboRunSimpleCase() {
    makeGradleFile(where = testProjectRoot) {
      """
      |plugins {
      |  id "com.osacky.fladle"
      |}
      |
      |fladle {
      |  serviceAccountCredentials = layout.projectDirectory.file("flank-gradle-service.json")
      |  debugApk = "foo.apk"
      |}
    """
    }

    val result = gradleRun {
      arguments = listOf("writeConfigProps", "-PsanityRobo")
      projectDir = testProjectRoot.root
    }

    assertThat(result.output).contains("SUCCESS")

    "build/fladle/flank.yml" readAndCompareWith {
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
    """
    }
  }

  @Test
  fun checkSanityRoboRunWithApksAdded() {
    makeGradleFile(where = testProjectRoot) {
      """
      |plugins {
      |  id "com.osacky.fladle"
      |}
      |
      |fladle {
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
    }

    val result = gradleRun {
      arguments = listOf("writeConfigProps", "-PsanityRobo")
      projectDir = testProjectRoot.root
    }

    assertThat(result.output).contains("SUCCESS")

    "build/fladle/flank.yml" readAndCompareWith {
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
    """
    }
  }

  @Test
  fun checkSanityRoboRunMultipleConfigs() {
    makeGradleFile(where = testProjectRoot) {
      """
      |plugins {
      |  id "com.osacky.fladle"
      |}
      |
      |fladle {
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
      |      testTargets = ['override']
      |      localResultsDir.set('overrideDir')
      |    }
      |  }
      |}
    """
    }

    val result = gradleRun {
      arguments = listOf("writeConfigProps", "-PsanityRobo")
      projectDir = testProjectRoot.root
    }

    assertThat(result.output).contains("SUCCESS")

    "build/fladle/flank.yml" readAndCompareWith {
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
    """
    }

    val resultOrange = gradleRun {
      arguments = listOf("writeConfigPropsOrange", "-PsanityRobo")
      projectDir = testProjectRoot.root
    }

    assertThat(resultOrange.output).contains("SUCCESS")

    "build/fladle/orange/flank.yml" readAndCompareWith {
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
    """
    }
  }

  @Test
  fun checkSanityRoboRunRoboScript() {
    makeGradleFile(where = testProjectRoot) {
      """
      |plugins {
      |  id "com.osacky.fladle"
      |}
      |
      |fladle {
      |  serviceAccountCredentials = layout.projectDirectory.file("flank-gradle-service.json")
      |  debugApk = "foo.apk"
      |  roboScript = "some/path/script.json"
      |}
    """
    }

    val result = gradleRun {
      arguments = listOf("writeConfigProps", "-PsanityRobo")
      projectDir = testProjectRoot.root
    }

    assertThat(result.output).contains("SUCCESS")

    "build/fladle/flank.yml" readAndCompareWith {
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
    """
    }
  }

  @Test
  fun checkSanityRoboRunRoboDirectives() {
    makeGradleFile(where = testProjectRoot) {
      """
      |plugins {
      |  id "com.osacky.fladle"
      |}
      |
      |fladle {
      |  serviceAccountCredentials = layout.projectDirectory.file("flank-gradle-service.json")
      |  debugApk = "foo.apk"
      |  roboDirectives = [
      |    ["click", "button1", ""],
      |    ["ignore", "button2"],
      |    ["text", "field1", "my text"],
      |  ]
      |}
    """
    }

    val result = gradleRun {
      arguments = listOf("writeConfigProps", "-PsanityRobo")
      projectDir = testProjectRoot.root
    }

    assertThat(result.output).contains("SUCCESS")

    "build/fladle/flank.yml" readAndCompareWith {
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
    """
    }
  }

  private infix fun String.readAndCompareWith(block: () -> String) = testProjectRoot
    .root
    .resolve(this)
    .readText()
    .run {
      assertThat(this).contains(block().trimMargin())
    }
}
