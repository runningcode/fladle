package com.osacky.flank.gradle

import com.android.build.gradle.AppExtension
import com.android.build.gradle.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.provider.MapProperty
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
    }

    val fulladleConfigureTask = root.tasks.register("configureFulladle") {
      doLast {
        root.subprojects {
          if (plugins.findPlugin("com.android.application") != null)
            configureApplicationModule(this, flankGradleExtension)
          else if (plugins.findPlugin("com.android.library") != null)
            configureLibraryModule(this, flankGradleExtension)
        }
      }
    }

    root.afterEvaluate {
      // TODO add other printYml tasks from other configs
      root.tasks.named("printYml").configure {
        dependsOn(fulladleConfigureTask)
      }

      root.tasks.withType(YamlConfigWriterTask::class.java).configureEach {
        dependsOn(fulladleConfigureTask)
      }
    }
  }
}

fun configureLibraryModule(project: Project, flankGradleExtension: FlankGradleExtension) = project.run {
  pluginManager.withPlugin("com.android.library") {
    val fulladleModuleExtension = extensions.getByType(FulladleModuleExtension::class.java)
    if (fulladleModuleExtension.enabled.get()) {
      val library = extensions.getByType<LibraryExtension>()
      library.testVariants.configureEach {
        if (file("$projectDir/src/androidTest").exists()) {

          val debugApk = fulladleModuleExtension.debugApk.let {
            buildString {
              if (it.isPresent) { append("    ") }
              appendProperty(it, name = "app")
            }
          }.trimEnd()

          val maxTestShards = fulladleModuleExtension.maxTestShards.let {
            buildString {
              if (it.isPresent) { append("    ") }
              appendProperty(it, name = "max-test-shards")
            }
          }.trimEnd()

          val clientDetails = mapPropertyToYaml(fulladleModuleExtension.clientDetails, "client-details")

          val environmentVariables = mapPropertyToYaml(fulladleModuleExtension.environmentVariables, "environment-variables")

          outputs.configureEach {
            flankGradleExtension.additionalTestApks.add(
              project.rootProject.provider {
                listOf("- test: $outputFile", debugApk, maxTestShards, clientDetails, environmentVariables)
                  .filter { it.isNotEmpty() }
                  .joinToString("\n")
                  .trimStart()
                  .trimEnd()
              }
            )
          }
        } else {
          println("Ignoring $name test variant in $path: No tests in $projectDir/src/androidTest")
        }
      }
    }
  }
}

fun configureApplicationModule(project: Project, flankGradleExtension: FlankGradleExtension) = project.run {
  pluginManager.withPlugin("com.android.application") {
    val fulladleModuleExtension = extensions.getByType(FulladleModuleExtension::class.java)
    val appExtension = extensions.getByType<AppExtension>()
    // Only configure the first test variant per module.
    // Does anyone test more than one variant per module?
    var addedTestsForModule = false

    // TODO deal with ignored/filtered variants
    appExtension.testVariants.configureEach testVariant@{
      if (addedTestsForModule) {
        return@testVariant
      }
      val appVariant = testedVariant
      appVariant.outputs.configureEach app@{

        this@testVariant.outputs.configureEach test@{
          val strs = mutableListOf<String>()
          // TODO is this racy?
          // If the debugApk isn't yet set, let's use this one.
          if (!flankGradleExtension.debugApk.isPresent) {
            flankGradleExtension.debugApk.set(rootProject.provider { this@app.outputFile.absolutePath })
          } else {
            // Otherwise, let's just add it to the list.
            strs.add("- app: ${this@app.outputFile}")
          }

          // If the instrumentation apk isn't yet set, let's use this one.
          if (!flankGradleExtension.instrumentationApk.isPresent) {
            flankGradleExtension.instrumentationApk.set(rootProject.provider { this@test.outputFile.absolutePath })
          } else {
            // Otherwise, let's just add it to the list.
            strs.add("      test: ${this@test.outputFile}")
          }
          val maxTestShards = fulladleModuleExtension.maxTestShards.let {
            buildString {
              if (it.isPresent) { append("    ") }
              appendProperty(it, name = "max-test-shards")
            }
          }.trimEnd()

          if (strs.isEmpty() || strs[0].isEmpty()) // root module, should not be added as additional test apk
            return@test

          val clientDetails = mapPropertyToYaml(fulladleModuleExtension.clientDetails, "client-details")

          val environmentVariables = mapPropertyToYaml(fulladleModuleExtension.environmentVariables, "environment-variables")

          strs.addAll(listOf(maxTestShards, clientDetails, environmentVariables))

          flankGradleExtension.additionalTestApks.add(
            rootProject.provider {
              strs
                .filter { it.trim().isNotEmpty() }
                .joinToString("\n")
                .trimEnd()
            }
          )

          addedTestsForModule = true
          return@test
        }
      }
    }
  }
}

fun mapPropertyToYaml(map: MapProperty<String, String>, propName: String) =
  map.let {
    buildString {
      if (it.isPresentAndNotEmpty) { append("    ") }
      appendMapProperty(it, name = propName) {
        appendLine("          \"${it.key}\": \"${it.value}\"")
      }
    }
  }.trimEnd()
