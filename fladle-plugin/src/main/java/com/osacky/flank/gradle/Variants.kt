package com.osacky.flank.gradle

import com.android.build.VariantOutput
import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.api.BaseVariantOutput

/**
 * Returns true if this [BaseVariant] matches the variant specified in the [config].
 *
 * If no variant is specified, all variants are considered a match.
 */
fun BaseVariant.isExpectedVariant(config: FladleConfig) =
  !config.variant.isPresent || (config.variant.isPresent && config.variant.get() == this.name)

/**
 * Returns true if this [BaseVariantOutput] matches the ABI specified in the [config].
 *
 * If the config does not specify an ABI, or if the config specifies an ABI but the [BaseVariantOutput]
 * is not filtered by ABI, it is considered a match.
 */
fun BaseVariantOutput.isExpectedAbiOutput(config: FladleConfig): Boolean {
  return !config.abi.isPresent ||
    !filterTypes.contains(VariantOutput.FilterType.ABI.name) ||
    filters.single { it.filterType == VariantOutput.FilterType.ABI.name }.identifier == config.abi.get()
}

/**
 * Returns true if this [BaseVariant] matches the variant specified in the [config].
 *
 * If no variant is specified, all variants are considered a match.
 */
fun BaseVariant.isExpectedVariantInModule(config: FulladleModuleExtension) =
  !config.variant.isPresent || (config.variant.isPresent && this.name.contains(config.variant.get()))
