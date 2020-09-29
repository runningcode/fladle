package com.osacky.flank.gradle.integration

import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

private fun baseConfigMessage(option: String) = "Incorrect [base] configuration. [$option] can't be used together with sanityRobo."
private fun additionalConfigMessage(option: String, name: String) = "Incorrect [$name] configuration. [$option] can't be used together with sanityRobo. If you want to launch robo test run without robo script place only sanityRoboRun() into [$name] configuration"

class SanityRoboCheck {
  @get:Rule
  var testProjectRoot = TemporaryFolder()

  @Before
  fun setUp() = testProjectRoot.newFile("flank-gradle-service.json").writeText("{}")

  @Test
  fun `sanityRobo - should throw an error if instrumentationApk set`() {
    makeGradleFile(
      where = testProjectRoot,
      buildScript =
        """
      |plugins {
      |  id "com.osacky.fladle"
      |}
      |
      |fladle {
      |  debugApk = "debug.apk"
      |  sanityRobo = true
      |  serviceAccountCredentials = layout.projectDirectory.file("flank-gradle-service.json")
      |  instrumentationApk = "test.apk"
      |  configs {
      |    sanity {
      |      sanityRobo.set(true)
      |      makeSanityRun()
      |    }
      |  }
      |}
    """
    )

    val result = failedGradleRun(
      arguments = listOf("printYml"),
      projectDir = testProjectRoot.root
    )

    assertThat(result.output).contains("FAILED")
    assertThat(result.output).contains(baseConfigMessage("instrumentationApk"))

    val resultSanity = failedGradleRun(
      arguments = listOf("printYmlSanity"),
      projectDir = testProjectRoot.root
    )

    assertThat(resultSanity.output).contains("FAILED")
    assertThat(result.output).contains(baseConfigMessage("instrumentationApk"))
  }

  @Test
  fun `sanityRobo - should throw an error if roboScript set`() {
    makeGradleFile(
      where = testProjectRoot,
      buildScript =
        """
      |plugins {
      |  id "com.osacky.fladle"
      |}
      |
      |fladle {
      |  debugApk = "debug.apk"
      |  sanityRobo = true
      |  serviceAccountCredentials = layout.projectDirectory.file("flank-gradle-service.json")
      |  roboScript = "some/path/script.json"
      |}
    """
    )

    val result = failedGradleRun(
      arguments = listOf("printYml"),
      projectDir = testProjectRoot.root
    )

    assertThat(result.output).contains("FAILED")
    assertThat(result.output).contains(baseConfigMessage("roboScript"))
  }

  @Test
  fun `sanityRobo - should throw an error if roboDirectives set`() {
    makeGradleFile(
      where = testProjectRoot,
      buildScript =
        """
      |plugins {
      |  id "com.osacky.fladle"
      |}
      |
      |fladle {
      |  debugApk = "debug.apk"
      |  sanityRobo = true
      |  serviceAccountCredentials = layout.projectDirectory.file("flank-gradle-service.json")
      |  roboDirectives = [
      |    ["click", "button1", ""],
      |    ["ignore", "button2"],
      |    ["text", "field1", "my text"],
      |  ]
      |}
    """
    )

    val result = failedGradleRun(
      arguments = listOf("printYml"),
      projectDir = testProjectRoot.root
    )

    assertThat(result.output).contains("FAILED")
    assertThat(result.output).contains(baseConfigMessage("roboDirectives"))
  }

  @Test
  fun `sanityRobo - should throw an error if additionalTestApks set`() {
    makeGradleFile(
      where = testProjectRoot,
      buildScript =
        """
      |plugins {
      |  id "com.osacky.fladle"
      |}
      |
      |fladle {
      |  debugApk = "debug.apk"
      |  sanityRobo = true
      |  serviceAccountCredentials = layout.projectDirectory.file("flank-gradle-service.json")
      |  additionalTestApks = [
      |    "- app: debug2.apk",
      |    "  test: test2.apk",
      |    "- test: test3.apk"
      |  ]
      |}
    """
    )

    val result = failedGradleRun(
      arguments = listOf("printYml"),
      projectDir = testProjectRoot.root
    )

    assertThat(result.output).contains("FAILED")
    assertThat(result.output).contains(baseConfigMessage("additionalTestApks"))
  }

  @Test
  fun `sanityRobo - should throw an error if roboScript set (multiple config)`() {
    makeGradleFile(
      where = testProjectRoot,
      buildScript =
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
      |    sanity {
      |      sanityRobo.set(true)
      |      roboScript.set("path/to/script.json")
      |    }
      |  }
      |}
    """
    )

    val expectedMessage = additionalConfigMessage("roboScript", "sanity")

    val result = failedGradleRun(
      arguments = listOf("printYml"),
      projectDir = testProjectRoot.root
    )

    assertThat(result.output).contains("FAILED")
    assertThat(result.output).contains(expectedMessage)

    val resultOrange = failedGradleRun(
      arguments = listOf("printYmlSanity"),
      projectDir = testProjectRoot.root
    )

    assertThat(resultOrange.output).contains("FAILED")
    assertThat(resultOrange.output).contains(expectedMessage)
  }

  @Test
  fun `sanityRobo - should print correct config yamls (inner config is sanity run)`() {
    makeGradleFile(
      where = testProjectRoot,
      buildScript =
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
      |      sanityRoboRun()
      |    }
      |  }
      |}
    """
    )

    val result = gradleRun(
      arguments = listOf("printYml"),
      projectDir = testProjectRoot.root
    )

    assertThat(result.output).contains("SUCCESS")
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
      |  additional-app-test-apks:
      |    - app: debug2.apk
      |      test: test2.apk
      |    - test: test3.apk
      |  ignore-failed-tests: false
      |  disable-sharding: false
      |  smart-flank-disable-upload: false
      |  legacy-junit-result: false
      |  full-junit-result: false
      |  output-style: single
    """.trimMargin()
    )

    val resultOrange = gradleRun(
      arguments = listOf("printYmlOrange"),
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
  fun `sanityRobo - should print correct config yamls (base config is sanity run)`() {
    makeGradleFile(
      where = testProjectRoot,
      buildScript =
        """
      |plugins {
      |  id "com.osacky.fladle"
      |}
      |
      |fladle {
      |  serviceAccountCredentials = layout.projectDirectory.file("flank-gradle-service.json")
      |  debugApk = "foo.apk"
      |  sanityRobo = true
      |  configs {
      |    orange {
      |      instrumentationApk.set("test.apk")
      |      additionalTestApks.set(project.provider { [
      |        "- app: debug2.apk",
      |        "  test: test2.apk",
      |        "- test: test3.apk"
      |      ] })
      |    }
      |  }
      |}
    """
    )

    val result = gradleRun(
      arguments = listOf("printYml"),
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
      arguments = listOf("printYmlOrange"),
      projectDir = testProjectRoot.root
    )

    assertThat(resultOrange.output).contains("SUCCESS")
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
      |  record-video: true
      |  performance-metrics: true
      |  timeout: 15m
      |  num-flaky-test-attempts: 0
      |
      |flank:
      |  keep-file-path: false
      |  additional-app-test-apks:
      |    - app: debug2.apk
      |      test: test2.apk
      |    - test: test3.apk
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
