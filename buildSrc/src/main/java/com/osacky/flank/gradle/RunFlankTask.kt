package com.osacky.flank.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Exec
import javax.inject.Inject

open class RunFlankTask @Inject constructor(private val extension: FlankGradleExtension) : DefaultTask() {

  init {
    description = "Runs instrumentation tests using flank on firebase test lab."

    val writeConfigProps = project.tasks.register("writeConfigProps", YamlConfigWriterTask::class.java, extension)

    val execFlank = project.tasks.register("execFlank", Exec::class.java) {
        description = "Runs instrumentation tests using flank on firebase test lab."
        workingDir("${project.fladleDir}/")
        commandLine("java", "-jar", "flank.jar", "firebase", "test", "android", "run")
        environment(mapOf("GOOGLE_APPLICATION_CREDENTIALS" to "${extension.serviceAccountCredentials}"))
        dependsOn(project.tasks.named("downloadFlank"), writeConfigProps)
    }
    dependsOn(execFlank)
  }
}