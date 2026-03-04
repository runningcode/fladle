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

/**
 * Returns true if this [Variant] matches the variant specified in the [config].
 *
 * If no variant is specified, all variants are considered a match.
 */
fun Variant.isExpectedVariantInModule(config: FulladleModuleExtension) =
  !config.variant.isPresent || (config.variant.isPresent && this.name.contains(config.variant.get()))
