package com.osacky.flank.gradle

import java.io.Serializable

/**
 * Serializable representation of module information collected during configuration time.
 * This enables configuration cache compatibility by avoiding live Project object references.
 */
data class ModuleInfo(
  val projectPath: String,
  val isAndroidApp: Boolean,
  val isAndroidLibrary: Boolean,
  val hasTests: Boolean,
  val enabled: Boolean,
  val config: SerializableModuleConfig,
  val variants: List<VariantInfo>,
) : Serializable

/**
 * Serializable representation of FulladleModuleExtension configuration.
 */
data class SerializableModuleConfig(
  val maxTestShards: Int?,
  val clientDetails: Map<String, String>,
  val environmentVariables: Map<String, String>,
  val debugApk: String?,
  val variant: String?,
) : Serializable

/**
 * Serializable representation of Android test variant information.
 */
data class VariantInfo(
  val name: String,
  val testedVariantName: String,
  val outputs: List<VariantOutputInfo>,
  val testOutputs: List<VariantOutputInfo>,
) : Serializable

/**
 * Serializable representation of variant output information.
 */
data class VariantOutputInfo(
  val outputFilePath: String,
  val filterType: String?,
  val identifier: String?,
) : Serializable

/**
 * Serializable representation of FlankGradleExtension configuration.
 */
data class SerializableFlankConfig(
  val debugApkPresent: Boolean,
  val instrumentationApkPresent: Boolean,
  val additionalTestApks: MutableList<String>,
) : Serializable {
  companion object {
    fun from(extension: FlankGradleExtension): SerializableFlankConfig {
      return SerializableFlankConfig(
        debugApkPresent = extension.debugApk.isPresent,
        instrumentationApkPresent = extension.instrumentationApk.isPresent,
        additionalTestApks = extension.additionalTestApks.get().toMutableList(),
      )
    }
  }
}
