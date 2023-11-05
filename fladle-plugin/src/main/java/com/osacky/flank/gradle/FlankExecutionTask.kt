package com.osacky.flank.gradle

import org.gradle.api.file.ProjectLayout
import org.gradle.api.tasks.Nested
import org.gradle.work.DisableCachingByDefault
import javax.inject.Inject

@DisableCachingByDefault(
  because = "Flank executions are dependent on resources such as network connection and server and therefore cannot be cached.",
)
open class FlankExecutionTask
  @Inject
  constructor(
    projectLayout: ProjectLayout,
    @get:Nested val config: FladleConfig,
  ) : FlankJavaExec(projectLayout) {
    init {
      doFirst {
        checkFilesExist(config)
      }
    }

    private fun checkFilesExist(base: FladleConfig) {
      if (base.serviceAccountCredentials.isPresent) {
        check(base.serviceAccountCredentials.get().asFile.exists()) {
          "serviceAccountCredential file doesn't exist ${base.serviceAccountCredentials.get()}"
        }
      }
    }
  }
