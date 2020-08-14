package com.osacky.flank.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.file.ProjectLayout
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import java.io.File
import javax.inject.Inject

open class YamlConfigWriterTask @Inject constructor(private val config: FladleConfig, private val extension: FlankGradleExtension, projectLayout: ProjectLayout) : DefaultTask() {

  private val yamlWriter = YamlWriter()

  private val fladleDir = projectLayout.fladleDir.get().asFile

  @OutputFile
  val fladleConfigFile: File = fladleDir.resolve("flank.yml")

  @Internal
  override fun getDescription(): String {
    return "Writes a flank.yml file based on the current FlankGradleExtension configuration."
  }

  @Internal
  override fun getGroup(): String {
    return FladlePluginDelegate.TASK_GROUP
  }

  @TaskAction
  fun writeFile() {
    fladleDir.mkdirs()
    fladleConfigFile.writeText(yamlWriter.createConfigProps(config, extension))
  }
}
