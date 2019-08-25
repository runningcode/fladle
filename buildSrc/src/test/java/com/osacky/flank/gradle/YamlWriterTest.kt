package com.osacky.flank.gradle

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test

class YamlWriterTest {

  internal val yamlWriter = YamlWriter()

  private lateinit var project: Project

  @Before
  fun setup() {
    project = ProjectBuilder.builder().withName("project").build()
  }

  @Test
  fun testWriteSingleDevice() {
    val devices = listOf(
        Device("NexusLowRes", 28)
    )
    val deviceString = yamlWriter.createDeviceString(devices)
    val expected = """
      |  device:
      |  - model: NexusLowRes
      |    version: 28
      |
    """.trimMargin()
    assertEquals(expected, deviceString)
  }

  @Test
  fun testWriteTwoDevices() {
    val devices = listOf(
        Device("NexusLowRes", 28),
        Device("Nexus5", 23)
    )
    val deviceString = yamlWriter.createDeviceString(devices)
    val expected = """
      |  device:
      |  - model: NexusLowRes
      |    version: 28
      |  - model: Nexus5
      |    version: 23
      |
    """.trimMargin()
    assertEquals(expected, deviceString)
  }

  @Test
  fun testWriteTwoCustomDevices() {
    val devices = listOf(
        Device("NexusLowRes", 23, orientation = "portrait"),
        Device("Nexus5", orientation = "landscape", version = 28)
    )
    val deviceString = yamlWriter.createDeviceString(devices)
    val expected = """
      |  device:
      |  - model: NexusLowRes
      |    version: 23
      |    orientation: portrait
      |  - model: Nexus5
      |    version: 28
      |    orientation: landscape
      |
    """.trimMargin()
    assertEquals(expected, deviceString)
  }

  @Test
  fun verifyMissingServiceThrowsError() {
    val extension = FlankGradleExtension(project)
    try {
      yamlWriter.createConfigProps(extension, extension)
      fail()
    } catch (expected: IllegalStateException) {
      assertEquals("ServiceAccountCredentials in fladle extension not set. https://github.com/runningcode/fladle#serviceaccountcredentials", expected.message)
    }
  }

  @Test
  fun verifyMissingServiceDoesntThrowErrorIfProjectIdSet() {
    val extension = FlankGradleExtension(project).apply {
      projectId = "set"
      debugApk = "path"
      instrumentationApk = "instrument"
    }
    val yaml = yamlWriter.createConfigProps(extension, extension)
    assertEquals("gcloud:\n" +
        "  app: path\n" +
        "  test: instrument\n" +
        "  device:\n" +
        "  - model: NexusLowRes\n" +
        "    version: 28\n" +
        "\n" +
        "  use-orchestrator: false\n" +
        "  auto-google-login: false\n" +
        "  record-video: true\n" +
        "  performance-metrics: true\n" +
        "  timeout: 15m\n" +
        "  flaky-test-attempts: 0\n" +
        "\n" +
        "flank:\n" +
        "  project: set\n", yaml)
  }

  @Test
  fun verifyDebugApkThrowsError() {
    val extension = FlankGradleExtension(project).apply {
      serviceAccountCredentials = "fake.json"
    }
    try {
      yamlWriter.createConfigProps(extension, extension)
      fail()
    } catch (expected: IllegalStateException) {
      assertEquals("debugApk cannot be null", expected.message)
    }
  }

  @Test
  fun verifyInstrumentationApkThrowsError() {
    val extension = FlankGradleExtension(project).apply {
      serviceAccountCredentials = "fake.json"
      debugApk = "path"
    }
    try {
      yamlWriter.createConfigProps(extension, extension)
      fail()
    } catch (expected: IllegalStateException) {
      assertEquals("instrumentationApk cannot be null", expected.message)
    }
  }

  @Test
  fun writeNoTestShards() {
    val extension = FlankGradleExtension(project).apply {
    }

    assertEquals("", yamlWriter.writeFlankProperties(extension))
  }

  @Test
  fun writeProjectIdOption() {
    val extension = FlankGradleExtension(project).apply {
      projectId = "foo"
    }

    assertEquals("flank:\n" +
        "  project: foo\n", yamlWriter.writeFlankProperties(extension))
  }

  @Test
  fun writeTestShardOption() {
    val extension = FlankGradleExtension(project).apply {
      testShards = 5
    }

    assertEquals("flank:\n" +
        "  max-test-shards: 5\n", yamlWriter.writeFlankProperties(extension))
  }

  @Test
  fun writeShardTimeOption() {
    val extension = FlankGradleExtension(project).apply {
      shardTime = 120
    }

    assertEquals("flank:\n" +
            "  shard-time: 120\n", yamlWriter.writeFlankProperties(extension))
  }

