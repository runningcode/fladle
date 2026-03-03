package com.osacky.flank.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Like the Fladle plugin, but it configures additionalTestApks for the _full_ project. Hence fulladle.
 *
 * When the settings plugin (com.osacky.fulladle.settings) is applied, module metadata flows
 * through Gradle's configuration system via JSON files. When it is not applied, the legacy
 * subprojects {} approach is used for backwards compatibility.
 */
class FulladlePlugin : Plugin<Project> {
  override fun apply(root: Project) {
    check(root.parent == null) { "Fulladle must be applied in the root project in order to configure subprojects." }
    FladlePluginDelegate().apply(root)

    val flankGradleExtension = root.extensions.getByType(FlankGradleExtension::class.java)

    // Detect whether the settings plugin is in use by checking if any subproject
    // has the module plugin applied (which the settings plugin does automatically).
    // We defer this check to afterEvaluate so that all plugins have been applied.
    root.afterEvaluate {
      val settingsPluginActive =
        root.subprojects.any { sub ->
          sub.plugins.hasPlugin(FulladleModulePlugin::class.java)
        }

      if (settingsPluginActive) {
        configureWithMetadata(root, flankGradleExtension)
      } else {
        configureLegacy(root, flankGradleExtension)
      }
    }
  }

  private fun configureWithMetadata(
    root: Project,
    flankGradleExtension: FlankGradleExtension,
  ) {
    // Create incoming configuration to collect metadata from subprojects
    val incoming =
      root.configurations.create("fulladleModuleMetadata") {
        isCanBeConsumed = false
        isCanBeResolved = true
        attributes {
          attribute(FulladleModulePlugin.FULLADLE_USAGE_ATTRIBUTE, "fulladle-module-metadata")
        }
      }

    // Add project dependencies for all subprojects
    root.subprojects.forEach { sub ->
      incoming.dependencies.add(root.dependencies.project(mapOf("path" to sub.path)))
    }

    // Wire metadata files into YamlConfigWriterTask
    root.tasks.withType(YamlConfigWriterTask::class.java).configureEach {
      moduleMetadataFiles.from(incoming)
      if (flankGradleExtension.abi.isPresent) {
        abiFilter.set(flankGradleExtension.abi)
      }
    }
  }

  private fun configureLegacy(
    root: Project,
    flankGradleExtension: FlankGradleExtension,
  ) {
    // Legacy mode: apply module extensions and variant callbacks directly
    root.subprojects {
      if (extensions.findByType(FulladleModuleExtension::class.java) == null) {
        extensions.create("fulladleModuleConfig", FulladleModuleExtension::class.java)
      }

      pluginManager.withPlugin("com.android.application") {
        val androidComponents =
          extensions.getByType(
            com.android.build.api.variant.ApplicationAndroidComponentsExtension::class.java,
          )
        val ext = extensions.findByType(FulladleModuleExtension::class.java) ?: return@withPlugin
        androidComponents.onVariants { variant ->
          val androidTest = (variant as? com.android.build.api.variant.HasAndroidTest)?.androidTest ?: return@onVariants
          val buildType = variant.buildType ?: return@onVariants
          val flavorName = variant.productFlavors.joinToString("") { it.second }
          val flavorPath = variant.productFlavors.joinToString("/") { it.second }
          val archivesName =
            extensions
              .getByType(org.gradle.api.plugins.BasePluginExtension::class.java)
              .archivesName
              .get()

          variant.outputs.forEach { output ->
            val abiFilter =
              output.filters.firstOrNull {
                it.filterType == com.android.build.api.variant.FilterConfiguration.FilterType.ABI
              }
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
              layout.buildDirectory
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
              layout.buildDirectory
                .file("outputs/apk/$testApkDirPath/$testApkFileName")
                .get()
                .asFile.absolutePath

            ext.variantApks.add(
              VariantApkInfo(
                variantName = variant.name,
                appApkPath = appApkPath,
                testApkPath = testApkPath,
                abiName = abiName,
              ),
            )
          }
        }
      }

      pluginManager.withPlugin("com.android.library") {
        val androidComponents =
          extensions.getByType(
            com.android.build.api.variant.LibraryAndroidComponentsExtension::class.java,
          )
        val ext = extensions.findByType(FulladleModuleExtension::class.java) ?: return@withPlugin
        androidComponents.onVariants { variant ->
          val androidTest = (variant as? com.android.build.api.variant.HasAndroidTest)?.androidTest ?: return@onVariants
          val buildType = variant.buildType ?: return@onVariants
          val flavorName = variant.productFlavors.joinToString("") { it.second }
          val flavorPath = variant.productFlavors.joinToString("/") { it.second }
          val archivesName =
            extensions
              .getByType(org.gradle.api.plugins.BasePluginExtension::class.java)
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
            layout.buildDirectory
              .file("outputs/apk/$testApkDirPath/$testApkFileName")
              .get()
              .asFile.absolutePath

          ext.variantApks.add(
            VariantApkInfo(
              variantName = variant.name,
              appApkPath = null,
              testApkPath = testApkPath,
              abiName = null,
            ),
          )
        }
      }
    }

    val fulladleConfigureTask =
      root.tasks.register("configureFulladle") {
        var modulesEnabled = false
        doLast {
          // first configure all app modules
          root.subprojects {
            if (!hasAndroidTest) {
              return@subprojects
            }
            modulesEnabled = true
            if (isAndroidAppModule) {
              configureModule(this, flankGradleExtension)
            }
          }
          // then configure all library modules
          root.subprojects {
            if (!hasAndroidTest) {
              return@subprojects
            }
            modulesEnabled = true
            if (isAndroidLibraryModule) {
              configureModule(this, flankGradleExtension)
            }
          }

          check(modulesEnabled) {
            "All modules were disabled for testing in fulladleModuleConfig or the enabled modules had no tests.\n" +
              "Either re-enable modules for testing or add modules with tests."
          }
        }
      }

    root.tasks.withType(YamlConfigWriterTask::class.java).configureEach {
      dependsOn(fulladleConfigureTask)
    }

    root.afterEvaluate {
      root.tasks.named("printYml").configure {
        dependsOn(fulladleConfigureTask)
      }
    }
  }
}

