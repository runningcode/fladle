package com.osacky.flank.gradle

import com.gradle.enterprise.gradleplugin.test.ImportJUnitXmlReports
import com.gradle.enterprise.gradleplugin.test.JUnitXmlDialect
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.TaskProvider

fun canImportReport(): Boolean {
  return try {
    Class.forName("com.gradle.enterprise.gradleplugin.test.ImportJUnitXmlReports")
    true
  } catch (e: ClassNotFoundException) {
    false
  }
}

fun importReport(tasks: TaskContainer, flankTask: TaskProvider<FlankExecutionTask>) {
  val enableTestUploads = flankTask.get().project.providers.gradleProperty("com.osacky.fladle.enableTestUploads").getOrElse("true").toBoolean()
  if (enableTestUploads) {
    ImportJUnitXmlReports.register(tasks, flankTask, JUnitXmlDialect.ANDROID_FIREBASE).configure {
      reports.setFrom("${project.buildDir}/fladle/${flankTask.get().config.localResultsDir.get()}/JUnitReport.xml")
    }
  }
}
