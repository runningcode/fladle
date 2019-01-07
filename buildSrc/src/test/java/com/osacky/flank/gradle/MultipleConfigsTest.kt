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
    file.writeText("""
      |plugins {
      |  id "com.osacky.fladle"
      |}
      |
      |fladle {
      |  serviceAccountCredentials("flank-gradle-service.json")
      |  debugApk("foo.apk")
      |  instrumentationApk("instrument.apk")
      |
      |  testTargets = ['default']
      |  configs {
      |    orange {
      |      testTargets = ['override']
      |    }
      |  }
      |}
    """.trimMargin())
    val result = GradleRunner.create()
        .withPluginClasspath()
        .withArguments("printYmlOrange")
        .withProjectDir(testProjectRoot.root)
        .build()

    assertThat(result.output).contains("""
      |gcloud:
      |  app: foo.apk
      |  test: instrument.apk
      |  use-orchestrator: false
      |  auto-google-login: false
      |  environment-variables:
      |    clearPackageData: false
      |# projectId will be automatically discovered
      |  device:
      |  - model: NexusLowRes
      |    version: 28
      |
      |  test-targets:
      |  - override
      |
    """.trimMargin())
  }
}