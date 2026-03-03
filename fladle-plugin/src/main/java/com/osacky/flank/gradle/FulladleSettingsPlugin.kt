package com.osacky.flank.gradle

import org.gradle.api.Plugin
import org.gradle.api.initialization.Settings

class FulladleSettingsPlugin : Plugin<Settings> {
  override fun apply(settings: Settings) {
    settings.gradle.lifecycle.beforeProject {
      if (this != rootProject) {
        pluginManager.apply(FulladleModulePlugin::class.java)
      }
    }
  }
}
