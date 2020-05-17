package com.osacky.flank.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project

class FlankGradlePlugin : Plugin<Project> {

  override fun apply(target: Project) {
    FladlePluginDelegate().apply(target)
  }
}
