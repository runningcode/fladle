package com.osacky.flank.gradle.validation

import com.google.common.truth.Truth.assertThat
import com.osacky.flank.gradle.integration.gradleRunner
import com.osacky.flank.gradle.integration.writeBuildDotGradle
import com.osacky.flank.gradle.integration.writeEmptyServiceCredential
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class ValidateExclusionsTest {
  @get:Rule
  var testProjectRoot = TemporaryFolder()

  @Test
  fun `should throw an error when mutually exclusive options used`() {
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
      |  testShards.set(1)
      |  maxTestShards.set(2)
      |  flankVersion.set("23.01.0")
      |}
      """.trimMargin(),
    )

    val result =
      testProjectRoot.gradleRunner()
        .withArguments("printYml")
        .buildAndFail()

    assertThat(result.output).contains("FAILED")
    assertThat(result.output).contains("Options testShards and maxTestShards cannot be used together. Choose one of them.")
  }

  @Test
  fun `should throw an error when mutually exclusive options used -- inner config`() {
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
      |  testShards.set(1)
      |  flankVersion.set("21.01.0")
      |  configs {
      |    newSharding {
      |      maxTestShards.set(2)
      |    }
      |  }
      |}
      """.trimMargin(),
    )

    testProjectRoot.writeEmptyServiceCredential()
    val runner = testProjectRoot.gradleRunner()

    runner.withArguments("printYml").build().run {
      assertThat(output).contains("SUCCESS")
    }

    runner.withArguments("printYmlNewSharding").buildAndFail().run {
      assertThat(output).contains("FAILED")
      assertThat(output).contains("Options testShards and maxTestShards cannot be used together. Choose one of them.")
    }
  }
}
