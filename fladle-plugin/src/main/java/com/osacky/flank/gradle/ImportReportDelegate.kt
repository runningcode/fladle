@file:Suppress("DEPRECATION")

package com.osacky.flank.gradle

import org.gradle.api.Project
import com.gradle.enterprise.gradleplugin.test.ImportJUnitXmlReports as GEImportJUnitXmlReports
import com.gradle.enterprise.gradleplugin.test.JUnitXmlDialect as GEJUnitXmlDialect
import com.gradle.develocity.agent.gradle.test.ImportJUnitXmlReports as DevelocityImportJUnitXmlReports
import com.gradle.develocity.agent.gradle.test.JUnitXmlDialect as DevelocityJUnitXmlDialect
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.TaskProvider

fun canImportReport(): Boolean = JUnitXmlHandler.canImport()

fun importReport(project: Project, flankTask: TaskProvider<FlankExecutionTask>) {
  val enableTestUploads = flankTask.get().project.providers
    .gradleProperty("com.osacky.fladle.enableTestUploads")
    .getOrElse("true")
    .toBoolean()
  if (enableTestUploads) {
    JUnitXmlHandler.get()?.register(project.tasks, flankTask, "${project.buildDir}/fladle/${flankTask.get().config.localResultsDir.get()}/JUnitReport.xml")
  }
}

/** Abstraction over Develocity and GE impls of JUnitXml reporting. */
sealed class JUnitXmlHandler {

  abstract fun register(tasks: TaskContainer, flankTask: TaskProvider<FlankExecutionTask>, reportsPath: String)

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
    override fun register(tasks: TaskContainer, flankTask: TaskProvider<FlankExecutionTask>, reportsPath: String) {
      DevelocityImportJUnitXmlReports.register(tasks, flankTask, DevelocityJUnitXmlDialect.ANDROID_FIREBASE).configure {
        this.reports.setFrom(reportsPath)
      }
    }
  }

  object GEJunitXmlHandler : JUnitXmlHandler() {
    override fun register(tasks: TaskContainer, flankTask: TaskProvider<FlankExecutionTask>, reportsPath: String) {
      GEImportJUnitXmlReports.register(tasks, flankTask, GEJUnitXmlDialect.ANDROID_FIREBASE).configure {
        this.reports.setFrom(reportsPath)
      }
    }
  }
}