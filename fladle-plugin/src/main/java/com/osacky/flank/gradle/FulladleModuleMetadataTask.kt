package com.osacky.flank.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault

@DisableCachingByDefault(because = "This task writes a small metadata file and does not benefit from caching.")
abstract class FulladleModuleMetadataTask : DefaultTask() {
  @get:Input
  abstract val modulePath: Property<String>

  @get:Input
  abstract val moduleType: Property<String>

  @get:Input
  abstract val moduleEnabled: Property<Boolean>

  @get:Input
  abstract val hasAndroidTestDir: Property<Boolean>

  @get:Input
  @get:Optional
  abstract val maxTestShards: Property<Int>

  @get:Input
  @get:Optional
  abstract val moduleVariant: Property<String>

  @get:Input
  @get:Optional
  abstract val debugApk: Property<String>

  @get:Input
  abstract val clientDetails: MapProperty<String, String>

  @get:Input
  abstract val environmentVariables: MapProperty<String, String>

  @get:Input
  abstract val variants: ListProperty<String>

  @get:OutputFile
  abstract val outputFile: RegularFileProperty

  fun addVariant(
    variantName: String,
    appApkPath: String?,
    testApkPath: String,
    abiName: String?,
  ) {
    val json =
      buildString {
        append("{")
        append("\"variantName\":\"${escapeJson(variantName)}\"")
        append(",\"testApkPath\":\"${escapeJson(testApkPath)}\"")
        if (appApkPath != null) {
          append(",\"appApkPath\":\"${escapeJson(appApkPath)}\"")
        }
        if (abiName != null) {
          append(",\"abiName\":\"${escapeJson(abiName)}\"")
        }
        append("}")
      }
    variants.add(json)
  }

  @TaskAction
  fun writeMetadata() {
    val sb = StringBuilder()
    sb.append("{\n")
    sb.append("  \"modulePath\": \"${escapeJson(modulePath.get())}\",\n")
    sb.append("  \"moduleType\": \"${escapeJson(moduleType.get())}\",\n")
    sb.append("  \"enabled\": ${moduleEnabled.get()},\n")
    sb.append("  \"hasAndroidTestDir\": ${hasAndroidTestDir.get()},\n")

    if (maxTestShards.isPresent) {
      sb.append("  \"maxTestShards\": ${maxTestShards.get()},\n")
    }
    if (moduleVariant.isPresent) {
      sb.append("  \"variant\": \"${escapeJson(moduleVariant.get())}\",\n")
    }
    if (debugApk.isPresent) {
      sb.append("  \"debugApk\": \"${escapeJson(debugApk.get())}\",\n")
    }

    val cd = clientDetails.getOrElse(emptyMap())
    if (cd.isNotEmpty()) {
      sb.append("  \"clientDetails\": {\n")
      cd.entries.forEachIndexed { index, entry ->
        sb.append("    \"${escapeJson(entry.key)}\": \"${escapeJson(entry.value)}\"")
        if (index < cd.size - 1) sb.append(",")
        sb.append("\n")
      }
      sb.append("  },\n")
    }

    val env = environmentVariables.getOrElse(emptyMap())
    if (env.isNotEmpty()) {
      sb.append("  \"environmentVariables\": {\n")
      env.entries.forEachIndexed { index, entry ->
        sb.append("    \"${escapeJson(entry.key)}\": \"${escapeJson(entry.value)}\"")
        if (index < env.size - 1) sb.append(",")
        sb.append("\n")
      }
      sb.append("  },\n")
    }

    val variantList = variants.getOrElse(emptyList())
    sb.append("  \"variants\": [\n")
    variantList.forEachIndexed { index, v ->
      sb.append("    $v")
      if (index < variantList.size - 1) sb.append(",")
      sb.append("\n")
    }
    sb.append("  ]\n")
    sb.append("}\n")

    outputFile.get().asFile.apply {
      parentFile.mkdirs()
      writeText(sb.toString())
    }
  }

  private fun escapeJson(value: String): String =
    value
      .replace("\\", "\\\\")
      .replace("\"", "\\\"")
      .replace("\n", "\\n")
      .replace("\r", "\\r")
      .replace("\t", "\\t")
}
