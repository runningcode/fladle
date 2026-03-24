package com.osacky.flank.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.ProjectLayout
import org.gradle.api.file.RegularFile
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
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
    private val objects: ObjectFactory,
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
      objects
        .listProperty(String::class.java)
        .convention(config.additionalTestApks)

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    @get:Optional
    val moduleMetadataFiles: ConfigurableFileCollection = objects.fileCollection()

    @get:Input
    @get:Optional
    val abiFilter: Property<String> = objects.property(String::class.java)

    @OutputFile
    val fladleConfigFile: Provider<RegularFile> = fladleDir.map { it.file("flank.yml") }

    @Internal
    override fun getDescription(): String = "Writes a flank.yml file based on the current FlankGradleExtension configuration."

    @Internal
    override fun getGroup(): String = FladlePluginDelegate.TASK_GROUP

    @TaskAction
    fun writeFile() {
      fladleDir.get().asFile.mkdirs()

      var metadataDebugApk: String? = null
      var metadataInstrumentationApk: String? = null
      val metadataAdditionalApks = mutableListOf<String>()

      // If module metadata files are present (settings plugin path),
      // parse them and assemble the fulladle config
      val metadataFiles = moduleMetadataFiles.files
      if (metadataFiles.isNotEmpty()) {
        val modules = ModuleMetadataParser.parseModuleMetadata(metadataFiles)
        val assemblyResult = ModuleMetadataParser.assembleFulladleConfig(modules, abiFilter.orNull)

        if (assemblyResult.debugApk != null && !config.debugApk.isPresent) {
          metadataDebugApk = assemblyResult.debugApk
        }
        if (assemblyResult.instrumentationApk != null && !config.instrumentationApk.isPresent) {
          metadataInstrumentationApk = assemblyResult.instrumentationApk
        }
        metadataAdditionalApks.addAll(assemblyResult.additionalTestApks)
      }

      // Create override properties using ObjectFactory (proper Gradle properties)
      val debugApkOverride: Property<String> =
        objects.property(String::class.java).apply {
          if (metadataDebugApk != null) {
            set(metadataDebugApk)
          } else if (config.debugApk.isPresent) {
            set(config.debugApk.get())
          }
        }
      val instrumentationApkOverride: Property<String> =
        objects.property(String::class.java).apply {
          if (metadataInstrumentationApk != null) {
            set(metadataInstrumentationApk)
          } else if (config.instrumentationApk.isPresent) {
            set(config.instrumentationApk.get())
          }
        }

      // Merge configured additional test apks with metadata-based ones
      val mergedAdditionalTestApks: ListProperty<String> =
        objects.listProperty(String::class.java).apply {
          addAll(additionalTestApks.get())
          addAll(metadataAdditionalApks)
        }

      // Create a merged config that overlays metadata values without mutating the extension
      val mergedConfig =
        object : FladleConfig by config {
          override val additionalTestApks: ListProperty<String>
            get() = mergedAdditionalTestApks

          override val debugApk: Property<String>
            get() = debugApkOverride

          override val instrumentationApk: Property<String>
            get() = instrumentationApkOverride
        }

      fladleConfigFile.get().asFile.writeText(yamlWriter.createConfigProps(mergedConfig, mergedConfig))
    }
  }
