package com.osacky.flank.gradle

import com.google.common.truth.Truth.assertThat
import org.gradle.testkit.runner.GradleRunner
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class MultipleConfigsTest {
  @get:Rule
  var testProjectRoot = TemporaryFolder()

  @Test
  fun checkCanPrintSecondConfig() {
    val file = testProjectRoot.newFile("build.gradle")
    file.writeText(
      """
      |plugins {
      |  id "com.osacky.fladle"
      |}
      |
      |fladle {
      |  serviceAccountCredentials = layout.projectDirectory.file("flank-gradle-service.json")
      |  debugApk = "foo.apk"
      |  instrumentationApk = "instrument.apk"
      |
      |  testTargets.set(project.provider { ['default'] })
      |  localResultsDir = 'defaultDir'
      |  configs {
      |    orange {
      |      testTargets.set(project.provider { ['override'] })
      |      localResultsDir.set('overrideDir')
      |    }
      |  }
      |}
    """.trimMargin()
    )
    testProjectRoot.newFile("flank-gradle-service.json").writeText("{}")

    val result = GradleRunner.create()
      .withPluginClasspath()
      .withArguments("writeConfigPropsOrange", "--stacktrace")
      .forwardOutput()
      .withProjectDir(testProjectRoot.root)
      .build()

    assertThat(result.output).contains("SUCCESS")

    val writtenYmlFile = testProjectRoot.root.resolve("build/fladle/orange/flank.yml")
    assertThat(writtenYmlFile.readText()).contains(
      """
      |gcloud:
      |  app: foo.apk
      |  test: instrument.apk
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
    """.trimMargin()
    )

    val regularConfig = GradleRunner.create()
      .withPluginClasspath()
      .withArguments("writeConfigProps")
      .forwardOutput()
      .withProjectDir(testProjectRoot.root)
      .build()

    assertThat(regularConfig.output).contains("SUCCESS")

    val writtenBaseYml = testProjectRoot.root.resolve("build/fladle/flank.yml")
    assertThat(writtenBaseYml.readText()).contains(
      """
      |gcloud:
      |  app: foo.apk
      |  test: instrument.apk
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
      |  - default
      |  num-flaky-test-attempts: 0
      |
      |flank:
      |  keep-file-path: false
      |  ignore-failed-tests: false
      |  disable-sharding: false
      |  smart-flank-disable-upload: false
      |  local-result-dir: defaultDir
      """.trimMargin()
    )
  }
}
