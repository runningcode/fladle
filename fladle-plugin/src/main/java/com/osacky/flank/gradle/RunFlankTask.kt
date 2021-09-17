package com.osacky.flank.gradle

import org.gradle.api.DefaultTask
import org.gradle.work.DisableCachingByDefault
import javax.inject.Inject

@DisableCachingByDefault(because = "Flank executions are dependent on resources such as network connection and server and therefore cannot be cached.")
open class RunFlankTask @Inject constructor() : DefaultTask() {

  init {
    description = "Runs instrumentation tests using flank on firebase test lab."
    group = FladlePluginDelegate.TASK_GROUP
  }
}
