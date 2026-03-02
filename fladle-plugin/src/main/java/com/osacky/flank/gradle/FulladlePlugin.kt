package com.osacky.flank.gradle

import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import com.android.build.api.variant.FilterConfiguration
import com.android.build.api.variant.HasAndroidTest
import com.android.build.api.variant.LibraryAndroidComponentsExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.BasePluginExtension
import org.gradle.kotlin.dsl.getByType

/**
 * Like the Fladle plugin, but it configures additionalTestApks for the _full_ project. Hence fulladle.
 */
class FulladlePlugin : Plugin<Project> {
  override fun apply(root: Project) {
    check(root.parent == null) { "Fulladle must be applied in the root project in order to configure subprojects." }
    FladlePluginDelegate().apply(root)

    val flankGradleExtension = root.extensions.getByType(FlankGradleExtension::class)

    root.subprojects {
      // Yuck, cross project configuration
      extensions.create("fulladleModuleConfig", FulladleModuleExtension::class.java)

      // Register onVariants callbacks to capture APK info during configuration
      pluginManager.withPlugin("com.android.application") {
        val androidComponents = extensions.getByType(ApplicationAndroidComponentsExtension::class.java)
        val ext = extensions.findByType(FulladleModuleExtension::class.java) ?: return@withPlugin
        androidComponents.onVariants { variant ->
          val androidTest = (variant as? HasAndroidTest)?.androidTest ?: return@onVariants
          val buildType = variant.buildType ?: return@onVariants
          val flavorName = variant.productFlavors.joinToString("") { it.second }
          val flavorPath = variant.productFlavors.joinToString("/") { it.second }
          val archivesName = extensions.getByType(BasePluginExtension::class.java).archivesName.get()

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
        val androidComponents = extensions.getByType(LibraryAndroidComponentsExtension::class.java)
        val ext = extensions.findByType(FulladleModuleExtension::class.java) ?: return@withPlugin
        androidComponents.onVariants { variant ->
          val androidTest = (variant as? HasAndroidTest)?.androidTest ?: return@onVariants
          val buildType = variant.buildType ?: return@onVariants
          val flavorName = variant.productFlavors.joinToString("") { it.second }
          val flavorPath = variant.productFlavors.joinToString("/") { it.second }
          val archivesName = extensions.getByType(BasePluginExtension::class.java).archivesName.get()

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
        // We first configure all app modules, then configure all library modules.
        // We force this order because app modules are better candidates to become
        // root level test/app APKs, since they produce app APKs.
        // If no app module had tests or was enabled, we will choose a library module
        // to become a root level module, in which case we will have to check if it
        // has its debugApk set.
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
      // TODO add other printYml tasks from other configs
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

  // Only configure the first test variant per module.
  // Does anyone test more than one variant per module?
  var addedTestsForModule = false

  for (variantInfo in fulladleModuleExtension.variantApks) {
    if (addedTestsForModule) break

    if (!variantInfo.isExpectedVariantInModule(fulladleModuleExtension)) continue

    // Check ABI filter against the extension
    if (flankGradleExtension.abi.isPresent && variantInfo.abiName != null && variantInfo.abiName != flankGradleExtension.abi.get()) continue
    if (flankGradleExtension.abi.isPresent && variantInfo.abiName == null) {
      // No ABI filter on this output - it's a match (universal)
    }

    val yml = StringBuilder()
    // If the debugApk isn't yet set, let's use this one.
    if (!flankGradleExtension.debugApk.isPresent) {
      if (project.isAndroidAppModule && variantInfo.appApkPath != null) {
        // app modules produce app apks that we can consume
        flankGradleExtension.debugApk.set(rootProject.provider { variantInfo.appApkPath })
      } else if (project.isAndroidLibraryModule) {
        // library modules do not produce an app apk and we'll use the one specified in fulladleModuleConfig block
        // we need library modules to specify the app apk to test against, even if it's a dummy one
        check(fulladleModuleExtension.debugApk.isPresent && fulladleModuleExtension.debugApk.orNull != null) {
          "Library module ${project.path} did not specify a debug apk. Library modules do not " +
            "generate a debug apk and one needs to be specified in the fulladleModuleConfig block\n" +
            "This is a required parameter in FTL which remains unused for library modules under test, " +
            "and you can use a dummy apk here"
        }
        flankGradleExtension.debugApk.set(rootProject.provider { fulladleModuleExtension.debugApk.get() })
      }
    } else {
      // Otherwise, let's just add it to the list.
      if (project.isAndroidAppModule && variantInfo.appApkPath != null) {
        yml.appendLine("- app: ${variantInfo.appApkPath}")
      } else if (project.isAndroidLibraryModule) {
        // app apk is not required for library modules so only use if it's explicitly specified
        if (fulladleModuleExtension.debugApk.orNull != null) {
          yml.appendLine("- app: ${fulladleModuleExtension.debugApk.get()}")
        }
      }
    }

    // If the instrumentation apk isn't yet set, let's use this one.
    if (!flankGradleExtension.instrumentationApk.isPresent) {
      flankGradleExtension.instrumentationApk.set(rootProject.provider { variantInfo.testApkPath })
    } else {
      // Otherwise, let's just add it to the list.
      if (yml.isBlank()) {
        // The first item in the list needs to start with a ` - `.
        yml.appendLine("- test: ${variantInfo.testApkPath}")
      } else {
        yml.appendLine("      test: ${variantInfo.testApkPath}")
      }
    }

    if (yml.isEmpty()) {
      // this is the root module
      // should not be added as additional test apk
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

// returns false if the module explicitly disabled testing or if it simply had no tests
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
  // if the root module overrode any value in its fulladleModuleConfig block
  // then use those values instead
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