  @Test
  fun writeNoTestRepeats() {
    val extension = FlankGradleExtension(project).apply {
      repeatTests = null
    }

    assertEquals("", yamlWriter.writeFlankProperties(extension))
  }

  @Test
  fun writeTestRepeats() {
    val extension = FlankGradleExtension(project).apply {
      repeatTests = 5
    }

    assertEquals("flank:\n" +
        "  repeat-tests: 5\n", yamlWriter.writeFlankProperties(extension))
  }

  @Test
  fun writeTestShardAndRepeatOption() {
    val extension = FlankGradleExtension(project).apply {
      testShards = 5
      repeatTests = 2
    }

    assertEquals("flank:\n" +
        "  max-test-shards: 5\n" +
        "  repeat-tests: 2\n", yamlWriter.writeFlankProperties(extension))
  }

  @Test
  fun writeResultsHistoryName() {
    val extension = FlankGradleExtension(project).apply {
      resultsHistoryName = "androidtest"
    }

    assertEquals("  use-orchestrator: false\n" +
        "  auto-google-login: false\n" +
        "  record-video: true\n" +
        "  performance-metrics: true\n" +
        "  timeout: 15m\n" +
        "  results-history-name: androidtest\n" +
        "  flaky-test-attempts: 0\n",
        yamlWriter.writeAdditionalProperties(extension))
  }

  @Test
  fun writeResultsBucket() {
    val extension = FlankGradleExtension(project).apply {
      resultsBucket = "fake-project.appspot.com"
    }

    assertEquals("  use-orchestrator: false\n" +
        "  auto-google-login: false\n" +
        "  record-video: true\n" +
        "  performance-metrics: true\n" +
        "  timeout: 15m\n" +
        "  results-bucket: fake-project.appspot.com\n" +
        "  flaky-test-attempts: 0\n",
        yamlWriter.writeAdditionalProperties(extension))
  }

  @Test
  fun writeTestTargetsAndResultsHistoryName() {
    val extension = FlankGradleExtension(project).apply {
      resultsHistoryName = "androidtest"
      testTargets = listOf("class com.example.Foo")
    }

    assertEquals("  use-orchestrator: false\n" +
        "  auto-google-login: false\n" +
        "  record-video: true\n" +
        "  performance-metrics: true\n" +
        "  timeout: 15m\n" +
        "  results-history-name: androidtest\n" +
        "  test-targets:\n" +
        "  - class com.example.Foo\n" +
        "  flaky-test-attempts: 0\n",
        yamlWriter.writeAdditionalProperties(extension))
  }

  @Test
  fun writeNoTestTargets() {
    val extension = FlankGradleExtension(project).apply {
      testTargets = listOf()
    }

    assertEquals("  use-orchestrator: false\n" +
        "  auto-google-login: false\n" +
        "  record-video: true\n" +
        "  performance-metrics: true\n" +
        "  timeout: 15m\n" +
        "  flaky-test-attempts: 0\n", yamlWriter.writeAdditionalProperties(extension))
  }

  @Test
  fun writeSingleTestTargets() {
    val extension = FlankGradleExtension(project).apply {
      testTargets = listOf("class com.example.Foo#testThing")
    }

    assertEquals("  use-orchestrator: false\n" +
        "  auto-google-login: false\n" +
        "  record-video: true\n" +
        "  performance-metrics: true\n" +
        "  timeout: 15m\n" +
        "  test-targets:\n" +
        "  - class com.example.Foo#testThing\n" +
        "  flaky-test-attempts: 0\n",
        yamlWriter.writeAdditionalProperties(extension))
  }

  @Test
  fun writeMultipleTestTargets() {
    val extension = FlankGradleExtension(project).apply {
      testTargets = listOf("class com.example.Foo#testThing", "class com.example.Foo#testThing2")
    }

    assertEquals("  use-orchestrator: false\n" +
        "  auto-google-login: false\n" +
        "  record-video: true\n" +
        "  performance-metrics: true\n" +
        "  timeout: 15m\n" +
        "  test-targets:\n" +
        "  - class com.example.Foo#testThing\n" +
        "  - class com.example.Foo#testThing2\n" +
        "  flaky-test-attempts: 0\n",
      yamlWriter.writeAdditionalProperties(extension))
  }

  @Test
  fun writeSmartFlankGcsPath() {
    val extension = FlankGradleExtension(project).apply {
      smartFlankGcsPath = "gs://test/fakepath.xml"
    }

    assertEquals("flank:\n" +
        "  smart-flank-gcs-path: gs://test/fakepath.xml\n",
        yamlWriter.writeFlankProperties(extension))
  }

