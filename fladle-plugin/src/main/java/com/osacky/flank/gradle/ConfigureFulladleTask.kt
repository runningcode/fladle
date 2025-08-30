package com.osacky.flank.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction

/**
 * Configuration cache compatible version of the configureFulladle task.
 *
 * This task uses serializable data structures collected at configuration time,
 * eliminating the need for live Project object references during task execution.
 */
abstract class ConfigureFulladleTask : DefaultTask() {
  @get:Input
  abstract val moduleInformation: ListProperty<ModuleInfo>

  @get:Internal
  abstract val flankExtension: Property<FlankGradleExtension>

  @TaskAction
  fun configure() {
    val modules = moduleInformation.get()
    val flankGradleExtension = flankExtension.get()

    var modulesEnabled = false

    // First configure all app modules
    modules.filter { it.isAndroidApp && it.enabled && it.hasTests }
      .forEach { moduleInfo ->
        modulesEnabled = true
        configureModule(moduleInfo, flankGradleExtension)
      }

    // Then configure all library modules
    modules.filter { it.isAndroidLibrary && it.enabled && it.hasTests }
      .forEach { moduleInfo ->
        modulesEnabled = true
        configureModule(moduleInfo, flankGradleExtension)
      }

    check(modulesEnabled) {
      "All modules were disabled for testing in fulladleModuleConfig or the enabled modules had no tests.\n" +
        "Either re-enable modules for testing or add modules with tests."
    }
  }

  /**
   * Configure a module using serializable data instead of live Project objects.
   * This mirrors the logic from the original configureModule function but operates
   * on serialized data structures.
   */
  private fun configureModule(
    moduleInfo: ModuleInfo,
    flankGradleExtension: FlankGradleExtension,
  ) {
    if (!moduleInfo.hasTests) {
      return
    }

    // Process each variant for this module (only the first matching one)
    // Only configure the first test variant per module.
    // Does anyone test more than one variant per module?
    var addedTestsForModule = false

    moduleInfo.variants.forEach { variantInfo ->
      if (addedTestsForModule) {
        return
      }
      if (isExpectedVariantInModule(variantInfo, moduleInfo.config)) {
        variantInfo.outputs.forEach { appOutput ->
          if (isExpectedAbiOutput(appOutput, flankGradleExtension)) {
            variantInfo.testOutputs.forEach testOutput@{ testOutput ->

              val yml = StringBuilder()

              // If the debugApk isn't yet set, let's use this one.
              if (!flankGradleExtension.debugApk.isPresent) {
                if (moduleInfo.isAndroidApp) {
                  // app modules produce app apks that we can consume
                  flankGradleExtension.debugApk.set(appOutput.outputFilePath)
                } else if (moduleInfo.isAndroidLibrary) {
                  // library modules do not produce an app apk and we'll use the one specified in fulladleModuleConfig block
                  check(moduleInfo.config.debugApk != null) {
                    "Library module ${moduleInfo.projectPath} did not specify a debug apk. Library modules do not " +
                      "generate a debug apk and one needs to be specified in the fulladleModuleConfig block\n" +
                      "This is a required parameter in FTL which remains unused for library modules under test, " +
                      "and you can use a dummy apk here"
                  }
                  flankGradleExtension.debugApk.set(moduleInfo.config.debugApk!!)
                }
              } else {
                // Otherwise, let's just add it to the list.
                if (moduleInfo.isAndroidApp) {
                  yml.appendLine("- app: ${appOutput.outputFilePath}")
                } else if (moduleInfo.isAndroidLibrary) {
                  // app apk is not required for library modules so only use if it's explicitly specified
                  if (moduleInfo.config.debugApk != null) {
                    yml.appendLine("- app: ${moduleInfo.config.debugApk}")
                  }
                }
              }

              // If the instrumentation apk isn't yet set, let's use this one.
              if (!flankGradleExtension.instrumentationApk.isPresent) {
                flankGradleExtension.instrumentationApk.set(testOutput.outputFilePath)
              } else {
                // Otherwise, let's just add it to the list.
                if (yml.isBlank()) {
                  // The first item in the list needs to start with a ` - `.
                  yml.appendLine("- test: ${testOutput.outputFilePath}")
                } else {
                  yml.appendLine("      test: ${testOutput.outputFilePath}")
                }
              }

              if (yml.isEmpty()) {
                // this is the root module
                // should not be added as additional test apk
                overrideRootLevelConfigs(flankGradleExtension, moduleInfo.config)
              } else {
                yml.appendProperty(moduleInfo.config.maxTestShards, "  max-test-shards")
                yml.appendMapProperty(
                  moduleInfo.config.clientDetails,
                  "  client-details",
                ) { yml.appendLine("    ${it.key}: ${it.value}") }
                yml.appendMapProperty(
                  moduleInfo.config.environmentVariables,
                  "  environment-variables",
                ) { yml.appendLine("    ${it.key}: ${it.value}") }
                flankGradleExtension.additionalTestApks.add(yml.toString())
              }
              addedTestsForModule = true
            }
          }
        }
      }
    }
  }

  /**
   * Check if the variant matches the expected variant for this module.
   */
  private fun isExpectedVariantInModule(
    variantInfo: VariantInfo,
    config: SerializableModuleConfig,
  ): Boolean {
    return config.variant == null || variantInfo.name.contains(config.variant)
  }

  /**
   * Check if the output matches the expected ABI.
   */
  private fun isExpectedAbiOutput(
    output: VariantOutputInfo,
    config: FladleConfig,
  ): Boolean {
    return !config.abi.isPresent ||
      output.filterType != "ABI" ||
      output.identifier == config.abi.get()
  }

  /**
   * Override root level configurations with module-specific values.
   */
  private fun overrideRootLevelConfigs(
    flankGradleExtension: FlankGradleExtension,
    moduleConfig: SerializableModuleConfig,
  ) {
    // if the root module overrode any value in its fulladleModuleConfig block
    // then use those values instead
    val debugApk = moduleConfig.debugApk
    if (debugApk != null && debugApk.isNotEmpty()) {
      flankGradleExtension.debugApk.set(debugApk)
    }
    val maxTestShards = moduleConfig.maxTestShards
    if (maxTestShards != null && maxTestShards > 0) {
      flankGradleExtension.maxTestShards.set(maxTestShards)
    }
    val clientDetails = moduleConfig.clientDetails
    if (clientDetails.isNotEmpty()) {
      flankGradleExtension.clientDetails.set(clientDetails)
    }
    val env = moduleConfig.environmentVariables
    if (env.isNotEmpty()) {
      flankGradleExtension.environmentVariables.set(env)
    }
  }
}

/**
 * Extension function to append a property to YAML if it exists.
 */
private fun StringBuilder.appendProperty(
  value: Any?,
  propertyName: String,
) {
  if (value != null) {
    appendLine("$propertyName: $value")
  }
}

/**
 * Extension function to append a map property to YAML if it exists.
 */
private fun <K, V> StringBuilder.appendMapProperty(
  map: Map<K, V>,
  propertyName: String,
  itemAppender: StringBuilder.(Map.Entry<K, V>) -> Unit,
) {
  if (map.isNotEmpty()) {
    appendLine(propertyName + ":")
    map.forEach { entry ->
      itemAppender(entry)
    }
  }
}
