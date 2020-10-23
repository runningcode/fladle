package com.osacky.flank.gradle

import org.gradle.api.file.ProjectLayout
import org.gradle.api.tasks.JavaExec
import javax.inject.Inject

open class FlankJavaExec @Inject constructor(projectLayout: ProjectLayout) : JavaExec() {
  init {
    group = TASK_GROUP
    main = "ftl.Main"
    workingDir(projectLayout.fladleDir)
  }

  fun setUpWorkingDir(configName: String) = workingDir(project.layout.buildDirectory.dir("fladle/$configName"))
}
