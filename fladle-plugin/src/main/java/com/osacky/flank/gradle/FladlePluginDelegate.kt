package com.osacky.flank.gradle

import com.android.build.gradle.AppExtension
import com.android.build.gradle.TestedExtension
import com.android.build.gradle.internal.TaskManager.ASSEMBLE_ANDROID_TEST
import com.android.builder.model.TestOptions
import com.osacky.flank.gradle.validation.checkForExclusionUsage
import com.osacky.flank.gradle.validation.validateOptionsUsed
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.tasks.TaskContainer
import org.gradle.kotlin.dsl.create
import org.gradle.util.GradleVersion

class FladlePluginDelegate {

  fun apply(target: Project) {
    checkMinimumGradleVersion()

    // Create Configuration to store flank dependency
    target.configurations.create(FLADLE_CONFIG)

    val extension = target.extensions.create<FlankGradleExtension>("fladle", target.objects)

    target.tasks.register("flankAuth", FlankJavaExec::class.java) {
      doFirst {
        target.layout.fladleDir.get().asFile.mkdirs()
      }
      classpath = project.fladleConfig
      args = listOf("auth", "login")
    }

    configureTasks(target, extension)
  }

  private fun checkMinimumGradleVersion() {
    // Gradle 4.9 is required because we use the lazy task configuration API.
    if (GRADLE_MIN_VERSION > GradleVersion.current()) {
      throw GradleException("Fladle requires at minimum version $GRADLE_MIN_VERSION. Detected version ${GradleVersion.current()}.")
    }
  }

  private fun configureTasks(project: Project, base: FlankGradleExtension) {
    if (GradleVersion.current() > GradleVersion.version("6.1")) {
      base.flankVersion.finalizeValueOnRead()
      base.flankCoordinates.finalizeValueOnRead()
      base.serviceAccountCredentials.finalizeValueOnRead()
    }
    project.afterEvaluate {
      // Add Flank dependency to Fladle Configuration
      // Must be done afterEvaluate otherwise extension values will not be set.
      project.dependencies.add(FLADLE_CONFIG, "${base.flankCoordinates.get()}:${base.flankVersion.get()}")

      // Only use automatic apk path detection for 'com.android.application' projects.
      project.pluginManager.withPlugin("com.android.application") {
        if (!base.debugApk.isPresent || !base.instrumentationApk.isPresent) {
          findDebugAndInstrumentationApk(project, base)
        }
      }
      tasks.apply {
        createTasksForConfig(base, base, project, "")

        base.configs.forEach { config ->
          createTasksForConfig(base, config, project, config.name.capitalize())
        }
      }
    }
  }

  private fun TaskContainer.createTasksForConfig(base: FlankGradleExtension, config: FladleConfig, project: Project, name: String) {
    checkIfSanityAndValidateConfigs(config)
    validateOptionsUsed(config = config, flank = base.flankVersion.get())
    checkForExclusionUsage(config)
    val configName = name.toLowerCase()
    // we want to use default dir only if user did not set own `localResultsDir`
    val useDefaultDir = config.localResultsDir.isPresent.not()
    val writeConfigProps = register("writeConfigProps$name", YamlConfigWriterTask::class.java, base, config, name)

    register("printYml$name") {
      description = "Print the flank.yml file to the console."
      group = TASK_GROUP
      dependsOn(writeConfigProps)
      doLast {
        println(writeConfigProps.get().fladleConfigFile.get().asFile.readText())
      }
    }

    register("flankDoctor$name", FlankJavaExec::class.java) {
      if (useDefaultDir) setUpWorkingDir(configName)
      description = "Finds problems with the current configuration."
      classpath = project.fladleConfig
      args = listOf("firebase", "test", "android", "doctor", "-c", writeConfigProps.get().fladleConfigFile.get().asFile.absolutePath)
      dependsOn(writeConfigProps)
    }

    val execFlank = register("execFlank$name", FlankExecutionTask::class.java, config)
    execFlank.configure {
      if (useDefaultDir) setUpWorkingDir(configName)
      description = "Runs instrumentation tests using flank on firebase test lab."
      classpath = project.fladleConfig
      args = if (project.hasProperty("dumpShards")) {
        listOf("firebase", "test", "android", "run", "-c", writeConfigProps.get().fladleConfigFile.get().asFile.absolutePath, "--dump-shards")
      } else {
        listOf("firebase", "test", "android", "run", "-c", writeConfigProps.get().fladleConfigFile.get().asFile.absolutePath)
      }
      if (config.serviceAccountCredentials.isPresent) {
        environment(mapOf("GOOGLE_APPLICATION_CREDENTIALS" to config.serviceAccountCredentials.get()))
      }
      dependsOn(writeConfigProps)
      if (config.dependOnAssemble.isPresent && config.dependOnAssemble.get()) {
        project.extensions.findByType(TestedExtension::class.java)?.let { testedExtension ->
          testedExtension.testVariants.configureEach {
            if (testedVariant.assembleProvider.isPresent) {
              dependsOn(testedVariant.assembleProvider.get())
            }
            if (assembleProvider.isPresent) {
              dependsOn(assembleProvider.get())
            }
          }
        }
      }
    }

    register("runFlank$name", RunFlankTask::class.java).configure {
      dependsOn(execFlank)
    }
  }

  private fun automaticallyConfigureTestOrchestrator(project: Project, extension: FlankGradleExtension, androidExtension: AppExtension) {
    project.afterEvaluate {
      val useOrchestrator = androidExtension.testOptions.executionEnum == TestOptions.Execution.ANDROIDX_TEST_ORCHESTRATOR || androidExtension.testOptions.executionEnum == TestOptions.Execution.ANDROID_TEST_ORCHESTRATOR
      if (useOrchestrator) {
        log("Automatically detected the use of Android Test Orchestrator")
      }
      extension.useOrchestrator.set(useOrchestrator)
    }
  }

  private fun findDebugAndInstrumentationApk(project: Project, extension: FlankGradleExtension) {
    val baseExtension = requireNotNull(project.extensions.findByType(AppExtension::class.java)) { "Could not find AppExtension in ${project.name}" }
    automaticallyConfigureTestOrchestrator(project, extension, baseExtension)
    baseExtension.testVariants.configureEach {
      val appVariant = testedVariant
      outputs.configureEach test@{
        appVariant.outputs.configureEach app@{
          if (!extension.variant.isPresent || (extension.variant.isPresent && extension.variant.get() == appVariant.name)) {

            if (!extension.debugApk.isPresent) {
              // Don't set debug apk if not already set. #172
              project.log("Configuring fladle.debugApk from variant ${this@app.name}")
              extension.debugApk.set(this@app.outputFile.absolutePath)
            }
            if (!extension.roboScript.isPresent && !extension.instrumentationApk.isPresent && !extension.sanityRobo.get()) {
              // Don't set instrumentation apk if not already set. #172
              project.log("Configuring fladle.instrumentationApk from variant ${this@test.name}")
              extension.instrumentationApk.set(this@test.outputFile.absolutePath)
            }
          }
        }
      }
    }
  }

  private val Project.fladleConfig: Configuration
    get() = configurations.getByName(FLADLE_CONFIG)

  companion object {
    val GRADLE_MIN_VERSION: GradleVersion = GradleVersion.version("5.5")
    const val TASK_GROUP = "fladle"
    const val FLADLE_CONFIG = "fladle"
    fun Project.log(message: String) {
      logger.info("Fladle: $message")
    }
  }
}
