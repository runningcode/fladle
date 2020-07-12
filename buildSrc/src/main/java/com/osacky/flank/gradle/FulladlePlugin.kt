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

    val fulladleConfigureTask = root.tasks.register("configureFulladle") {
      doLast {
        logger.info(
          """
              Warning: Fulladle is still in development. It is very likely not to work.
               * Report bugs to the Firebase Community slack in #flank or as an issue in the Fladle project.
               * Include the output from the printYml task.
          """.trimIndent()
        )
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
            val library = extensions.getByType<LibraryExtension>()
            library.testVariants.configureEach {
              if (file("$projectDir/src/androidTest").exists()) {
                outputs.configureEach {
                  flankGradleExtension.additionalTestApks.add(
                    root.provider {
                      "- test: $outputFile"
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