fun configureModule(
  project: Project,
  flankGradleExtension: FlankGradleExtension,
) = project.run {
  val fulladleModuleExtension = extensions.findByType(FulladleModuleExtension::class.java) ?: return
  if (!hasAndroidTest) {
    return
  }

  var addedTestsForModule = false

  for (variantInfo in fulladleModuleExtension.variantApks) {
    if (addedTestsForModule) break

    if (!variantInfo.isExpectedVariantInModule(fulladleModuleExtension)) continue

    if (flankGradleExtension.abi.isPresent && variantInfo.abiName != null && variantInfo.abiName != flankGradleExtension.abi.get()) continue
    if (flankGradleExtension.abi.isPresent && variantInfo.abiName == null) {
      // No ABI filter on this output - it's a match (universal)
    }

    val yml = StringBuilder()
    if (!flankGradleExtension.debugApk.isPresent) {
      if (project.isAndroidAppModule && variantInfo.appApkPath != null) {
        flankGradleExtension.debugApk.set(rootProject.provider { variantInfo.appApkPath })
      } else if (project.isAndroidLibraryModule) {
        check(fulladleModuleExtension.debugApk.isPresent && fulladleModuleExtension.debugApk.orNull != null) {
          "Library module ${project.path} did not specify a debug apk. Library modules do not " +
            "generate a debug apk and one needs to be specified in the fulladleModuleConfig block\n" +
            "This is a required parameter in FTL which remains unused for library modules under test, " +
            "and you can use a dummy apk here"
        }
        flankGradleExtension.debugApk.set(rootProject.provider { fulladleModuleExtension.debugApk.get() })
      }
    } else {
      if (project.isAndroidAppModule && variantInfo.appApkPath != null) {
        yml.appendLine("- app: ${variantInfo.appApkPath}")
      } else if (project.isAndroidLibraryModule) {
        if (fulladleModuleExtension.debugApk.orNull != null) {
          yml.appendLine("- app: ${fulladleModuleExtension.debugApk.get()}")
        }
      }
    }

    if (!flankGradleExtension.instrumentationApk.isPresent) {
      flankGradleExtension.instrumentationApk.set(rootProject.provider { variantInfo.testApkPath })
    } else {
      if (yml.isBlank()) {
        yml.appendLine("- test: ${variantInfo.testApkPath}")
      } else {
        yml.appendLine("      test: ${variantInfo.testApkPath}")
      }
    }

    if (yml.isEmpty()) {
      overrideRootLevelConfigs(flankGradleExtension, fulladleModuleExtension)
    } else {
      yml.appendProperty(fulladleModuleExtension.maxTestShards, "    max-test-shards")
      yml.appendMapProperty(
        fulladleModuleExtension.clientDetails,
        "    client-details",
      ) { appendLine("        ${it.key}: ${it.value}") }
      yml.appendMapProperty(
        fulladleModuleExtension.environmentVariables,
        "    environment-variables",
      ) { appendLine("        ${it.key}: ${it.value}") }
      flankGradleExtension.additionalTestApks.add(yml.toString())
    }
    addedTestsForModule = true
  }
}

val Project.isAndroidAppModule
  get() = plugins.hasPlugin("com.android.application")
val Project.isAndroidLibraryModule
  get() = plugins.hasPlugin("com.android.library")

val Project.hasAndroidTest: Boolean
  get() {
    if (!(isAndroidLibraryModule || isAndroidAppModule)) {
      return false
    }
    val fulladleModuleExtension = extensions.findByType(FulladleModuleExtension::class.java) ?: return false
    if (!fulladleModuleExtension.enabled.get()) {
      return false
    }
    if (!file("$projectDir/src/androidTest").exists()) {
      println("Ignoring test variants in $path: No tests in $projectDir/src/androidTest")
      return false
    }
    return true
  }

fun overrideRootLevelConfigs(
  flankGradleExtension: FlankGradleExtension,
  fulladleModuleExtension: FulladleModuleExtension,
) {
  val debugApk = fulladleModuleExtension.debugApk.orNull
  if (debugApk != null && debugApk.isNotEmpty()) {
    flankGradleExtension.debugApk.set(fulladleModuleExtension.debugApk.get())
  }
  val maxTestShards = fulladleModuleExtension.maxTestShards.orNull
  if (maxTestShards != null && maxTestShards > 0) {
    flankGradleExtension.maxTestShards.set(fulladleModuleExtension.maxTestShards.get())
  }
  val clientDetails = fulladleModuleExtension.clientDetails.orNull
  if (clientDetails != null && clientDetails.size != 0) {
    flankGradleExtension.clientDetails.set(fulladleModuleExtension.clientDetails.get())
  }
  val env = fulladleModuleExtension.environmentVariables.orNull
  if (env != null && env.size != 0) {
    flankGradleExtension.environmentVariables.set(fulladleModuleExtension.environmentVariables.get())
  }
}
