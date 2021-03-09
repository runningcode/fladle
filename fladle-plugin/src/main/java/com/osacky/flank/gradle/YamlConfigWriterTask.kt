package com.osacky.flank.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.file.ProjectLayout
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import java.util.Locale
import javax.inject.Inject

open class YamlConfigWriterTask @Inject constructor(
  @get:Nested val base: FlankGradleExtension,
  @get:Nested val config: FladleConfig,
  @get:Input val configName: String,
  projectLayout: ProjectLayout
) : DefaultTask() {

  init {
    description = "Writes a flank.yml file based on the current FlankGradleExtension configuration."
    group = FladlePluginDelegate.TASK_GROUP
  }

  private val yamlWriter = YamlWriter()

  private val fladleDir = projectLayout.fladleDir.map {
    if (configName == "") {
      it
    } else {
      it.dir(configName.toLowerCase(Locale.ROOT))
    }
  }

  @OutputFile
  val fladleConfigFile: Provider<RegularFile> = fladleDir.map { it.file("flank.yml") }

  @TaskAction
  fun writeFile() {
    fladleDir.get().asFile.mkdirs()
    fladleConfigFile.get().asFile.writeText(yamlWriter.createConfigProps(config, base))
  }
}
