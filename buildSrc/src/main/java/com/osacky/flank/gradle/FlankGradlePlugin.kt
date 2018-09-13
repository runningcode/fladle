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
    project.tasks.apply {

      register("downloadFlank", Download::class.java) {
        description = "Downloads flank to the build/fladle directory in the current project."
        src("https://github.com/TestArmada/flank/releases/download/v${extension.flankVersion}/flank.jar")
        dest("${project.fladleDir}/flank.jar")
        onlyIfModified(true)
      }

      register("writeConfigProps") {
        description = "Writes a flank.yml file based on the current FlankGradleExtension configuration."
        doLast {
          project.file("${project.fladleDir}/flank.yml").writeText(YamlWriter().createConfigProps(extension))
        }
      }

      register("runFlank", Exec::class.java) {
        description = "Runs instrumentation tests using flank on firebase test lab."
        workingDir("${project.fladleDir}/")
        commandLine("java", "-jar", "flank.jar", "firebase", "test", "android", "run")
        environment(mapOf("GOOGLE_APPLICATION_CREDENTIALS" to "${extension.serviceAccountCredentials}"))
        dependsOn(named("downloadFlank"), named("writeConfigProps"))
      }

      register("flankDoctor", Exec::class.java) {
        description = "Finds problems with the current configuration."
        workingDir("${project.fladleDir}/")
        commandLine("java", "-jar", "flank.jar", "firebase", "test", "android", "doctor")
        dependsOn(named("downloadFlank"), named("writeConfigProps"))
      }
    }
  }

  private val Project.fladleDir: String
    get() = "$buildDir/fladle"
}