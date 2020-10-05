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
      |  test-targets:
      |  - override
      |
      |flank:
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
      |  test-targets:
      |  - default
      |
      |flank:
      |  local-result-dir: defaultDir
      """.trimMargin()
    )
  }
}