  @Test
  fun writeNoDirectoriesToPull() {
    val extension = FlankGradleExtension(project).apply {
      directoriesToPull = listOf()
    }

    assertEquals("  use-orchestrator: false\n" +
        "  auto-google-login: false\n" +
        "  record-video: true\n" +
        "  performance-metrics: true\n" +
        "  timeout: 15m\n" +
        "  flaky-test-attempts: 0\n", yamlWriter.writeAdditionalProperties(extension))
  }

  @Test
  fun writeSingleDirectoriesToPull() {
    val extension = FlankGradleExtension(project).apply {
      directoriesToPull = listOf("/sdcard/screenshots")
    }

    assertEquals("  use-orchestrator: false\n" +
        "  auto-google-login: false\n" +
        "  record-video: true\n" +
        "  performance-metrics: true\n" +
        "  timeout: 15m\n" +
        "  directories-to-pull:\n" +
        "  - /sdcard/screenshots\n" +
        "  flaky-test-attempts: 0\n",
      yamlWriter.writeAdditionalProperties(extension))
  }

  @Test
  fun writeMultipleDirectoriesToPull() {
    val extension = FlankGradleExtension(project).apply {
      directoriesToPull = listOf("/sdcard/screenshots", "/sdcard/reports")
    }

    assertEquals("  use-orchestrator: false\n" +
        "  auto-google-login: false\n" +
        "  record-video: true\n" +
        "  performance-metrics: true\n" +
        "  timeout: 15m\n" +
        "  directories-to-pull:\n" +
        "  - /sdcard/screenshots\n" +
        "  - /sdcard/reports\n" +
        "  flaky-test-attempts: 0\n",
      yamlWriter.writeAdditionalProperties(extension))
  }

  @Test
  fun writeNoFilesToDownload() {
    val extension = FlankGradleExtension(project).apply {
      filesToDownload = listOf()
    }

    assertEquals("", yamlWriter.writeFlankProperties(extension))
  }

  @Test
  fun writeSingleFilesToDownload() {
    val extension = FlankGradleExtension(project).apply {
      filesToDownload = listOf(".*/screenshots/.*")
    }

    assertEquals("flank:\n" +
        "  files-to-download:\n" +
        "  - .*/screenshots/.*\n",
      yamlWriter.writeFlankProperties(extension))
  }

  @Test
  fun writeMultipleFilesToDownload() {
    val extension = FlankGradleExtension(project).apply {
      filesToDownload = listOf(".*/screenshots/.*", ".*/reports/.*")
    }

    assertEquals("flank:\n" +
        "  files-to-download:\n" +
        "  - .*/screenshots/.*\n" +
        "  - .*/reports/.*\n",
      yamlWriter.writeFlankProperties(extension))
  }

  @Test
  fun writeSingleEnvironmentVariables() {
    val extension = FlankGradleExtension(project).apply {
      environmentVariables = mapOf(
        "listener" to "com.osacky.flank.sample.Listener"
      )
    }

    assertEquals("  use-orchestrator: false\n" +
        "  auto-google-login: false\n" +
        "  record-video: true\n" +
        "  performance-metrics: true\n" +
        "  timeout: 15m\n" +
        "  environment-variables:\n" +
        "    listener: com.osacky.flank.sample.Listener\n" +
        "  flaky-test-attempts: 0\n",
      yamlWriter.writeAdditionalProperties(extension))
  }

  @Test
  fun writeMultipleEnvironmentVariables() {
    val extension = FlankGradleExtension(project).apply {
      environmentVariables = mapOf(
        "clearPackageData" to "true",
        "listener" to "com.osacky.flank.sample.Listener"
      )
    }

    assertEquals("  use-orchestrator: false\n" +
        "  auto-google-login: false\n" +
        "  record-video: true\n" +
        "  performance-metrics: true\n" +
        "  timeout: 15m\n" +
        "  environment-variables:\n" +
        "    clearPackageData: true\n" +
        "    listener: com.osacky.flank.sample.Listener\n" +
        "  flaky-test-attempts: 0\n",
      yamlWriter.writeAdditionalProperties(extension))
  }

  @Test
  fun writeDefaultProperties() {
    val extension = FlankGradleExtension(project).apply {
      useOrchestrator = true
      autoGoogleLogin = true
      recordVideo = false
      performanceMetrics = false
      timeoutMin = 45
    }

    assertEquals("  use-orchestrator: true\n" +
        "  auto-google-login: true\n" +
        "  record-video: false\n" +
        "  performance-metrics: false\n" +
        "  timeout: 45m\n" +
        "  flaky-test-attempts: 0\n",
      yamlWriter.writeAdditionalProperties(extension))
  }
}
