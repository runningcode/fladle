package com.osacky.flank.gradle

import org.gradle.api.file.ProjectLayout
import javax.inject.Inject

open class FlankExecutionTask @Inject constructor(projectLayout: ProjectLayout, private val config: FladleConfig) : FlankJavaExec(projectLayout) {

  init {
    doFirst {
      checkFilesExist(config)
    }
  }

  private fun checkFilesExist(base: FladleConfig) {
    if (base.serviceAccountCredentials.isPresent) {
      check(base.serviceAccountCredentials.get().asFile.exists()) { "serviceAccountCredential file doesn't exist ${base.serviceAccountCredentials.get()}" }
    }
  }
}
