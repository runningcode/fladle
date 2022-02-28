package com.osacky.flank.gradle.integration

import com.google.common.truth.Truth.assertThat
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class FlankAuthTestTask {
  @get:Rule
  var testProjectRoot = TemporaryFolder()

  @Ignore("Flaky. See https://github.com/runningcode/fladle/issues/245")
  @Test
  fun testFlankAuth() {
    // We set a task timeout because running flankAuth opens a link in the web browser.
    // We want the task to fail from the timeout and not any other reason like a missing folder.
    testProjectRoot.writeBuildDotGradle(
      """plugins {
         |  id "com.osacky.fladle"
         |}
         |repositories {
         |  mavenCentral()
         |}
         |
         |tasks.named("flankAuth").configure {
         |  timeout.set(Duration.ofSeconds(5))
         |}
         |
      """.trimMargin()
    )

    val result = testProjectRoot.gradleRunner().withArguments("flankAuth").buildAndFail()

    assertThat(result.output).contains("Visit the following URL in your browser:")
  }
}
