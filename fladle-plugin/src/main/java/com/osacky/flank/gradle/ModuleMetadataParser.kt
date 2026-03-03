package com.osacky.flank.gradle

import java.io.File

data class ModuleMetadata(
  val modulePath: String,
  val moduleType: String,
  val enabled: Boolean,
  val hasAndroidTestDir: Boolean,
  val maxTestShards: Int?,
  val variant: String?,
  val debugApk: String?,
  val clientDetails: Map<String, String>,
  val environmentVariables: Map<String, String>,
  val variants: List<VariantMetadata>,
)

data class VariantMetadata(
  val variantName: String,
  val appApkPath: String?,
  val testApkPath: String,
  val abiName: String?,
)

data class FulladleAssemblyResult(
  val debugApk: String?,
  val instrumentationApk: String?,
  val additionalTestApks: List<String>,
)

object ModuleMetadataParser {
  fun parseModuleMetadata(files: Set<File>): List<ModuleMetadata> =
    files
      .filter { it.exists() }
      .map { parseFile(it) }

  fun assembleFulladleConfig(
    modules: List<ModuleMetadata>,
    abiFilter: String?,
  ): FulladleAssemblyResult {
    val enabledModules =
      modules.filter { it.enabled && it.hasAndroidTestDir }

    check(enabledModules.isNotEmpty()) {
      "All modules were disabled for testing in fulladleModuleConfig or the enabled modules had no tests.\n" +
        "Either re-enable modules for testing or add modules with tests."
    }

    // Sort: app modules first, then library modules (alphabetical within each group)
    val sorted =
      enabledModules.sortedWith(
        compareBy<ModuleMetadata> { if (it.moduleType == "application") 0 else 1 }
          .thenBy { it.modulePath },
      )

    var rootDebugApk: String? = null
    var rootInstrumentationApk: String? = null
    val additionalTestApks = mutableListOf<String>()

    for (module in sorted) {
      // Only configure the first matching variant per module
      val matchingVariant = findMatchingVariant(module, abiFilter) ?: continue

      val yml = StringBuilder()

      if (rootDebugApk == null) {
        // First module becomes root
        if (module.moduleType == "application" && matchingVariant.appApkPath != null) {
          rootDebugApk = matchingVariant.appApkPath
        } else if (module.moduleType == "library") {
          check(module.debugApk != null && module.debugApk.isNotEmpty()) {
            "Library module ${module.modulePath} did not specify a debug apk. Library modules do not " +
              "generate a debug apk and one needs to be specified in the fulladleModuleConfig block\n" +
              "This is a required parameter in FTL which remains unused for library modules under test, " +
              "and you can use a dummy apk here"
          }
          rootDebugApk = module.debugApk
        }
      } else {
        // Additional module
        if (module.moduleType == "application" && matchingVariant.appApkPath != null) {
          yml.appendLine("- app: ${matchingVariant.appApkPath}")
        } else if (module.moduleType == "library") {
          if (module.debugApk != null && module.debugApk.isNotEmpty()) {
            yml.appendLine("- app: ${module.debugApk}")
          }
        }
      }

      if (rootInstrumentationApk == null) {
        rootInstrumentationApk = matchingVariant.testApkPath
      } else {
        if (yml.isBlank()) {
          yml.appendLine("- test: ${matchingVariant.testApkPath}")
        } else {
          yml.appendLine("      test: ${matchingVariant.testApkPath}")
        }
      }

      if (yml.isEmpty()) {
        // This is the root module - apply overrides
        // (overrides are returned via the rootDebugApk/rootInstrumentationApk above)
      } else {
        // Append per-module overrides
        val maxTestShards = module.maxTestShards
        if (maxTestShards != null && maxTestShards > 0) {
          yml.appendLine("    max-test-shards: $maxTestShards")
        }
        if (module.clientDetails.isNotEmpty()) {
          yml.appendLine("    client-details:")
          module.clientDetails.forEach { (key, value) ->
            yml.appendLine("        $key: $value")
          }
        }
        if (module.environmentVariables.isNotEmpty()) {
          yml.appendLine("    environment-variables:")
          module.environmentVariables.forEach { (key, value) ->
            yml.appendLine("        $key: $value")
          }
        }
        additionalTestApks.add(yml.toString())
      }
    }

    return FulladleAssemblyResult(
      debugApk = rootDebugApk,
      instrumentationApk = rootInstrumentationApk,
      additionalTestApks = additionalTestApks,
    )
  }

