package com.osacky.flank.gradle.integration

import com.google.common.truth.Truth.assertThat
import org.gradle.testkit.runner.GradleRunner
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class FulladleSettingsPluginTest {
  @get:Rule
  var testProjectRoot = TemporaryFolder()

  val agpDependency: String = "com.android.tools.build:gradle:9.0.1"

  fun writeBuildGradle(build: String) {
    val file = testProjectRoot.newFile("build.gradle")
    file.writeText(build)
  }

  fun writeSettingsGradle(vararg includes: String) {
    testProjectRoot.newFile("settings.gradle").writeText(
      """
      plugins {
        id "com.osacky.fulladle.settings"
      }
      ${includes.joinToString("\n") { "include '$it'" }}

      dependencyResolutionManagement {
        repositories {
          mavenCentral()
          google()
        }
      }
      """.trimIndent(),
    )
  }

  @Test
  fun `settings plugin with submodules`() {
    val appFixture = "android-project"
    val libraryFixture = "android-library-project"
    writeSettingsGradle(appFixture, libraryFixture)
    testProjectRoot.setupFixture(appFixture)
    testProjectRoot.setupFixture(libraryFixture)

    writeBuildGradle(
      """
      buildscript {
          repositories {
              google()
          }
          dependencies {
              classpath '$agpDependency'
          }
      }
      plugins {
        id "com.osacky.fulladle"
      }
      fladle {
        serviceAccountCredentials = project.layout.projectDirectory.file("android-project/flank-gradle-5cf02dc90531.json")
      }
      """.trimIndent(),
    )

    val result =
      testProjectRoot
        .gradleRunner()
        .withArguments(":printYml")
        .build()

    assertThat(result.output).contains("SUCCESS")
    assertThat(result.output).containsMatch(
      """
      > Task :printYml
      gcloud:
        app: [0-9a-zA-Z\/_]*/android-project/build/outputs/apk/debug/android-project-debug.apk
        test: [0-9a-zA-Z\/_]*/android-project/build/outputs/apk/androidTest/debug/android-project-debug-androidTest.apk
      """.trimIndent(),
    )
    assertThat(result.output).contains("additional-app-test-apks:")
  }

  @Test
  fun `settings plugin with disabled module`() {
    val appFixture = "android-project"
    val libraryFixture = "android-library-project"
    val ignoredLibraryProject = "android-lib-ignored"
    writeSettingsGradle(appFixture, libraryFixture, ignoredLibraryProject)
    testProjectRoot.setupFixture(appFixture)
    testProjectRoot.setupFixture(libraryFixture)
    File(testProjectRoot.root, libraryFixture).copyRecursively(testProjectRoot.newFile(ignoredLibraryProject), overwrite = true)

    writeBuildGradle(
      """
      buildscript {
          repositories {
              google()
          }
          dependencies {
              classpath '$agpDependency'
          }
      }
      plugins {
        id "com.osacky.fulladle"
      }
      fladle {
        serviceAccountCredentials = project.layout.projectDirectory.file("android-project/flank-gradle-5cf02dc90531.json")
      }
      """.trimIndent(),
    )

    // Disable the ignored library project
    File(testProjectRoot.root, "$ignoredLibraryProject/build.gradle").appendText(
      """
      fulladleModuleConfig {
        enabled = false
      }
      """.trimIndent(),
    )

    val result =
      testProjectRoot
        .gradleRunner()
        .withArguments(":printYml")
        .build()

    assertThat(result.output).contains("SUCCESS")
  }

  @Test
  fun `settings plugin with submodule overrides`() {
    val appFixture = "android-project"
    val appFixture2 = "android-project2"
    val libraryFixture = "android-library-project"
    val libraryFixture2 = "android-lib2"
    writeSettingsGradle(appFixture, appFixture2, libraryFixture, libraryFixture2)
    testProjectRoot.setupFixture(appFixture)
    testProjectRoot.setupFixture(appFixture2)
    testProjectRoot.setupFixture(libraryFixture)
    File(testProjectRoot.root, libraryFixture).copyRecursively(testProjectRoot.newFile(libraryFixture2), overwrite = true)

    writeBuildGradle(
      """
      buildscript {
          repositories {
              google()
          }
          dependencies {
              classpath '$agpDependency'
          }
      }
      plugins {
        id "com.osacky.fulladle"
      }
      fladle {
        serviceAccountCredentials = project.layout.projectDirectory.file("android-project/flank-gradle-5cf02dc90531.json")
        environmentVariables = [
          "clearPackageData": "true",
          "listener": "com.osacky.flank.sample.Listener"
        ]
      }
      """.trimIndent(),
    )

    File(testProjectRoot.root, "$libraryFixture2/build.gradle").appendText(
      """
      fulladleModuleConfig {
        maxTestShards = 4
        clientDetails = ["test-type": "PR","build-number": "132"]
      }
      """.trimIndent(),
    )

    File(testProjectRoot.root, "$libraryFixture/build.gradle").appendText(
      """
      fulladleModuleConfig {
        maxTestShards = 7
        environmentVariables = [
            "clearPackageData": "false",
            "listener": "com.osacky.flank.sample.Listener.Different"
        ]
        debugApk = "dummy_app.apk"
      }
      """.trimIndent(),
    )

    val result =
      testProjectRoot
        .gradleRunner()
        .withArguments(":printYml")
        .build()

    assertThat(result.output).contains("SUCCESS")
    assertThat(result.output).contains("additional-app-test-apks:")
  }

  @Test
  fun `settings plugin with flavors`() {
    val appFixture = "android-project"
    val libraryFixture = "android-library-project"
    val flavourProject = "android-project-flavors"
    val flavourLibrary = "android-library-project-flavors"
    writeSettingsGradle(appFixture, libraryFixture, flavourProject, flavourLibrary)
    testProjectRoot.setupFixture(appFixture)
    testProjectRoot.setupFixture(libraryFixture)
    testProjectRoot.setupFixture(flavourProject)
    testProjectRoot.setupFixture(flavourLibrary)

    writeBuildGradle(
      """
      buildscript {
          repositories {
              google()
          }
          dependencies {
              classpath '$agpDependency'
          }
      }
      plugins {
        id "com.osacky.fulladle"
      }
      fladle {
        serviceAccountCredentials = project.layout.projectDirectory.file("android-project/flank-gradle-5cf02dc90531.json")
      }
      """.trimIndent(),
    )

    File(testProjectRoot.root, "$flavourProject/build.gradle").appendText(
      """
      fulladleModuleConfig {
        variant = "vanillaDebug"
      }
      """.trimIndent(),
    )

    File(testProjectRoot.root, "$flavourLibrary/build.gradle").appendText(
      """
      fulladleModuleConfig {
        variant = "strawberryDebug"
      }
      """.trimIndent(),
    )

    val result =
      testProjectRoot
        .gradleRunner()
        .withArguments(":printYml")
        .build()

    assertThat(result.output).contains("SUCCESS")
    assertThat(result.output).contains("additional-app-test-apks:")
  }

  @Test
  fun `settings plugin smoke test`() {
    testProjectRoot.newFile("settings.gradle").writeText(
      """
      plugins {
        id "com.osacky.fulladle.settings"
      }
      """.trimIndent(),
    )
    writeBuildGradle(
      """
      plugins {
        id "com.osacky.fulladle"
      }
      """.trimIndent(),
    )
    val result =
      testProjectRoot
        .gradleRunner()
        .withArguments("help")
        .build()
    assertThat(result.output).contains("SUCCESS")
  }

  @Test
  fun `settings plugin with non-Android module`() {
    val appFixture = "android-project"
    val libraryFixture = "android-library-project"
    val nonAndroidFixture = "lib1"
    writeSettingsGradle(appFixture, libraryFixture, nonAndroidFixture)
    testProjectRoot.setupFixture(appFixture)
    testProjectRoot.setupFixture(libraryFixture)
    File(testProjectRoot.root, libraryFixture).copyRecursively(testProjectRoot.newFile(nonAndroidFixture), overwrite = true)

    writeBuildGradle(
      """
      buildscript {
          repositories {
              google()
          }
          dependencies {
              classpath '$agpDependency'
          }
      }
      plugins {
        id "com.osacky.fulladle"
      }
      fladle {
        serviceAccountCredentials = project.layout.projectDirectory.file("android-project/flank-gradle-5cf02dc90531.json")
      }
      """.trimIndent(),
    )

    // Replace the non-Android module's build.gradle to be a pure Java library
    File(testProjectRoot.root, "$nonAndroidFixture/build.gradle").writeText(
      """
      apply plugin: 'java-library'
      """.trimIndent(),
    )

    val result =
      testProjectRoot
        .gradleRunner()
        .withArguments(":printYml")
        .build()

    assertThat(result.output).contains("SUCCESS")
  }
}
