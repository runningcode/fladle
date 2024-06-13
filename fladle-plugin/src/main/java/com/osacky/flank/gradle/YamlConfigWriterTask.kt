package com.osacky.flank.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.file.ProjectLayout
import org.gradle.api.file.RegularFile
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
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
open class YamlConfigWriterTask
  @Inject
  constructor(
    @get:Nested val base: FlankGradleExtension,
    @get:Nested val config: FladleConfig,
    @get:Input val configName: String,
    projectLayout: ProjectLayout,
    objects: ObjectFactory,
  ) : DefaultTask() {
    private val yamlWriter = YamlWriter()

    private val fladleDir =
      projectLayout.fladleDir.map {
        if (configName == "") {
          it
        } else {
          it.dir(configName.toLowerCase(Locale.ROOT))
        }
      }

    @get:Input
    val additionalTestApks: ListProperty<String> =
      objects.listProperty(String::class.java)
        .convention(config.additionalTestApks)

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
      val mergedConfig =
        object : FladleConfig by config {
          override val additionalTestApks: ListProperty<String>
            get() = this@YamlConfigWriterTask.additionalTestApks
        }
      fladleConfigFile.get().asFile.writeText(yamlWriter.createConfigProps(mergedConfig, base))
    }
  }
