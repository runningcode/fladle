package com.osacky.flank.gradle

import com.android.build.api.variant.FilterConfiguration
import com.android.build.api.variant.Variant
import com.android.build.api.variant.VariantOutput

/**
 * Returns true if this [Variant] matches the variant specified in the [config].
 *
 * If no variant is specified, all variants are considered a match.
 */
fun Variant.isExpectedVariant(config: FladleConfig) =
  !config.variant.isPresent || (config.variant.isPresent && config.variant.get() == this.name)

/**
 * Returns true if this [VariantOutput] matches the ABI specified in the [config].
 *
 * If the config does not specify an ABI, or if the config specifies an ABI but the [VariantOutput]
 * is not filtered by ABI, it is considered a match.
 */
fun VariantOutput.isExpectedAbiOutput(config: FladleConfig): Boolean {
  val abiFilters = filters.filter { it.filterType == FilterConfiguration.FilterType.ABI }
  return !config.abi.isPresent ||
    abiFilters.isEmpty() ||
    abiFilters.any { it.identifier == config.abi.get() }
}

fun Variant.appApkPath(
  archivesName: String,
  buildType: String,
  abiName: String?,
): String = "${apkDirPath(buildType)}/${appApkFileName(archivesName, buildType, abiName)}"

fun Variant.androidTestApkPath(
  archivesName: String,
  buildType: String,
): String = "androidTest/${apkDirPath(buildType)}/${androidTestApkFileName(archivesName, buildType)}"

private fun Variant.apkDirPath(buildType: String): String {
  val flavorPath = flavorName.orEmpty()
  return if (flavorPath.isNotEmpty()) "$flavorPath/$buildType" else buildType
}

private fun Variant.appApkFileName(
  archivesName: String,
  buildType: String,
  abiName: String?,
): String =
  buildString {
    append(archivesName)
    val flavorName = flavorFileName()
    if (flavorName.isNotEmpty()) append("-$flavorName")
    if (abiName != null) append("-$abiName")
    append("-$buildType.apk")
  }

private fun Variant.androidTestApkFileName(
  archivesName: String,
  buildType: String,
): String =
  buildString {
    append(archivesName)
    val flavorName = flavorFileName()
    if (flavorName.isNotEmpty()) append("-$flavorName")
    append("-$buildType-androidTest.apk")
  }

private fun Variant.flavorFileName(): String = productFlavors.joinToString("-") { it.second }
