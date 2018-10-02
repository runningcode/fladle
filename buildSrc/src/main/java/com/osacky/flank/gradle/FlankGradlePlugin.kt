package com.osacky.flank.gradle

import com.android.build.gradle.AppExtension
import de.undercouch.gradle.tasks.download.Download
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Exec
import org.gradle.util.GradleVersion

class FlankGradlePlugin : Plugin<Project> {

  override fun apply(target: Project) {
    checkMinimumGradleVersion()
    val extension = target.extensions.create("fladle", FlankGradleExtension::class.java)
    configureTasks(target, extension)
  }

  private fun checkMinimumGradleVersion() {
    // Gradle 4.9 is required because we use the lazy task configuration API.
    if (GRADLE_MIN_VERSION > GradleVersion.current()) {
      throw GradleException("Fladle requires at minimum version $GRADLE_MIN_VERSION. Detected version ${GradleVersion.current()}.")
    }
  }

  private fun configureTasks(project: Project, extension: FlankGradleExtension) {
    project.pluginManager.withPlugin("com.android.application") {
      if (extension.debugApk == null || extension.instrumentationApk == null) {
        findDebugAndInstrumentationApk(project, extension)
      }
    }
    project.tasks.apply {

      register("downloadFlank", Download::class.java) {
        description = "Downloads flank to the build/fladle directory in the current project."
        src("https://github.com/TestArmada/flank/releases/download/${extension.flankVersion}/flank.jar")
        dest("${project.fladleDir}/flank.jar")
        onlyIfModified(true)
      }

      register("printYml") {
        description = "Print the flank.yml file to the console."
        doLast {
          println(YamlWriter().createConfigProps(extension))
        }
      }

      register("runFlank", RunFlankTask::class.java, extension)

      register("flankDoctor", Exec::class.java) {
        description = "Finds problems with the current configuration."
        workingDir("${project.fladleDir}/")
        commandLine("java", "-jar", "flank.jar", "firebase", "test", "android", "doctor")
        dependsOn(named("downloadFlank"), named("writeConfigProps"))
      }
    }
  }

  private fun findDebugAndInstrumentationApk(project: Project, extension: FlankGradleExtension) {
    val baseExtension = project.extensions.findByType(AppExtension::class.java)!!
    baseExtension.applicationVariants.all {
      if (testVariant != null) {
        outputs.all debug@{
          testVariant.outputs.all test@{
            extension.debugApk = this@debug.outputFile.absolutePath
            extension.instrumentationApk = this@test.outputFile.absolutePath
          }
        }
      }
    }
  }

  companion object {
    val GRADLE_MIN_VERSION = GradleVersion.version("4.9")
  }
}