package com.osacky.flank.gradle

import org.gradle.api.DefaultTask
import javax.inject.Inject

open class RunFlankTask @Inject constructor(private val extension: FlankGradleExtension) : DefaultTask() {

  init {
    description = "Runs instrumentation tests using flank on firebase test lab."
    val execTask = project.tasks.named("execFlank")
    dependsOn(execTask)
  }
}