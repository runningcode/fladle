package com.osacky.flank.gradle.validation

import com.google.common.truth.Truth.assertThat
import com.osacky.flank.gradle.FladleConfig
import com.osacky.flank.gradle.FlankGradleExtension
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Assert.assertThrows
import org.junit.Test

class ValidateOptionsTest {

  @Test
  fun `should throw an error when unavailable option used`() {
    val config: FladleConfig = FlankGradleExtension(ProjectBuilder.builder().withName("project").build().objects)
    config.networkProfile.set("LET")

    val exception = assertThrows(IllegalStateException::class.java) { config.validateOptionsUsed() }
    assertThat(exception.message).containsMatch("Option networkProfile is available since flank 21.0.0, which is higher than used [0-9]*")
  }

  @Test
  fun `should not throw an error when available option used`() {
    val config: FladleConfig = FlankGradleExtension(ProjectBuilder.builder().withName("project").build().objects)
    config.testRunnerClass.set("any")

    config.validateOptionsUsed()
  }
}
