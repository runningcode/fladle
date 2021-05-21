package com.osacky.flank.gradle

import com.android.build.gradle.AppExtension
import com.android.build.gradle.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
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
          pluginManager.withPlugin("com.android.application") {
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
                  // TODO is this racy?
                  // If the debugApk isn't yet set, let's use this one.
                  if (!flankGradleExtension.debugApk.isPresent) {
                    flankGradleExtension.debugApk.set(root.provider { this@app.outputFile.absolutePath })
                  } else {
                    // Otherwise, let's just add it to the list.
                    flankGradleExtension.additionalTestApks.add(
                      root.provider {
                        "- app: ${this@app.outputFile}"
                      }
                    )
                  }
                  // If the instrumentation apk isn't yet set, let's use this one.
                  if (!flankGradleExtension.instrumentationApk.isPresent) {
                    flankGradleExtension.instrumentationApk.set(root.provider { this@test.outputFile.absolutePath })
                  } else {
                    // Otherwise, let's just add it to the list.
                    flankGradleExtension.additionalTestApks.add(
                      root.provider {
                        "  test: ${this@test.outputFile}"
                      }
                    )
                  }
                  addedTestsForModule = true
                  return@test
                }
              }
            }
          }
          pluginManager.withPlugin("com.android.library") {
            val fulladleModuleExtension = extensions.getByType(FulladleModuleExtension::class.java)
            if (fulladleModuleExtension.enabled.get()) {
              val library = extensions.getByType<LibraryExtension>()
              library.testVariants.configureEach {
                if (file("$projectDir/src/androidTest").exists()) {
                  val debugApk = fulladleModuleExtension.debugApk.orNull.let {
                    if (it == null) return@let ""
                    "      app: $it"
                  }
                  val maxTestShards = fulladleModuleExtension.maxTestShard.orNull.let {
                    if (it == null) return@let ""
                    "      max-test-shards: $it"
                  }
                  val clientDetails = fulladleModuleExtension.clientDetails.get().let {
                    if (it.isEmpty()) return@let ""
                    "      client-details:\n${it.toYaml(5)}"
                  }
                  val environmentVariables = fulladleModuleExtension.environmentVariables.get().let {
                    if (it.isEmpty()) return@let ""
                    "      environment-variables:\n${it.toYaml(5)}"
                  }

                  outputs.configureEach {
                    flankGradleExtension.additionalTestApks.add(
                      root.provider {
                        listOf("- test: $outputFile", debugApk, maxTestShards, clientDetails, environmentVariables)
                          .filter { it.isNotEmpty() }
                          .joinToString("\n")
                      }
                    )
                  }
                } else {
                  println("ignoring variant for $this in $projectDir")
                }
              }
            }
          }
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

fun Map<String, String>.toYaml(indent: Int = 0) = this
  .mapKeys { "\"${it.key}\"" }
  .mapValues { "\"${it.value}\"" }
  .toList()
  .joinToString("\n") { "${"  ".repeat(indent)}${it.first}: ${it.second}" }