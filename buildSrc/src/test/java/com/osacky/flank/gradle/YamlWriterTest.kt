package com.osacky.flank.gradle

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test

class YamlWriterTest {

  private val yamlWriter = YamlWriter()

  private lateinit var project: Project

  @Before
  fun setup() {
    project = ProjectBuilder.builder().withName("project").build()
  }

  @Test
  fun testWriteSingleDevice() {
    val devices = listOf(
        mapOf("model" to "NexusLowRes", "version" to "28")
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
        mapOf("model" to "NexusLowRes", "version" to "28"),
        mapOf("model" to "Nexus5", "version" to "23")
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
        mapOf("model" to "NexusLowRes", "version" to "23", "orientation" to "portrait"),
        mapOf("model" to "Nexus5", "orientation" to "landscape", "version" to "28")
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
  fun testWriteTwoCustomDevicesWithLocale() {
    val devices = listOf(
            mapOf("model" to "NexusLowRes", "version" to "23", "orientation" to "portrait", "locale" to "en"),
            mapOf("model" to "Nexus5", "orientation" to "landscape", "locale" to "es_ES", "version" to "28")
    )
    val deviceString = yamlWriter.createDeviceString(devices)
    val expected = """
      |  device:
      |  - model: NexusLowRes
      |    version: 23
      |    orientation: portrait
      |    locale: en
      |  - model: Nexus5
      |    version: 28
      |    orientation: landscape
      |    locale: es_ES
      |
    """.trimMargin()
    assertEquals(expected, deviceString)
  }

  @Test
  fun testThrowsExceptionWhenMissingModelKeyInDevice() {
    val devices = listOf(
            mapOf("version" to "23", "orientation" to "portrait", "locale" to "en")
    )
    try {
      yamlWriter.createDeviceString(devices)
      fail()
    } catch (expected: RequiredDeviceKeyMissingException) {
      assertEquals("Device should have 'model' key set to a value.", expected.message)
    }
  }

  @Test
  fun testThrowsExceptionWhenMissingVersionKeyInDevice() {
    val devices = listOf(
            mapOf("model" to "NexusLowRes", "orientation" to "portrait", "locale" to "en")
    )
    try {
      yamlWriter.createDeviceString(devices)
      fail()
    } catch (expected: RequiredDeviceKeyMissingException) {
      assertEquals("Device should have 'version' key set to a value.", expected.message)
    }
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
    val extension = emptyExtension {
      projectId = "set"
      debugApk.set("path")
      instrumentationApk.set("instrument")
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
            "  num-flaky-test-attempts: 0\n" +
            "\n" +
            "flank:\n" +
            "  project: set\n" +
            "  keep-file-path: false\n" +
            "  ignore-failed-tests: false\n" +
            "  disable-sharding: false\n" +
            "  smart-flank-disable-upload: false\n", yaml
    )
  }

  @Test
  fun verifyDebugApkThrowsError() {
    val extension = emptyExtension {
      serviceAccountCredentials.set(project.layout.projectDirectory.file("fake.json"))
    }
    try {
      yamlWriter.createConfigProps(extension, extension)
      fail()
    } catch (expected: IllegalStateException) {
      assertEquals("debugApk must be specified", expected.message)
    }
  }

  @Test
  fun verifyInstrumentationApkThrowsError() {
    val extension = emptyExtension {
      serviceAccountCredentials.set(project.layout.projectDirectory.file("fake.json"))
      debugApk.set("path")
    }
    try {
      yamlWriter.createConfigProps(extension, extension)
      fail()
    } catch (expected: IllegalStateException) {
      assertEquals("instrumentationApk must be specified", expected.message)
    }
  }

  @Test
  fun writeNoTestShards() {
    val extension = emptyExtension {
    }

    assertEquals("flank:\n" +
            "  keep-file-path: false\n" +
            "  ignore-failed-tests: false\n" +
            "  disable-sharding: false\n" +
            "  smart-flank-disable-upload: false\n",
      yamlWriter.writeFlankProperties(extension))
  }

  @Test
  fun writeProjectIdOption() {
    val extension = emptyExtension {
      projectId = "foo"
    }

    assertEquals("flank:\n" +
            "  project: foo\n" +
            "  keep-file-path: false\n" +
            "  ignore-failed-tests: false\n" +
            "  disable-sharding: false\n" +
            "  smart-flank-disable-upload: false\n",
      yamlWriter.writeFlankProperties(extension))
  }

  @Test
  fun writeTestShardOption() {
    val extension = emptyExtension {
      testShards = 5
    }

    assertEquals("flank:\n" +
            "  max-test-shards: 5\n" +
            "  keep-file-path: false\n" +
            "  ignore-failed-tests: false\n" +
            "  disable-sharding: false\n" +
            "  smart-flank-disable-upload: false\n",
            yamlWriter.writeFlankProperties(extension))
  }

  @Test
  fun writeShardTimeOption() {
    val extension = emptyExtension {
      shardTime = 120
    }

    assertEquals("flank:\n" +
            "  shard-time: 120\n" +
            "  keep-file-path: false\n" +
            "  ignore-failed-tests: false\n" +
            "  disable-sharding: false\n" +
            "  smart-flank-disable-upload: false\n",
      yamlWriter.writeFlankProperties(extension))
  }

  @Test
  fun writeNoTestRepeats() {
    val extension = emptyExtension {
      repeatTests = null
    }

    assertEquals("flank:\n" +
            "  keep-file-path: false\n" +
            "  ignore-failed-tests: false\n" +
            "  disable-sharding: false\n" +
            "  smart-flank-disable-upload: false\n",
      yamlWriter.writeFlankProperties(extension))
  }

  @Test
  fun writeTestRepeats() {
    val extension = emptyExtension {
      repeatTests = 5
    }

    assertEquals("flank:\n" +
            "  num-test-runs: 5\n" +
            "  keep-file-path: false\n" +
            "  ignore-failed-tests: false\n" +
            "  disable-sharding: false\n" +
            "  smart-flank-disable-upload: false\n",
            yamlWriter.writeFlankProperties(extension))
  }

  @Test
  fun writeTestShardAndRepeatOption() {
    val extension = emptyExtension {
      testShards = 5
      repeatTests = 2
    }

    assertEquals(
            "flank:\n" +
                    "  max-test-shards: 5\n" +
                    "  num-test-runs: 2\n" +
                    "  keep-file-path: false\n" +
                    "  ignore-failed-tests: false\n" +
                    "  disable-sharding: false\n" +
                    "  smart-flank-disable-upload: false\n",
            yamlWriter.writeFlankProperties(extension))
  }

  @Test
  fun writeResultsHistoryName() {
    val extension = emptyExtension {
      resultsHistoryName = "androidtest"
    }

    assertEquals("  use-orchestrator: false\n" +
        "  auto-google-login: false\n" +
        "  record-video: true\n" +
        "  performance-metrics: true\n" +
        "  timeout: 15m\n" +
        "  results-history-name: androidtest\n" +
        "  num-flaky-test-attempts: 0\n",
        yamlWriter.writeAdditionalProperties(extension))
  }

  @Test
  fun writeResultsBucket() {
    val extension = emptyExtension {
      resultsBucket = "fake-project.appspot.com"
    }

    assertEquals("  use-orchestrator: false\n" +
        "  auto-google-login: false\n" +
        "  record-video: true\n" +
        "  performance-metrics: true\n" +
        "  timeout: 15m\n" +
        "  results-bucket: fake-project.appspot.com\n" +
        "  num-flaky-test-attempts: 0\n",
        yamlWriter.writeAdditionalProperties(extension))
  }

  @Test
  fun writeResultsDir() {
    val extension = emptyExtension {
      resultsDir = "resultsGoHere"
    }

    assertEquals("  use-orchestrator: false\n" +
        "  auto-google-login: false\n" +
        "  record-video: true\n" +
        "  performance-metrics: true\n" +
        "  timeout: 15m\n" +
        "  num-flaky-test-attempts: 0\n" +
        "  results-dir: resultsGoHere\n",
        yamlWriter.writeAdditionalProperties(extension))
  }

  @Test
  fun writeTestTargetsAndResultsHistoryName() {
    val extension = emptyExtension {
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
        "  num-flaky-test-attempts: 0\n",
        yamlWriter.writeAdditionalProperties(extension))
  }

  @Test
  fun writeNoTestTargets() {
    val extension = emptyExtension {
      testTargets = listOf()
    }

    assertEquals("  use-orchestrator: false\n" +
        "  auto-google-login: false\n" +
        "  record-video: true\n" +
        "  performance-metrics: true\n" +
        "  timeout: 15m\n" +
        "  num-flaky-test-attempts: 0\n", yamlWriter.writeAdditionalProperties(extension))
  }

  @Test
  fun writeSingleTestTargets() {
    val extension = emptyExtension {
      testTargets = listOf("class com.example.Foo#testThing")
    }

    assertEquals("  use-orchestrator: false\n" +
        "  auto-google-login: false\n" +
        "  record-video: true\n" +
        "  performance-metrics: true\n" +
        "  timeout: 15m\n" +
        "  test-targets:\n" +
        "  - class com.example.Foo#testThing\n" +
        "  num-flaky-test-attempts: 0\n",
        yamlWriter.writeAdditionalProperties(extension))
  }

  @Test
  fun writeMultipleTestTargets() {
    val extension = emptyExtension {
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
        "  num-flaky-test-attempts: 0\n",
      yamlWriter.writeAdditionalProperties(extension))
  }

  @Test
  fun writeSmartFlankGcsPath() {
    val extension = emptyExtension {
      smartFlankGcsPath = "gs://test/fakepath.xml"
    }

    assertEquals("flank:\n" +
            "  smart-flank-gcs-path: gs://test/fakepath.xml\n" +
            "  keep-file-path: false\n" +
            "  ignore-failed-tests: false\n" +
            "  disable-sharding: false\n" +
            "  smart-flank-disable-upload: false\n",
        yamlWriter.writeFlankProperties(extension))
  }

  @Test
  fun writeNoDirectoriesToPull() {
    val extension = emptyExtension {
      directoriesToPull = listOf()
    }

    assertEquals("  use-orchestrator: false\n" +
        "  auto-google-login: false\n" +
        "  record-video: true\n" +
        "  performance-metrics: true\n" +
        "  timeout: 15m\n" +
        "  num-flaky-test-attempts: 0\n", yamlWriter.writeAdditionalProperties(extension))
  }

  @Test
  fun writeSingleDirectoriesToPull() {
    val extension = emptyExtension {
      directoriesToPull = listOf("/sdcard/screenshots")
    }

    assertEquals("  use-orchestrator: false\n" +
        "  auto-google-login: false\n" +
        "  record-video: true\n" +
        "  performance-metrics: true\n" +
        "  timeout: 15m\n" +
        "  directories-to-pull:\n" +
        "  - /sdcard/screenshots\n" +
        "  num-flaky-test-attempts: 0\n",
      yamlWriter.writeAdditionalProperties(extension))
  }

  @Test
  fun writeMultipleDirectoriesToPull() {
    val extension = emptyExtension {
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
        "  num-flaky-test-attempts: 0\n",
      yamlWriter.writeAdditionalProperties(extension))
  }

  @Test
  fun writeNoFilesToDownload() {
    val extension = emptyExtension {
      filesToDownload = listOf()
    }

    assertEquals("flank:\n" +
            "  keep-file-path: false\n" +
            "  ignore-failed-tests: false\n" +
            "  disable-sharding: false\n" +
            "  smart-flank-disable-upload: false\n",
      yamlWriter.writeFlankProperties(extension))
  }

  @Test
  fun writeSingleFilesToDownload() {
    val extension = emptyExtension {
      filesToDownload = listOf(".*/screenshots/.*")
    }

    assertEquals("flank:\n" +
            "  keep-file-path: false\n" +
            "  files-to-download:\n" +
            "  - .*/screenshots/.*\n" +
            "  ignore-failed-tests: false\n" +
            "  disable-sharding: false\n" +
            "  smart-flank-disable-upload: false\n",
      yamlWriter.writeFlankProperties(extension))
  }

  @Test
  fun writeMultipleFilesToDownload() {
    val extension = emptyExtension {
      filesToDownload = listOf(".*/screenshots/.*", ".*/reports/.*")
    }

    assertEquals("flank:\n" +
        "  keep-file-path: false\n" +
        "  files-to-download:\n" +
        "  - .*/screenshots/.*\n" +
        "  - .*/reports/.*\n" +
        "  ignore-failed-tests: false\n" +
        "  disable-sharding: false\n" +
        "  smart-flank-disable-upload: false\n",
      yamlWriter.writeFlankProperties(extension))
  }

  @Test
  fun writeSingleEnvironmentVariables() {
    val extension = emptyExtension {
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
        "  num-flaky-test-attempts: 0\n",
      yamlWriter.writeAdditionalProperties(extension))
  }

  @Test
  fun writeMultipleEnvironmentVariables() {
    val extension = emptyExtension {
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
        "  num-flaky-test-attempts: 0\n",
      yamlWriter.writeAdditionalProperties(extension))
  }

  @Test
  fun writeDefaultProperties() {
    val extension = emptyExtension {
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
        "  num-flaky-test-attempts: 0\n",
      yamlWriter.writeAdditionalProperties(extension))
  }

  @Test
  fun writeNoKeepFilePath() {
    val extension = FlankGradleExtension(project)

    assertEquals("flank:\n" +
            "  keep-file-path: false\n" +
            "  ignore-failed-tests: false\n" +
            "  disable-sharding: false\n" +
            "  smart-flank-disable-upload: false\n",
      yamlWriter.writeFlankProperties(extension))
  }

  @Test
  fun writeKeepFilePath() {
    val extension = emptyExtension {
      keepFilePath = true
    }

    assertEquals("flank:\n" +
            "  keep-file-path: true\n" +
            "  ignore-failed-tests: false\n" +
            "  disable-sharding: false\n" +
            "  smart-flank-disable-upload: false\n",
      yamlWriter.writeFlankProperties(extension))
  }

  @Test
  fun writeAdditionalTestApks() {
    val extension = emptyExtension {
      debugApk.set("../orange/build/output/app.apk")
      instrumentationApk.set("../orange/build/output/app-test.apk")
      additionalTestApks = mapOf(
        "../orange/build/output/app.apk" to listOf("../orange/build/output/app-test2.apk"),
        "../bob/build/output/app.apk" to listOf("../bob/build/output/app-test.apk")
      )
    }

    assertThat(
      yamlWriter.writeFlankProperties(extension),
      equalTo(
"flank:\n" +
        "  keep-file-path: false\n" +
        "  additional-app-test-apks:\n" +
        "    - app: ../orange/build/output/app.apk\n" +
        "      test: ../orange/build/output/app-test2.apk\n" +
        "    - app: ../bob/build/output/app.apk\n" +
        "      test: ../bob/build/output/app-test.apk\n" +
        "  ignore-failed-tests: false\n" +
        "  disable-sharding: false\n" +
        "  smart-flank-disable-upload: false\n"
      )
    )
  }

  @Test
  fun verifyDefaultValues() {
    val defaultFlankProperties = FlankGradleExtension(project).toFlankProperties()
    val defaultAdditionalProperties = FlankGradleExtension(project).toAdditionalProperties().trimIndent()

    val expectedFlank = """
      flank:
        keep-file-path: false
        ignore-failed-tests: false
        disable-sharding: false
        smart-flank-disable-upload: false
    """.trimIndent()

    val expectedAdditional = """
        use-orchestrator: false
        auto-google-login: false
        record-video: true
        performance-metrics: true
        timeout: 15m
        num-flaky-test-attempts: 0
    """.trimIndent()

    assertEquals(expectedFlank, defaultFlankProperties)
    assertEquals(expectedAdditional, defaultAdditionalProperties)
  }

  @Test
  fun writeRunTimeout() {
    val extension = emptyExtension {
      runTimeout = "20m"
    }

    assertTrue(yamlWriter.writeFlankProperties(extension).contains("  run-timeout: 20m"))
  }

  @Test
  fun writeIgnoreFailedTests() {
    val properties = emptyExtension {
      ignoreFailedTests = true
    }.toFlankProperties()

    assertTrue(properties.contains("  ignore-failed-tests: true"))
  }

  @Test
  fun writeDisableSharding() {
    val properties = emptyExtension {
      disableSharding = true
    }.toFlankProperties()

    assertTrue(properties.contains("  disable-sharding: true"))
  }

  @Test
  fun writeSmartFlankDisableUpload() {
    val properties = emptyExtension {
      smartFlankDisableUpload = true
    }.toFlankProperties()

    assertTrue(properties.contains("  smart-flank-disable-upload: true"))
  }

  @Test
  fun writeTestRunnerClass() {
    val properties = emptyExtension {
      testRunnerClass = "any.class.Runner"
    }.toAdditionalProperties()

    assertTrue(properties.contains("  test-runner-class: any.class.Runner"))
  }

  @Test
  fun writeLocalResultsDir() {
    val properties = emptyExtension {
      localResultsDir = "~/my/results/dir"
    }.toFlankProperties()

    assertTrue(properties.contains("  local-result-dir: ~/my/results/dir"))
  }

  @Test
  fun writeNumUniformShards() {
    val properties = emptyExtension {
      numUniformShards = 20
    }.toAdditionalProperties()

    assertTrue(properties.contains("  num-uniform-shards: 20"))
  }

  @Test
  fun writeClientDetails() {
    val properties = emptyExtension {
      clientDetails = mapOf(
              "anyDetail1" to "anyValue1",
              "anyDetail2" to "anyValue2"
      )
    }.toAdditionalProperties()

    assertTrue(properties.contains("""
      |  client-details:
      |    anyDetail1: anyValue1
      |    anyDetail2: anyValue2
    """.trimMargin()))
  }

  @Test
  fun writeTestTargetsAlwaysRun() {
    val properties = emptyExtension {
      testTargetsAlwaysRun = listOf(
              "com.example.FirstTests#test1",
              "com.example.FirstTests#test2",
              "com.example.FirstTests#test3"
      )
    }.toFlankProperties()

    assertTrue(properties.contains("""
      |  test-targets-always-run:
      |  - class com.example.FirstTests#test1
      |  - class com.example.FirstTests#test2
      |  - class com.example.FirstTests#test3
    """.trimMargin()))
  }

  @Test
  fun writeOtherFiles() {
    val properties = emptyExtension {
      otherFiles = mapOf(
              "/example/path/test1" to "anyfile.txt",
              "/example/path/test2" to "anyfile2.txt"
      )
    }.toAdditionalProperties()

    assertTrue(properties.contains("""
        |  other-files:
        |    /example/path/test1 anyfile.txt
        |    /example/path/test2 anyfile2.txt
    """.trimMargin()))
  }

  @Test
  fun writeNetworkProfile() {
    val properties = emptyExtension {
      networkProfile = "LTE"
    }.toAdditionalProperties()

    assertTrue(properties.contains("  network-profile: LTE"))
  }

  private fun emptyExtension(block: FlankGradleExtension.() -> Unit) = FlankGradleExtension(project).apply(block)
  private fun FlankGradleExtension.toFlankProperties() = yamlWriter.writeFlankProperties(this).trimIndent()
  private fun FlankGradleExtension.toAdditionalProperties() = yamlWriter.writeAdditionalProperties(this)
}
