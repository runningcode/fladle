package com.osacky.flank.gradle.integration

import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class FulladlePluginIntegrationTest {
  @get:Rule
  var testProjectRoot = TemporaryFolder()

  fun writeBuildGradle(build: String) {
    val file = testProjectRoot.newFile("build.gradle")
    file.writeText(build)
  }

  @Test
  fun fulladleSmokeTest() {
    writeBuildGradle(
      """plugins {
             |  id "com.osacky.fulladle"
             |}""".trimMargin()
    )
    val result = testProjectRoot.gradleRunner()
      .withArguments("help")
      .withGradleVersion("6.0")
      .build()
    assertThat(result.output).contains("SUCCESS")
  }

  @Test
  fun fulladleWithSubmodules() {
    val appFixture = "android-project"
    val libraryFixture = "android-library-project"
    val ignoredLibraryProject = "android-lib-ignored"
    testProjectRoot.newFile("settings.gradle").writeText(
      """
        include '$appFixture'
        include '$libraryFixture'
        include '$ignoredLibraryProject'
        
        dependencyResolutionManagement {
          repositories {
            mavenCentral()
            google()
          }
        }
      """.trimIndent()
    )
    testProjectRoot.setupFixture(appFixture)
    testProjectRoot.setupFixture(libraryFixture)
    File(testProjectRoot.root, libraryFixture).copyRecursively(testProjectRoot.newFile(ignoredLibraryProject), overwrite = true)

    writeBuildGradle(
      """
        buildscript {
            repositories {
                google()
                jcenter()
            }

            dependencies {
                classpath 'com.android.tools.build:gradle:4.2.1'
            }
        }
        
        plugins {
          id "com.osacky.fulladle"
        }
        
        
        fladle {
          serviceAccountCredentials = project.layout.projectDirectory.file("android-project/flank-gradle-5cf02dc90531.json")
        }
      """.trimIndent()
    )

    // Configure second included project to ignore fulladle module
    File(testProjectRoot.root, "$ignoredLibraryProject/build.gradle").appendText(
      """
      fulladleModuleConfig {
        enabled = false
      }
      """.trimIndent()
    )

    val result = testProjectRoot.gradleRunner()
      .withArguments(":printYml")
      .withGradleVersion("6.9")
      .build()

    assertThat(result.output).contains("SUCCESS")
    // Ensure that there is only one additional test APK even though there are two library modules.
    assertThat(result.output).containsMatch(
      """
     > Task :printYml
     gcloud:
       app: [0-9a-zA-Z\/_]*/android-project/build/outputs/apk/debug/android-project-debug.apk
       test: [0-9a-zA-Z\/_]*/android-project/build/outputs/apk/androidTest/debug/android-project-debug-androidTest.apk
       device:
       - model: NexusLowRes
         version: 28
     
       use-orchestrator: false
       auto-google-login: false
       record-video: true
       performance-metrics: true
       timeout: 15m
       num-flaky-test-attempts: 0
     
     flank:
       keep-file-path: false
       additional-app-test-apks:
         - test: [0-9a-zA-Z\/_]*/android-library-project/build/outputs/apk/androidTest/debug/android-library-project-debug-androidTest.apk
       ignore-failed-tests: false
       disable-sharding: false
       smart-flank-disable-upload: false
       legacy-junit-result: false
       full-junit-result: false
       output-style: single
      """.trimIndent()
    )
  }
}
