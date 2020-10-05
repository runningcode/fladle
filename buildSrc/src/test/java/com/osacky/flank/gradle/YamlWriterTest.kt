package com.osacky.flank.gradle

import com.google.common.truth.Truth.assertThat
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Assert.assertEquals
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
    val expected =
      """
      |  device:
      |  - model: NexusLowRes
      |    version: 28
      |
    """.trimMargin()
    assertThat(deviceString).isEqualTo(expected)
  }

  @Test
  fun testWriteTwoDevices() {
    val devices = listOf(
      mapOf("model" to "NexusLowRes", "version" to "28"),
      mapOf("model" to "Nexus5", "version" to "23")
    )
    val deviceString = yamlWriter.createDeviceString(devices)
    val expected =
      """
      |  device:
      |  - model: NexusLowRes
      |    version: 28
      |  - model: Nexus5
      |    version: 23
      |
    """.trimMargin()
    assertThat(deviceString).isEqualTo(expected)
  }

  @Test
  fun testWriteTwoCustomDevices() {
    val devices = listOf(
      mapOf("model" to "NexusLowRes", "version" to "23", "orientation" to "portrait"),
      mapOf("model" to "Nexus5", "orientation" to "landscape", "version" to "28")
    )
    val deviceString = yamlWriter.createDeviceString(devices)
    val expected =
      """
      |  device:
      |  - model: NexusLowRes
      |    version: 23
      |    orientation: portrait
      |  - model: Nexus5
      |    version: 28
      |    orientation: landscape
      |
    """.trimMargin()
    assertThat(deviceString).isEqualTo(expected)
  }

  @Test
  fun testWriteTwoCustomDevicesWithLocale() {
    val devices = listOf(
      mapOf("model" to "NexusLowRes", "version" to "23", "orientation" to "portrait", "locale" to "en"),
      mapOf("model" to "Nexus5", "orientation" to "landscape", "locale" to "es_ES", "version" to "28")
    )
    val deviceString = yamlWriter.createDeviceString(devices)
    val expected =
      """
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
    assertThat(deviceString).isEqualTo(expected)
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
    val extension = FlankGradleExtension(project.objects)
    try {
      yamlWriter.createConfigProps(extension, extension)
      fail()
    } catch (expected: IllegalStateException) {
      assertEquals(
        "ServiceAccountCredentials in fladle extension not set. https://github.com/runningcode/fladle#serviceaccountcredentials",
        expected.message
      )
    }
  }

  @Test
  fun verifyMissingServiceDoesntThrowErrorIfProjectIdSet() {
    val extension = emptyExtension {
      projectId.set("set")
      debugApk.set("path")
      instrumentationApk.set("instrument")
    }
    val yaml = yamlWriter.createConfigProps(extension, extension)
    assertThat(yaml).isEqualTo(
      """
             gcloud:
               app: path
               test: instrument

             flank:
               project: set
      """.trimIndent() + '\n' // Dunno why this needs to be here to make the tests pass.
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
  fun verifyNoInstrumentationApkThrowsError() {
    val extension = emptyExtension {
      serviceAccountCredentials.set(project.layout.projectDirectory.file("fake.json"))
      debugApk.set("path")
    }
    try {
      yamlWriter.createConfigProps(extension, extension)
      fail()
    } catch (expected: IllegalStateException) {
      assertThat(expected).hasMessageThat().isEqualTo(
        """
        Must specify either a instrumentationApk file or a roboScript file.
        instrumentationApk=null
        roboScript=null
        """.trimIndent()
      )
    }
  }

  @Test
  fun verifyInstrumentationApkAndRoboscriptThrowsError() {
    val extension = emptyExtension {
      serviceAccountCredentials.set(project.layout.projectDirectory.file("fake.json"))
      debugApk.set("path")
      instrumentationApk.set("build/test/*.apk")
      roboScript.set("foo")
    }
    try {
      yamlWriter.createConfigProps(extension, extension)
      fail()
    } catch (expected: IllegalStateException) {
      assertThat(expected).hasMessageThat().isEqualTo(
        """
        Both instrumentationApk file and roboScript file were specified, but only one is expected.
        instrumentationApk=build/test/*.apk
        roboScript=foo
        """.trimIndent()
      )
    }
  }

  @Test
  fun verifyOnlyWithRoboscriptWorks() {
    val extension = emptyExtension {
      serviceAccountCredentials.set(project.layout.projectDirectory.file("fake.json"))
      debugApk.set("path")
      roboScript.set("foo")
    }
    val configProps = yamlWriter.createConfigProps(extension, extension)
    assertThat(configProps).isEqualTo(
      """
    gcloud:
      app: path
      robo-script: foo

    flank:
      """.trimIndent() + '\n'
    )
  }

  @Test
  fun writeNoTestShards() {
    val extension = emptyExtension {
    }.toFlankProperties()

    assertThat(extension).doesNotContain("max-test-shards")
  }

  @Test
  fun writeProjectIdOption() {
    val extension = emptyExtension {
      projectId.set("foo")
    }.toFlankProperties()

    assertThat(extension).containsMatch("project: foo")
  }

  @Test
  fun writeTestShardOption() {
    val extension = emptyExtension {
      testShards.set(5)
    }.toFlankProperties()

    assertThat(extension).containsMatch("max-test-shards: 5")
  }

  @Test
  fun writeShardTimeOption() {
    val extension = emptyExtension {
      shardTime.set(120)
    }.toFlankProperties()

    assertThat(extension).containsMatch("shard-time: 120")
  }

  @Test
  fun writeNoTestRepeats() {
    val extension = emptyExtension {}.toFlankProperties()

    assertThat(extension).doesNotContain("num-test-runs")
  }

  @Test
  fun writeTestRepeats() {
    val extension = emptyExtension {
      repeatTests.set(5)
    }.toFlankProperties()

    assertThat(extension).containsMatch("num-test-runs: 5")
  }

  @Test
  fun writeTestShardAndRepeatOption() {
    val extension = emptyExtension {
      testShards.set(5)
      repeatTests.set(2)
    }.toFlankProperties()

    assertThat(extension).containsMatch("num-test-runs: 2")
    assertThat(extension).containsMatch("max-test-shards: 5")
  }

  @Test
  fun writeResultsHistoryName() {
    val extension = emptyExtension {
      resultsHistoryName.set("androidtest")
    }.toAdditionalProperties()

    assertThat(extension).containsMatch("results-history-name: androidtest")
  }

  @Test
  fun writeResultsBucket() {
    val extension = emptyExtension {
      resultsBucket.set("fake-project.appspot.com")
    }.toAdditionalProperties()

    assertThat(extension).containsMatch("results-bucket: fake-project.appspot.com")
  }

  @Test
  fun writeResultsDir() {
    val extension = emptyExtension {
      resultsDir.set("resultsGoHere")
    }.toAdditionalProperties()

    assertThat(extension).containsMatch("results-dir: resultsGoHere")
  }

  @Test
  fun writeTestTargetsAndResultsHistoryName() {
    val extension = emptyExtension {
      resultsHistoryName.set("androidtest")
      testTargets.set(
        project.provider {
          listOf("class com.example.Foo")
        }
      )
    }.toAdditionalProperties()

    assertThat(extension).containsMatch("results-history-name: androidtest")
    assertThat(extension).containsMatch(
      """
    |  test-targets:
    |  - class com.example.Foo
      """.trimMargin()
    )
  }

  @Test
  fun writeNoTestTargets() {
    val extension = emptyExtension {}.toAdditionalProperties()

    assertThat(extension).doesNotContain("additional-test-apks")
  }

  @Test
  fun writeSingleTestTargets() {
    val extension = emptyExtension {
      testTargets.set(
        project.provider {
          listOf("class com.example.Foo#testThing")
        }
      )
    }.toAdditionalProperties()

    assertThat(extension).containsMatch(
      """
    |  test-targets:
    |  - class com.example.Foo#testThing
      """.trimMargin()
    )
  }

  @Test
  fun writeMultipleTestTargets() {
    val extension = emptyExtension {
      testTargets.set(
        project.provider {
          listOf("class com.example.Foo#testThing", "class com.example.Foo#testThing2")
        }
      )
    }.toAdditionalProperties()

    assertThat(extension).containsMatch(
      """
    |  test-targets:
    |  - class com.example.Foo#testThing
    |  - class com.example.Foo#testThing2
      """.trimMargin()
    )
  }

  @Test
  fun writeSmartFlankGcsPath() {
    val extension = emptyExtension {
      smartFlankGcsPath.set("gs://test/fakepath.xml")
    }.toFlankProperties()

    assertThat(extension).containsMatch("smart-flank-gcs-path: gs://test/fakepath.xml")
  }

  @Test
  fun writeNoDirectoriesToPull() {
    val extension = emptyExtension {
      directoriesToPull.set(
        project.provider {
          emptyList<String>()
        }
      )
    }.toAdditionalProperties()

    assertThat(extension).doesNotContain("directories-to-pull")
  }

  @Test
  fun writeSingleDirectoriesToPull() {
    val extension = emptyExtension {
      directoriesToPull.set(
        project.provider {
          listOf("/sdcard/screenshots")
        }
      )
    }.toAdditionalProperties()

    assertThat(extension).containsMatch(
      """
    |  directories-to-pull:
    |  - /sdcard/screenshots
      """.trimMargin()
    )
  }

  @Test
  fun writeMultipleDirectoriesToPull() {
    val extension = emptyExtension {
      directoriesToPull.set(
        project.provider {
          listOf("/sdcard/screenshots", "/sdcard/reports")
        }
      )
    }.toAdditionalProperties()

    assertThat(extension).containsMatch(
      """
    |  directories-to-pull:
    |  - /sdcard/screenshots
    |  - /sdcard/reports
      """.trimMargin()
    )
  }

  @Test
  fun writeNoFilesToDownload() {
    val extension = emptyExtension {
      filesToDownload.set(
        project.provider {
          emptyList<String>()
        }
      )
    }.toFlankProperties()

    assertThat(extension).doesNotContain("files-to-download")
  }

  @Test
  fun writeSingleFilesToDownload() {
    val extension = emptyExtension {
      filesToDownload.set(
        project.provider {
          listOf(".*/screenshots/.*")
        }
      )
    }.toFlankProperties()

    assertThat(extension).containsMatch(
      """
    |  files-to-download:
    |  - .*/screenshots/.*
      """.trimMargin()
    )
  }

  @Test
  fun writeMultipleFilesToDownload() {
    val extension = emptyExtension {
      filesToDownload.set(
        project.provider {
          listOf(".*/screenshots/.*", ".*/reports/.*")
        }
      )
    }.toFlankProperties()

    assertThat(extension).containsMatch(
      """
    |  files-to-download:
    |  - .*/screenshots/.*
    |  - .*/reports/.*
      """.trimMargin()
    )
  }

  @Test
  fun writeSingleEnvironmentVariables() {
    val extension = emptyExtension {
      environmentVariables.set(
        project.provider {
          mapOf(
            "listener" to "com.osacky.flank.sample.Listener"
          )
        }
      )
    }.toAdditionalProperties()

    assertThat(extension).containsMatch(
      """
    |  environment-variables:
    |    listener: com.osacky.flank.sample.Listener
      """.trimMargin()
    )
  }

  @Test
  fun writeMultipleEnvironmentVariables() {
    val extension = emptyExtension {
      environmentVariables.set(
        project.provider {
          mapOf(
            "clearPackageData" to "true",
            "listener" to "com.osacky.flank.sample.Listener"
          )
        }
      )
    }.toAdditionalProperties()

    assertThat(extension).containsMatch(
      """
    |  environment-variables:
    |    clearPackageData: true
    |    listener: com.osacky.flank.sample.Listener
      """.trimMargin()
    )
  }

  @Test
  fun writeNoKeepFilePath() {
    val extension = emptyExtension().toFlankProperties()

    assertThat(extension).doesNotContain("keep-file-path")
  }

  @Test
  fun writeKeepFilePath() {
    val extension = emptyExtension {
      keepFilePath.set(true)
    }.toFlankProperties()

    assertThat(extension).containsMatch("keep-file-path: true")
  }

  @Test
  fun writeAdditionalTestApks() {
    val extension = emptyExtension {
      debugApk.set("../orange/build/output/app.apk")
      instrumentationApk.set("../orange/build/output/app-test.apk")
      additionalTestApks.set(
        project.provider {
          listOf(
            "- app: ../orange/build/output/app.apk",
            "  test: ../orange/build/output/app-test2.apk",
            "- app: ../bob/build/output/app.apk",
            "  test: ../bob/build/output/app-test.apk",
            "- test: ../bob/build/output/app-test2.apk",
            "- test: ../bob/build/output/app-test3.apk"
          )
        }
      )
    }.toFlankProperties()

    assertThat(extension).containsMatch(
      """
    |  additional-app-test-apks:
    |    - app: ../orange/build/output/app.apk
    |      test: ../orange/build/output/app-test2.apk
    |    - app: ../bob/build/output/app.apk
    |      test: ../bob/build/output/app-test.apk
    |    - test: ../bob/build/output/app-test2.apk
    |    - test: ../bob/build/output/app-test3.apk
      """.trimMargin()
    )
  }

  @Test
  fun writeRunTimeout() {
    val extension = emptyExtension {
      runTimeout.set("20m")
    }.toFlankProperties()

    assertThat(extension).containsMatch("run-timeout: 20m")
  }

  @Test
  fun writeIgnoreFailedTests() {
    val properties = emptyExtension {
      ignoreFailedTests.set(true)
    }.toFlankProperties()

    assertThat(properties).containsMatch("ignore-failed-tests: true")
  }

  @Test
  fun writeDisableSharding() {
    val properties = emptyExtension {
      disableSharding.set(true)
    }.toFlankProperties()

    assertThat(properties).containsMatch("disable-sharding: true")
  }

  @Test
  fun writeSmartFlankDisableUpload() {
    val properties = emptyExtension {
      smartFlankDisableUpload.set(true)
    }.toFlankProperties()

    assertThat(properties).containsMatch("smart-flank-disable-upload: true")
  }

  @Test
  fun writeTestRunnerClass() {
    val properties = emptyExtension {
      testRunnerClass.set("any.class.Runner")
    }.toAdditionalProperties()

    assertThat(properties).containsMatch("test-runner-class: any.class.Runner")
  }

  @Test
  fun writeLocalResultsDir() {
    val properties = emptyExtension {
      localResultsDir.set("~/my/results/dir")
    }.toFlankProperties()

    assertThat(properties).containsMatch("local-result-dir: ~/my/results/dir")
  }

  @Test
  fun writeNumUniformShards() {
    val properties = emptyExtension {
      numUniformShards.set(20)
    }.toAdditionalProperties()

    assertThat(properties).containsMatch("num-uniform-shards: 20")
  }

  @Test
  fun writeOutputStyle() {
    val properties = emptyExtension {
      outputStyle.set("anyString")
    }.toFlankProperties()

    assertThat(properties).containsMatch("output-style: anyString")
  }

  @Test
  fun missingOutputStyle() {
    val properties = emptyExtension().toFlankProperties()

    assertThat(properties).doesNotContainMatch("output-style")
  }

  @Test
  fun writeLegacyJunitResult() {
    val properties = emptyExtension {
      legacyJunitResult.set(true)
    }.toFlankProperties()

    assertThat(properties).containsMatch("legacy-junit-result: true")
  }

  @Test
  fun missingLegacyJunitResult() {
    val properties = emptyExtension().toFlankProperties()

    assertThat(properties).doesNotContainMatch("legacy-junit-result")
  }

  @Test
  fun writeFullJunitResult() {
    val properties = emptyExtension {
      fullJunitResult.set(true)
    }.toFlankProperties()

    assertThat(properties).containsMatch("full-junit-result: true")
  }

  @Test
  fun missingFullJunitResult() {
    val properties = emptyExtension().toFlankProperties()

    assertThat(properties).doesNotContainMatch("full-junit-result")
  }

  @Test
  fun writeClientDetails() {
    val properties = emptyExtension {
      clientDetails.set(
        project.provider {
          mapOf(
            "anyDetail1" to "anyValue1",
            "anyDetail2" to "anyValue2"
          )
        }
      )
    }.toAdditionalProperties()

    assertThat(properties).containsMatch(
      """
      |  client-details:
      |    anyDetail1: anyValue1
      |    anyDetail2: anyValue2
      """.trimMargin()
    )
  }

  @Test
  fun writeTestTargetsAlwaysRun() {
    val properties = emptyExtension {
      testTargetsAlwaysRun.set(
        project.provider {
          listOf(
            "com.example.FirstTests#test1",
            "com.example.FirstTests#test2",
            "com.example.FirstTests#test3"
          )
        }
      )
    }.toFlankProperties()

    assertThat(properties).containsMatch(
      """
      |  test-targets-always-run:
      |  - class com.example.FirstTests#test1
      |  - class com.example.FirstTests#test2
      |  - class com.example.FirstTests#test3
      """.trimMargin()
    )
  }

  @Test
  fun writeOtherFiles() {
    val properties = emptyExtension {
      otherFiles.set(
        project.provider {
          mapOf(
            "/example/path/test1" to "anyfile.txt",
            "/example/path/test2" to "anyfile2.txt"
          )
        }
      )
    }.toAdditionalProperties()

    assertThat(properties).containsMatch(
      """
        |  other-files:
        |    /example/path/test1: anyfile.txt
        |    /example/path/test2: anyfile2.txt
      """.trimMargin()
    )
  }

  @Test
  fun writeNetworkProfile() {
    val properties = emptyExtension {
      networkProfile.set("LTE")
    }.toAdditionalProperties()

    assertThat(properties).containsMatch("network-profile: LTE")
  }

  @Test
  fun writeRoboScript() {
    val properties = emptyExtension {
      roboScript.set("~/my/dir/with/script.json")
    }.toAdditionalProperties()

    assertThat(properties).containsMatch("robo-script: ~/my/dir/with/script.json")
  }

  @Test
  fun writeRoboDirectives() {
    val properties = emptyExtension {
      roboDirectives.set(
        project.provider {
          listOf(
            listOf("click", "button3"),
            listOf("ignore", "button1", ""),
            listOf("text", "field1", "my common text")
          )
        }
      )
    }.toAdditionalProperties()

    assertThat(properties).containsMatch(
      """
        |  robo-directives:
        |    click:button3: ""
        |    ignore:button1: ""
        |    text:field1: my common text
      """.trimMargin()
    )
  }

  private fun emptyExtension() = FlankGradleExtension(project.objects)
  private fun emptyExtension(block: FlankGradleExtension.() -> Unit) = emptyExtension().apply(block)
  private fun FlankGradleExtension.toFlankProperties() = yamlWriter.writeFlankProperties(this).trimIndent()
  private fun FlankGradleExtension.toAdditionalProperties() = yamlWriter.writeAdditionalProperties(this)
}
