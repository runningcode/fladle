package com.osacky.flank.gradle

import de.undercouch.gradle.tasks.download.Download
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Exec

class FlankGradlePlugin : Plugin<Project> {
  override fun apply(target: Project) {
    val extension = target.extensions.create("fladle", FlankGradleExtension::class.java)
    configureTasks(target, extension)
  }

  private fun configureTasks(project: Project, extension: FlankGradleExtension) {
    project.afterEvaluate {
      project.tasks.apply {

        create("downloadFlank", Download::class.java) {
          src("https://github.com/TestArmada/flank/releases/download/v${extension.flankVersion}/flank.jar")
          dest("${project.fladleDir}/flank.jar")
          onlyIfModified(true)
        }

        create("writeConfigProps") {
          doLast {
            file("${project.fladleDir}/flank.yml").writeText(createConfigProps(extension))
          }
        }

        create("runFlank", Exec::class.java) {
          workingDir("${project.fladleDir}/")
          commandLine("java", "-jar", "flank.jar", "firebase", "test", "android", "run")
          environment(mapOf("GOOGLE_APPLICATION_CREDENTIALS" to "${extension.serviceAccountCredentials}"))
          dependsOn("downloadFlank", "writeConfigProps")
        }

        create("flankDoctor", Exec::class.java) {
          workingDir("${project.fladleDir}/")
          commandLine("java", "-jar", "flank.jar", "firebase", "test", "android", "doctor")
          dependsOn("downloadFlank", "writeConfigProps")
        }
      }
    }
  }

  val Project.fladleDir: String
    get() = "$buildDir/fladle"

  private fun createConfigProps(extension: FlankGradleExtension): String {
    return """gcloud:
      |  app: ${extension.debugApk}
      |  test: ${extension.instrumentationApk}
      |  project: ${extension.projectId}
    """.trimMargin()
  }
}