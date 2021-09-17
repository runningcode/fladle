package com.osacky.flank.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.file.ProjectLayout
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault
import java.util.Locale
import javax.inject.Inject

@DisableCachingByDefault(because = "The task writes a small file from in memory properties and does not benefit from caching.")
open class YamlConfigWriterTask @Inject constructor(
  @get:Nested val base: FlankGradleExtension,
  @get:Nested val config: FladleConfig,
  @get:Input val configName: String,
  projectLayout: ProjectLayout
) : DefaultTask() {

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
    fladleDir.get().asFile.mkdirs()
    fladleConfigFile.get().asFile.writeText(yamlWriter.createConfigProps(config, base))
  }
}
