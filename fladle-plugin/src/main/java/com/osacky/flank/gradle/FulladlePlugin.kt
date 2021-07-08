package com.osacky.flank.gradle

import com.android.build.gradle.AppExtension
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.TestedExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
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
          configureModule(this,flankGradleExtension)
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

fun configureModule(project: Project, flankGradleExtension: FlankGradleExtension) = project.run {
  val fulladleModuleExtension = extensions.findByType(FulladleModuleExtension::class.java) ?: return
  if (!fulladleModuleExtension.enabled.get())
    return

  if (project.isAndroidLibraryModule) {
    // we need library modules to specify the app apk to test against, even if it's a dummy one
    check(fulladleModuleExtension.debugApk.isPresent && fulladleModuleExtension.debugApk.orNull != null) {
      "Library module ${project.path} did not specify a debug apk. Library modules do not generate a debug apk and one needs to be specified in the fulladleModuleConfig block"
    }
  }
  val testedExtension = extensions.getByType<TestedExtension>()
  // Only configure the first test variant per module.
  // Does anyone test more than one variant per module?
  var addedTestsForModule = false

  // TODO deal with ignored/filtered variants
  testedExtension.testVariants.configureEach testVariant@{
    if (!file("$projectDir/src/androidTest").exists()) {
      println("Ignoring $name test variant in $path: No tests in $projectDir/src/androidTest")
      return@testVariant
    }
    if (addedTestsForModule) {
      return@testVariant
    }
    testedVariant.outputs.configureEach app@{
      this@testVariant.outputs.configureEach test@{
        val strs = mutableListOf<String>()

        if (!flankGradleExtension.debugApk.isPresent) {
          if (project.isAndroidAppModule) {
            // app modules produce app apks that we can consume
            flankGradleExtension.debugApk.set(rootProject.provider { this@app.outputFile.absolutePath })
          } else if (project.isAndroidLibraryModule) {
            // library modules do not produce an app apk and we'll use the one specified in fulladleModuleConfig block
            flankGradleExtension.debugApk.set(rootProject.provider { fulladleModuleExtension.debugApk.get() })
          }
        } else {
          // Otherwise, let's just add it to the list.
          if (project.isAndroidAppModule){
              strs.add("- app: ${this@app.outputFile}")
          } else if (project.isAndroidLibraryModule) {
            strs.add("- app: ${fulladleModuleExtension.debugApk.get()}")
          }
        }

        // If the instrumentation apk isn't yet set, let's use this one.
        if (!flankGradleExtension.instrumentationApk.isPresent) {
          flankGradleExtension.instrumentationApk.set(rootProject.provider { this@test.outputFile.absolutePath })
        } else {
          // Otherwise, let's just add it to the list.
          strs.add("      test: ${this@test.outputFile}")
        }

        if (strs.isEmpty()) {
          // this is the root module
          // should not be added as additional test apk
          return@test
        }

        val maxTestShards = propertyToYaml(fulladleModuleExtension.maxTestShards, "max-test-shards")
        val clientDetails = mapPropertyToYaml(fulladleModuleExtension.clientDetails, "client-details")
        val environmentVariables = mapPropertyToYaml(fulladleModuleExtension.environmentVariables, "environment-variables")

        strs.addAll(listOf(maxTestShards, clientDetails, environmentVariables))

        writeAdditionalTestApps(strs, flankGradleExtension, rootProject)

        addedTestsForModule = true
        return@test
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

fun propertyToYaml(prop: Property<*>, propName: String) =
  prop.let {
    buildString {
      if (it.isPresent) { append("    ") }
      appendProperty(it, name = propName)
    }
  }.trimEnd()

fun writeAdditionalTestApps(strs: List<String>, flankGradleExtension: FlankGradleExtension, rootProject: Project) {
  flankGradleExtension.additionalTestApks.add(
    rootProject.provider {
      strs
        .filter { it.trim().isNotEmpty() }
        .joinToString("\n")
        .trimEnd()
    }
  )
}

val Project.isAndroidAppModule
  get() = plugins.hasPlugin("com.android.application")
val Project.isAndroidLibraryModule
  get() = plugins.hasPlugin("com.android.library")