  private fun findMatchingVariant(
    module: ModuleMetadata,
    abiFilter: String?,
  ): VariantMetadata? {
    for (variant in module.variants) {
      // Check variant filter
      if (module.variant != null && !variant.variantName.contains(module.variant)) continue

      // Check ABI filter
      if (abiFilter != null && variant.abiName != null && variant.abiName != abiFilter) continue

      return variant
    }
    return null
  }

  // Simple JSON parser - no external dependency needed
  internal fun parseFile(file: File): ModuleMetadata {
    val content = file.readText()
    return parseJson(content)
  }

  internal fun parseJson(json: String): ModuleMetadata {
    val modulePath = extractString(json, "modulePath") ?: ""
    val moduleType = extractString(json, "moduleType") ?: ""
    val enabled = extractBoolean(json, "enabled") ?: true
    val hasAndroidTestDir = extractBoolean(json, "hasAndroidTestDir") ?: false
    val maxTestShards = extractInt(json, "maxTestShards")
    val variant = extractString(json, "variant")
    val debugApk = extractString(json, "debugApk")
    val clientDetails = extractMap(json, "clientDetails")
    val environmentVariables = extractMap(json, "environmentVariables")
    val variants = extractVariants(json)

    return ModuleMetadata(
      modulePath = modulePath,
      moduleType = moduleType,
      enabled = enabled,
      hasAndroidTestDir = hasAndroidTestDir,
      maxTestShards = maxTestShards,
      variant = variant,
      debugApk = debugApk,
      clientDetails = clientDetails,
      environmentVariables = environmentVariables,
      variants = variants,
    )
  }

  private fun extractString(
    json: String,
    key: String,
  ): String? {
    val pattern = "\"$key\"\\s*:\\s*\"((?:[^\"\\\\]|\\\\.)*)\"".toRegex()
    val match = pattern.find(json) ?: return null
    return unescapeJson(match.groupValues[1])
  }

  private fun extractBoolean(
    json: String,
    key: String,
  ): Boolean? {
    val pattern = "\"$key\"\\s*:\\s*(true|false)".toRegex()
    val match = pattern.find(json) ?: return null
    return match.groupValues[1].toBoolean()
  }

  private fun extractInt(
    json: String,
    key: String,
  ): Int? {
    val pattern = "\"$key\"\\s*:\\s*(-?\\d+)".toRegex()
    val match = pattern.find(json) ?: return null
    return match.groupValues[1].toInt()
  }

  private fun extractMap(
    json: String,
    key: String,
  ): Map<String, String> {
    val result = mutableMapOf<String, String>()
    val mapPattern = "\"$key\"\\s*:\\s*\\{([^}]*)}".toRegex()
    val mapMatch = mapPattern.find(json) ?: return result
    val mapContent = mapMatch.groupValues[1]
    val entryPattern = "\"((?:[^\"\\\\]|\\\\.)*)\"\\s*:\\s*\"((?:[^\"\\\\]|\\\\.)*)\"".toRegex()
    entryPattern.findAll(mapContent).forEach { entry ->
      result[unescapeJson(entry.groupValues[1])] = unescapeJson(entry.groupValues[2])
    }
    return result
  }

  private fun extractVariants(json: String): List<VariantMetadata> {
    val variants = mutableListOf<VariantMetadata>()
    val arrayPattern = "\"variants\"\\s*:\\s*\\[([^\\]]*)]".toRegex(RegexOption.DOT_MATCHES_ALL)
    val arrayMatch = arrayPattern.find(json) ?: return variants
    val arrayContent = arrayMatch.groupValues[1]

    // Match each object in the array
    val objectPattern = "\\{([^}]*)}".toRegex()
    objectPattern.findAll(arrayContent).forEach { objMatch ->
      val obj = objMatch.value
      val variantName = extractString(obj, "variantName") ?: return@forEach
      val testApkPath = extractString(obj, "testApkPath") ?: return@forEach
      val appApkPath = extractString(obj, "appApkPath")
      val abiName = extractString(obj, "abiName")
      variants.add(
        VariantMetadata(
          variantName = variantName,
          appApkPath = appApkPath,
          testApkPath = testApkPath,
          abiName = abiName,
        ),
      )
    }
    return variants
  }

  private fun unescapeJson(value: String): String =
    value
      .replace("\\\\", "\\")
      .replace("\\\"", "\"")
      .replace("\\n", "\n")
      .replace("\\r", "\r")
      .replace("\\t", "\t")
}
