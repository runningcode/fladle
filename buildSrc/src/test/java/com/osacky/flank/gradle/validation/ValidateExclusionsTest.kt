package com.osacky.flank.gradle.validation

import com.google.common.truth.Truth.assertThat
import com.osacky.flank.gradle.FladleConfig
import com.osacky.flank.gradle.FlankGradleExtension
import com.osacky.flank.gradle.integration.gradleRunner
import com.osacky.flank.gradle.integration.writeBuildDotGradle
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class ValidateExclusionsTest {
  @get:Rule
  var testProjectRoot = TemporaryFolder()

  private val objects = ProjectBuilder.builder().withName("project").build().objects
  private lateinit var config: FladleConfig

  @Before
  fun setUp() {
    testProjectRoot.newFile("flank-gradle-service.json").writeText("{}")
    config = FlankGradleExtension(objects)
  }

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
      |  flankVersion.set("21.01.0")
      |}
    """.trimMargin()
    )

    val runner = testProjectRoot.gradleRunner()

    runner.withArguments("printYml").buildAndFail().run {
      assertThat(output).contains("FAILED")
      assertThat(output).contains("Options testShards and maxTestShards cannot be used both! Choose one of them.")
    }
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
    """.trimMargin()
    )

    val runner = testProjectRoot.gradleRunner()

    runner.withArguments("printYml").buildAndFail().run {
      assertThat(output).contains("FAILED")
      assertThat(output).contains("Options testShards and maxTestShards cannot be used both! Choose one of them.")
    }

    runner.withArguments("printYmlNewSharding").buildAndFail().run {
      assertThat(output).contains("FAILED")
      assertThat(output).contains("Options testShards and maxTestShards cannot be used both! Choose one of them.")
    }
  }
}
