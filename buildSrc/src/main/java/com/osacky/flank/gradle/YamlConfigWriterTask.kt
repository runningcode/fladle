package com.osacky.flank.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import javax.inject.Inject

open class YamlConfigWriterTask @Inject constructor(private val extension: FlankGradleExtension) : DefaultTask() {

  private val yamlWriter = YamlWriter()

  override fun getDescription(): String {
    return "Writes a flank.yml file based on the current FlankGradleExtension configuration."
  }

  @TaskAction
  fun writeFile() {
    project.file("${project.fladleDir}/flank.yml").writeText(yamlWriter.createConfigProps(extension))
  }
}