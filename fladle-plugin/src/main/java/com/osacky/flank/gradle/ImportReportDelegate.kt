@file:Suppress("DEPRECATION")

package com.osacky.flank.gradle

import org.gradle.api.Project
import org.gradle.api.file.Directory
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.TaskProvider
import com.gradle.develocity.agent.gradle.test.ImportJUnitXmlReports as DevelocityImportJUnitXmlReports
import com.gradle.develocity.agent.gradle.test.JUnitXmlDialect as DevelocityJUnitXmlDialect
import com.gradle.enterprise.gradleplugin.test.ImportJUnitXmlReports as GEImportJUnitXmlReports
import com.gradle.enterprise.gradleplugin.test.JUnitXmlDialect as GEJUnitXmlDialect

fun canImportReport(): Boolean = JUnitXmlHandler.canImport()

fun importReport(
  project: Project, flankTaskProvider: TaskProvider<FlankExecutionTask>,
) {
  val enableTestUploads =
    project.providers
      .gradleProperty("com.osacky.fladle.enableTestUploads")
      .getOrElse("true")
      .toBoolean()
  if (enableTestUploads) {
    val resultsProvider: Provider<RegularFile> = project.layout.buildDirectory
      .dir("fladle")
      .flatMap { fladleDir ->
        val localResultsDirProvider: Provider<Directory> = fladleDir
          .dir(flankTaskProvider.flatMap { task -> task.config.localResultsDir })

        localResultsDirProvider.map { localResultsDir -> localResultsDir.file("JUnitReport.xml") }
      }
    JUnitXmlHandler.get()?.register(
      project.tasks,
      flankTaskProvider,
      resultsProvider
    )
  }
}

/** Abstraction over Develocity and GE impls of JUnitXml reporting. */
sealed class JUnitXmlHandler {

  abstract fun register(
    tasks: TaskContainer,
    flankTask: TaskProvider<FlankExecutionTask>,
    reportsFile: Provider<RegularFile>,
  )

  companion object {
    private fun canImport(name: String) = try {
      Class.forName(name)
      true
    } catch (e: ClassNotFoundException) {
      false
    }

    private val canImportDevelocity get() = canImport("com.gradle.develocity.agent.gradle.test.ImportJUnitXmlReports")

    private val canImportGE get() = canImport("com.gradle.enterprise.gradleplugin.test.ImportJUnitXmlReports")

    fun canImport() = canImportDevelocity || canImportGE

    fun get() = if (canImportDevelocity) {
      DevelocityJunitXmlHandler
    } else if (canImportGE) {
      GEJunitXmlHandler
    } else {
      null
    }
  }

  object DevelocityJunitXmlHandler : JUnitXmlHandler() {
    override fun register(
      tasks: TaskContainer,
      flankTask: TaskProvider<FlankExecutionTask>,
      reportsFile: Provider<RegularFile>,
    ) {
      DevelocityImportJUnitXmlReports.register(tasks, flankTask, DevelocityJUnitXmlDialect.ANDROID_FIREBASE).configure {
        reports.setFrom(reportsFile)
      }
    }
  }

  object GEJunitXmlHandler : JUnitXmlHandler() {
    override fun register(
      tasks: TaskContainer,
      flankTask: TaskProvider<FlankExecutionTask>,
      reportsFile: Provider<RegularFile>,
    ) {
      GEImportJUnitXmlReports.register(tasks, flankTask, GEJUnitXmlDialect.ANDROID_FIREBASE).configure {
        reports.setFrom(reportsFile)
      }
    }
  }
}
