package com.osacky.flank.gradle.integration

import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

private fun baseConfigMessage(option: String) = "Incorrect [base] configuration. [$option] can't be used together with sanityRobo."

private fun additionalConfigMessage(
  option: String,
  name: String,
) = "Incorrect [$name] configuration. [$option] can't be used together with sanityRobo." +
  "To configure sanityRobo, add clearPropertiesForSanityRobo() to the [$name] configuration"

class SanityRoboTest {
  @get:Rule
  var testProjectRoot = TemporaryFolder()

  @Before
  fun setUp() = testProjectRoot.newFile("flank-gradle-service.json").writeText("{}")

  @Test
  fun `sanityRobo - should throw an error if instrumentationApk set`() {
    testProjectRoot.writeBuildDotGradle(
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
      |      clearPropertiesForSanityRobo()
      |    }
      |  }
      |}
      """.trimMargin(),
    )

    val runner = testProjectRoot.gradleRunner()
    val result = runner.withArguments("printYml").buildAndFail()

    assertThat(result.output).contains("FAILED")
    assertThat(result.output).contains(baseConfigMessage("instrumentationApk"))

    val resultSanity = runner.withArguments("printYmlSanity").build()

    assertThat(resultSanity.output).contains("SUCCESS")
  }

  @Test
  fun `sanityRobo - should throw an error if roboScript set`() {
    testProjectRoot.writeBuildDotGradle(
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
      """.trimMargin(),
    )

    val result = testProjectRoot.gradleRunner().withArguments("printYml").buildAndFail()

    assertThat(result.output).contains("FAILED")
    assertThat(result.output).contains(baseConfigMessage("roboScript"))
  }

  @Test
  fun `sanityRobo - should throw an error if roboDirectives set`() {
    testProjectRoot.writeBuildDotGradle(
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
        """.trimMargin(),
    )

    val result = testProjectRoot.gradleRunner().withArguments("printYml").buildAndFail()

    assertThat(result.output).contains("FAILED")
    assertThat(result.output).contains(baseConfigMessage("roboDirectives"))
  }

  @Test
  fun `sanityRobo - should throw an error if additionalTestApks set`() {
    testProjectRoot.writeBuildDotGradle(
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
        """.trimMargin(),
    )

    val result = testProjectRoot.gradleRunner().withArguments("printYml").buildAndFail()

    assertThat(result.output).contains("FAILED")
    assertThat(result.output).contains(baseConfigMessage("additionalTestApks"))
  }

  @Test
  fun `sanityRobo - should throw an error if roboScript set (multiple config)`() {
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
      |  additionalTestApks = [
      |    "- app: debug2.apk",
      |    "  test: test2.apk",
      |    "- test: test3.apk"
      |  ]
      |  configs {
      |    sanity {
      |      clearPropertiesForSanityRobo()
      |      roboScript.set("path/to/script.json")
      |    }
      |  }
      |}
      """.trimMargin(),
    )

    val expectedMessage = additionalConfigMessage("roboScript", "sanity")

    val runner = testProjectRoot.gradleRunner()
    val result = runner.withArguments("printYml").build()

    assertThat(result.output).contains("SUCCESS")

    val resultOrange = runner.withArguments("printYmlSanity").buildAndFail()

    assertThat(resultOrange.output).contains("FAILED")
    assertThat(resultOrange.output).contains(expectedMessage)
  }

  @Test
  fun `sanityRobo - should print correct config yamls (inner config is sanity run)`() {
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
      |  additionalTestApks = [
      |    "- app: debug2.apk",
      |    "  test: test2.apk",
      |    "- test: test3.apk"
      |  ]
      |  configs {
      |    orange {
      |      clearPropertiesForSanityRobo()
      |    }
      |  }
      |}
      """.trimMargin(),
    )

    val runner = testProjectRoot.gradleRunner()
    val result = runner.withArguments("printYml").build()

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
      """.trimMargin(),
    )

    val resultOrange = runner.withArguments("printYmlOrange").build()

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
      """.trimMargin(),
    )
  }

  @Test
  fun `sanityRobo - should print correct config yamls (base config is sanity run)`() {
    testProjectRoot.writeBuildDotGradle(
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
      |      sanityRobo.set(false)
      |    }
      |  }
      |}
      """.trimMargin(),
    )

    val runner = testProjectRoot.gradleRunner()
    val result = runner.withArguments("printYml").build()

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
      """.trimMargin(),
    )

    val resultOrange = runner.withArguments("printYmlOrange").build()

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
      """.trimMargin(),
    )
  }
}
