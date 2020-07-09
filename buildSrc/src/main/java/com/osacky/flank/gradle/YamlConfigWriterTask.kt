package com.osacky.flank.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import javax.inject.Inject

open class YamlConfigWriterTask @Inject constructor(private val config: FladleConfig, private val extension: FlankGradleExtension) : DefaultTask() {

  private val yamlWriter = YamlWriter()

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
    val fladleDir = project.fladleDir.get().asFile
    if (!fladleDir.exists()) {
      fladleDir.mkdirs()
    }
    fladleDir.resolve("flank.yml").writeText(yamlWriter.createConfigProps(config, extension))
  }
}
