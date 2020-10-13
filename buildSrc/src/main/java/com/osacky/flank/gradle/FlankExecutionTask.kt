package com.osacky.flank.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.file.ProjectLayout
import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.property
import org.gradle.process.ExecOperations
import javax.inject.Inject

open class FlankExecutionTask @Inject constructor(
  objectFactory: ObjectFactory,
  private val projectLayout: ProjectLayout,
  private val execOperations: ExecOperations
) : DefaultTask() {
  @get:Input
  val dumpShards: Property<Boolean> = objectFactory.property<Boolean>().convention(false)

  @get:InputFile
  val serviceAccountCredentials: Property<RegularFile> = objectFactory.fileProperty()

  @Suppress("unused")
  @get:InputFile
  @PathSensitive(PathSensitivity.RELATIVE)
  val yamlFile: RegularFileProperty = objectFactory.fileProperty()

  @get:InputFiles
  @Classpath
  val fladleConfig = objectFactory.fileCollection()

  init {
    group = JavaExec.TASK_GROUP
    description = "Runs instrumentation tests using flank on firebase test lab."

    outputs.upToDateWhen {
      false
    }
  }

  @TaskAction
  fun run() {
    if (serviceAccountCredentials.isPresent) {
      check(serviceAccountCredentials.get().asFile.exists()) { "serviceAccountCredential file doesn't exist ${serviceAccountCredentials.get()}" }
    }

    execOperations.javaexec {
      classpath = fladleConfig
      args = if (dumpShards.get()) {
        listOf("firebase", "test", "android", "run", "--dump-shards")
      } else {
        listOf("firebase", "test", "android", "run")
      }

      if (serviceAccountCredentials.isPresent) {
        environment(mapOf("GOOGLE_APPLICATION_CREDENTIALS" to serviceAccountCredentials.get()))
      }

      main = "ftl.Main"
      workingDir(projectLayout.fladleDir)
    }.assertNormalExitValue()
  }
}
