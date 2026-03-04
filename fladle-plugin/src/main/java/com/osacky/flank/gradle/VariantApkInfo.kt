package com.osacky.flank.gradle

data class VariantApkInfo(
  val variantName: String,
  val appApkPath: String?,
  val testApkPath: String,
  val abiName: String?,
) {
  fun isExpectedVariantInModule(config: FulladleModuleExtension): Boolean =
    !config.variant.isPresent || (config.variant.isPresent && variantName.contains(config.variant.get()))
}
