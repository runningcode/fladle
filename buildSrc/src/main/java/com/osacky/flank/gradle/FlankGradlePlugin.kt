package com.osacky.flank.gradle

import de.undercouch.gradle.tasks.download.Download
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Copy
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
          dest("${project.buildDir}/fladle/flank.jar")
          onlyIfModified(true)
        }

        create("writeConfigProps") {
          doLast {
            file("${project.buildDir}/fladle/flank.yml").writeText(createConfigProps(extension, project))
          }
        }

        create("runFlank", Exec::class.java) {
          workingDir("${project.buildDir}/fladle/")
          // java -jar Flank-2.0.3.jar -a path/to/debug.apk -t path/to/test-debug.apk
          commandLine("java", "-jar", "flank.jar", "firebase", "test", "android", "run")
          environment(mapOf("GOOGLE_APPLICATION_CREDENTIALS" to "${extension.serviceAccountCredentials}"))
          dependsOn("downloadFlank", "writeConfigProps")
        }

        create("flankDoctor", Exec::class.java) {
          workingDir("${project.buildDir}/fladle/")
          commandLine("java", "-jar", "flank.jar", "firebase", "test", "android", "doctor")
          dependsOn("downloadFlank", "writeConfigProps")

        }
      }
    }
  }

  val Project.fladleDir : String
    get() = "$buildDir/fladle"

  val Project.gcloudBin : String
    get() = "$fladleDir/gcloud/google-cloud-sdk/bin"

  private fun createConfigProps(extension: FlankGradleExtension, project: Project) : String {
    return """gcloud:
      |  app: ${project.buildDir}/outputs/apk/debug/app-debug.apk
      |  test: ${project.buildDir}/outputs/apk/androidTest/debug/app-debug-androidTest.apk
      |  project: ${extension.projectId}
    """.trimMargin()
  }
}