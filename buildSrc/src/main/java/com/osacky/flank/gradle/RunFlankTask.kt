package com.osacky.flank.gradle

import org.gradle.api.DefaultTask
import javax.inject.Inject

open class RunFlankTask @Inject constructor(private val extension: FladleConfig) : DefaultTask() {

  init {
    description = "Runs instrumentation tests using flank on firebase test lab."
  }

  private fun named(taskName: String) = project.tasks.named(taskName)
}