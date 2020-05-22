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
      |  testTargets = ['default']
      |  configs {
      |    orange {
      |      testTargets.set(['override'])
      |      timeoutMin.set 25
      |      environmentVariables.set([
      |        "anyVariable": "anyValue"
      |      ])
      |      useOrchestrator.set true
      |    }
      |  }
      |}
    """.trimMargin()
    )

    GradleRunner.create()
      .withPluginClasspath()
      .withArguments("writeConfigPropsOrange")
      .withProjectDir(testProjectRoot.root)
      .build()

    val writtenYmlFile = testProjectRoot.root.resolve("build/fladle/flank.yml")
    assertThat(writtenYmlFile.readText()).contains(
      """
      |gcloud:
      |  app: foo.apk
      |  test: instrument.apk
      |  device:
      |  - model: NexusLowRes
      |    version: 28
      |
      |  use-orchestrator: true
      |  auto-google-login: false
      |  record-video: true
      |  performance-metrics: true
      |  timeout: 25m
      |  environment-variables:
      |    anyVariable: anyValue
      |  test-targets:
      |  - override
      |  num-flaky-test-attempts: 0
      |
      |flank:
      |  keep-file-path: false
    """.trimMargin()
    )
  }
}
