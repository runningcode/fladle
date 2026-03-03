package com.osacky.flank.gradle

import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import com.android.build.api.variant.FilterConfiguration
import com.android.build.api.variant.HasAndroidTest
import com.android.build.api.variant.LibraryAndroidComponentsExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.attributes.Attribute
import org.gradle.api.plugins.BasePluginExtension

class FulladleModulePlugin : Plugin<Project> {
  override fun apply(project: Project) {
    // Create the per-module extension if not already present
    if (project.extensions.findByType(FulladleModuleExtension::class.java) == null) {
      project.extensions.create("fulladleModuleConfig", FulladleModuleExtension::class.java)
    }

    val metadataTask =
      project.tasks.register("writeFulladleModuleMetadata", FulladleModuleMetadataTask::class.java) {
        modulePath.set(project.path)
        moduleType.convention("none")
        moduleEnabled.convention(true)
        hasAndroidTestDir.convention(false)
        clientDetails.convention(emptyMap())
        environmentVariables.convention(emptyMap())
        variants.convention(emptyList())
        outputFile.set(project.layout.buildDirectory.file("fulladle/module-metadata.json"))
      }

    // Wire module extension properties into the metadata task
    project.afterEvaluate {
      val ext = project.extensions.findByType(FulladleModuleExtension::class.java) ?: return@afterEvaluate
      metadataTask.configure {
        moduleEnabled.set(ext.enabled)
        hasAndroidTestDir.set(project.file("src/androidTest").exists())
        if (ext.maxTestShards.isPresent) {
          maxTestShards.set(ext.maxTestShards)
        }
        if (ext.variant.isPresent) {
          moduleVariant.set(ext.variant)
        }
        if (ext.debugApk.isPresent) {
          debugApk.set(ext.debugApk)
        }
        clientDetails.set(ext.clientDetails)
        environmentVariables.set(ext.environmentVariables)
      }
    }

    // Create outgoing configuration for metadata
    val outgoing =
      project.configurations.create("fulladleModuleMetadataElements") {
        isCanBeConsumed = true
        isCanBeResolved = false
        attributes {
          attribute(FULLADLE_USAGE_ATTRIBUTE, "fulladle-module-metadata")
        }
      }
    outgoing.outgoing.artifact(metadataTask.flatMap { it.outputFile })

    // Wire up variant APK info from Android plugins
    project.pluginManager.withPlugin("com.android.application") {
      metadataTask.configure { moduleType.set("application") }
      val androidComponents = project.extensions.getByType(ApplicationAndroidComponentsExtension::class.java)
      androidComponents.onVariants { variant ->
        val androidTest = (variant as? HasAndroidTest)?.androidTest ?: return@onVariants
        val buildType = variant.buildType ?: return@onVariants
        val flavorName = variant.productFlavors.joinToString("") { it.second }
        val flavorPath = variant.productFlavors.joinToString("/") { it.second }
        val archivesName =
          project.extensions
            .getByType(BasePluginExtension::class.java)
            .archivesName
            .get()

        variant.outputs.forEach { output ->
          val abiFilter = output.filters.firstOrNull { it.filterType == FilterConfiguration.FilterType.ABI }
          val abiName = abiFilter?.identifier

          val appApkDirPath = if (flavorPath.isNotEmpty()) "$flavorPath/$buildType" else buildType
          val appApkFileName =
            buildString {
              append(archivesName)
              if (flavorName.isNotEmpty()) append("-$flavorName")
              if (abiName != null) append("-$abiName")
              append("-$buildType.apk")
            }
          val appApkPath =
            project.layout.buildDirectory
              .file("outputs/apk/$appApkDirPath/$appApkFileName")
              .get()
              .asFile.absolutePath

          val testApkDirPath =
            if (flavorPath.isNotEmpty()) "androidTest/$flavorPath/$buildType" else "androidTest/$buildType"
          val testApkFileName =
            if (flavorName.isNotEmpty()) {
              "$archivesName-$flavorName-$buildType-androidTest.apk"
            } else {
              "$archivesName-$buildType-androidTest.apk"
            }
          val testApkPath =
            project.layout.buildDirectory
              .file("outputs/apk/$testApkDirPath/$testApkFileName")
              .get()
              .asFile.absolutePath

          metadataTask.configure {
            addVariant(variant.name, appApkPath, testApkPath, abiName)
          }
        }
      }
    }

    project.pluginManager.withPlugin("com.android.library") {
      metadataTask.configure { moduleType.set("library") }
      val androidComponents = project.extensions.getByType(LibraryAndroidComponentsExtension::class.java)
      androidComponents.onVariants { variant ->
        val androidTest = (variant as? HasAndroidTest)?.androidTest ?: return@onVariants
        val buildType = variant.buildType ?: return@onVariants
        val flavorName = variant.productFlavors.joinToString("") { it.second }
        val flavorPath = variant.productFlavors.joinToString("/") { it.second }
        val archivesName =
          project.extensions
            .getByType(BasePluginExtension::class.java)
            .archivesName
            .get()

        val testApkDirPath =
          if (flavorPath.isNotEmpty()) "androidTest/$flavorPath/$buildType" else "androidTest/$buildType"
        val testApkFileName =
          if (flavorName.isNotEmpty()) {
            "$archivesName-$flavorName-$buildType-androidTest.apk"
          } else {
            "$archivesName-$buildType-androidTest.apk"
          }
        val testApkPath =
          project.layout.buildDirectory
            .file("outputs/apk/$testApkDirPath/$testApkFileName")
            .get()
            .asFile.absolutePath

        metadataTask.configure {
          addVariant(variant.name, null, testApkPath, null)
        }
      }
    }
  }

  companion object {
    val FULLADLE_USAGE_ATTRIBUTE: Attribute<String> = Attribute.of("com.osacky.fulladle.usage", String::class.java)
  }
}
