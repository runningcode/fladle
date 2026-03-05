package com.osacky.flank.gradle

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import com.android.build.api.variant.FilterConfiguration
import com.android.build.api.variant.HasAndroidTest
import com.osacky.flank.gradle.validation.checkForExclusionUsage
import com.osacky.flank.gradle.validation.validateOptionsUsed
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.plugins.BasePluginExtension
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
        target.layout.fladleDir
          .get()
          .asFile
          .mkdirs()
      }
      classpath = project.fladleConfig
      args = listOf("auth", "login")
    }

    configureTasks(target, extension)
  }

  private fun checkMinimumGradleVersion() {
    if (GRADLE_MIN_VERSION > GradleVersion.current()) {
      throw GradleException("Fladle requires at minimum version $GRADLE_MIN_VERSION. Detected version ${GradleVersion.current()}.")
    }
  }

  private fun configureTasks(
    project: Project,
    base: FlankGradleExtension,
  ) {
    base.flankVersion.finalizeValueOnRead()
    base.flankCoordinates.finalizeValueOnRead()
    base.serviceAccountCredentials.finalizeValueOnRead()

    // Register onVariants callbacks before afterEvaluate for APK path detection
    project.pluginManager.withPlugin("com.android.application") {
      if (!base.debugApk.isPresent || !base.instrumentationApk.isPresent) {
        findDebugAndInstrumentationApk(project, base)
      }
    }

    project.afterEvaluate {
      // Add Flank dependency to Fladle Configuration
      // Must be done afterEvaluate otherwise extension values will not be set.
      project.dependencies.add(FLADLE_CONFIG, "${base.flankCoordinates.get()}:${base.flankVersion.get()}")

      tasks.apply {
        createTasksForConfig(base, base, project, "")

        base.configs.forEach { config ->
          createTasksForConfig(base, config, project, config.name.capitalize())
        }
      }
    }
  }

  private fun TaskContainer.createTasksForConfig(
    base: FlankGradleExtension,
    config: FladleConfig,
    project: Project,
    name: String,
  ) {
    val configName = name.toLowerCase()
    // we want to use default dir only if user did not set own `localResultsDir`
    val useDefaultDir = config.localResultsDir.isPresent.not()

    val validateFladle =
      register("validateFladleConfig$name") {
        description = "Perform validation actions"
        group = TASK_GROUP
        doLast {
          checkIfSanityAndValidateConfigs(config)
          validateOptionsUsed(config = config, flank = base.flankVersion.get())
          checkForExclusionUsage(config)
        }
      }

    val writeConfigProps = register("writeConfigProps$name", YamlConfigWriterTask::class.java, base, config, name)

    writeConfigProps.configure { dependsOn(validateFladle) }

    register("printYml$name") {
      description = "Print the flank.yml file to the console."
      group = TASK_GROUP
      dependsOn(writeConfigProps)
      doLast {
        println(
          writeConfigProps
            .get()
            .fladleConfigFile
            .get()
            .asFile
            .readText(),
        )
      }
    }

    register("flankDoctor$name", FlankJavaExec::class.java) {
      if (useDefaultDir) setUpWorkingDir(configName)
      description = "Finds problems with the current configuration."
      classpath = project.fladleConfig
      args =
        listOf(
          "firebase",
          "test",
          "android",
          "doctor",
          "-c",
          writeConfigProps
            .get()
            .fladleConfigFile
            .get()
            .asFile.absolutePath,
        )
      dependsOn(writeConfigProps)
    }

    val execFlank = register("execFlank$name", FlankExecutionTask::class.java, config)
    execFlank.configure {
      if (useDefaultDir) setUpWorkingDir(configName)
      description = "Runs instrumentation tests using flank on firebase test lab."
      classpath = project.fladleConfig
      args =
        if (project.hasProperty("dumpShards")) {
          listOf(
            "firebase",
            "test",
            "android",
            "run",
            "-c",
            writeConfigProps
              .get()
              .fladleConfigFile
              .get()
              .asFile.absolutePath,
            "--dump-shards",
          )
        } else {
          listOf(
            "firebase",
            "test",
            "android",
            "run",
            "-c",
            writeConfigProps
              .get()
              .fladleConfigFile
              .get()
              .asFile.absolutePath,
          )
        }
      if (config.serviceAccountCredentials.isPresent) {
        environment(mapOf("GOOGLE_APPLICATION_CREDENTIALS" to config.serviceAccountCredentials.get()))
      }
      dependsOn(writeConfigProps)
      if (config.dependOnAssemble.isPresent && config.dependOnAssemble.get()) {
        // Find assemble tasks by convention name pattern
        val variantName = config.variant.orNull
        if (variantName != null) {
          val capitalizedVariant = variantName.capitalize()
          dependsOn("assemble$capitalizedVariant")
          dependsOn("assemble${capitalizedVariant}AndroidTest")
        } else {
          dependsOn("assembleDebug")
          dependsOn("assembleDebugAndroidTest")
        }
      }
      if (config.localResultsDir.hasValue) {
        this.outputs.dir("${workingDir.path}/${config.localResultsDir.get()}")
        // This task is never upToDate since it relies on network connections and firebase test lab.
        this.outputs.upToDateWhen { false }
      }
    }
    if (config.localResultsDir.hasValue && canImportReport()) {
      try {
        importReport(project, execFlank)
      } catch (e: Exception) {
        project.logger.warn(e.message)
        e.printStackTrace()
      }
    }

    register("runFlank$name", RunFlankTask::class.java).configure {
      dependsOn(execFlank)
    }
  }

  private fun automaticallyConfigureTestOrchestrator(
    project: Project,
    config: FladleConfig,
    androidExtension: ApplicationExtension,
  ) {
    project.afterEvaluate {
      val execution = androidExtension.testOptions.execution.uppercase()
      val useOrchestrator =
        execution == "ANDROIDX_TEST_ORCHESTRATOR" ||
          execution == "ANDROID_TEST_ORCHESTRATOR"
      if (useOrchestrator) {
        log("Automatically detected the use of Android Test Orchestrator")
        config.useOrchestrator.set(true)
      }
    }
  }

  private fun findDebugAndInstrumentationApk(
    project: Project,
    config: FladleConfig,
  ) {
    val androidExtension =
      requireNotNull(project.extensions.findByType(ApplicationExtension::class.java)) { "Could not find ApplicationExtension in ${project.name}" }
    automaticallyConfigureTestOrchestrator(project, config, androidExtension)

    val androidComponents =
      requireNotNull(project.extensions.findByType(ApplicationAndroidComponentsExtension::class.java)) {
        "Could not find ApplicationAndroidComponentsExtension in ${project.name}"
      }

    androidComponents.onVariants { variant ->
      if (!variant.isExpectedVariant(config)) return@onVariants
      val androidTest = (variant as? HasAndroidTest)?.androidTest ?: return@onVariants

      val buildType = variant.buildType ?: return@onVariants
      val flavorName = variant.productFlavors.joinToString("") { it.second }
      val flavorPath = variant.productFlavors.joinToString("/") { it.second }
      val archivesName = project.extensions.getByType(BasePluginExtension::class.java).archivesName.get()
      val buildDir = project.layout.buildDirectory

      // Test APK path
      val testApkDirPath = if (flavorPath.isNotEmpty()) "androidTest/$flavorPath/$buildType" else "androidTest/$buildType"
      val testApkFileName =
        if (flavorName.isNotEmpty()) {
          "$archivesName-$flavorName-$buildType-androidTest.apk"
        } else {
          "$archivesName-$buildType-androidTest.apk"
        }
      val testApkPath =
        buildDir
          .file("outputs/apk/$testApkDirPath/$testApkFileName")
          .get()
          .asFile
          .absolutePath

      variant.outputs.forEach { output ->
        if (!output.isExpectedAbiOutput(config)) return@forEach

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
          buildDir
            .file("outputs/apk/$appApkDirPath/$appApkFileName")
            .get()
            .asFile
            .absolutePath

        if (!config.debugApk.isPresent) {
          // Don't set debug apk if not already set. #172
          project.log("Configuring fladle.debugApk from variant ${variant.name}")
          config.debugApk.set(appApkPath)
        }
        if (!config.roboScript.isPresent && !config.instrumentationApk.isPresent && !config.sanityRobo.get()) {
          // Don't set instrumentation apk if not already set. #172
          project.log("Configuring fladle.instrumentationApk from variant ${variant.name}")
          config.instrumentationApk.set(testApkPath)
        }
      }
    }
  }

  private val Project.fladleConfig: Configuration
    get() = configurations.getByName(FLADLE_CONFIG)

  companion object {
    val GRADLE_MIN_VERSION: GradleVersion = GradleVersion.version("9.1")
    const val TASK_GROUP = "fladle"
    const val FLADLE_CONFIG = "fladle"

    fun Project.log(message: String) {
      logger.info("Fladle: $message")
    }
  }
}
