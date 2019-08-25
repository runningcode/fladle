package com.osacky.flank.gradle

import javax.inject.Inject
import org.gradle.api.DefaultTask

open class RunFlankTask @Inject constructor() : DefaultTask() {

  init {
    description = "Runs instrumentation tests using flank on firebase test lab."
    group = FlankGradlePlugin.TASK_GROUP
  }
}
