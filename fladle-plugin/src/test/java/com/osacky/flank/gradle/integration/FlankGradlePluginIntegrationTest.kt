package com.osacky.flank.gradle.integration

import com.google.common.truth.Truth.assertThat
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class FlankGradlePluginIntegrationTest {

  @get:Rule
  var testProjectRoot = TemporaryFolder()

  val minSupportGradleVersion = "5.5"
  val oldVersion = "5.3.1"

  fun writeBuildGradle(build: String) {
    val file = testProjectRoot.newFile("build.gradle")
    file.writeText(build)
  }

  @Test
  fun testLowGradleVersionFailsBuild() {
    writeBuildGradle(
      """plugins {
             |  id "com.osacky.fladle"
             |}""".trimMargin()
    )
    val result = GradleRunner.create()
      .withProjectDir(testProjectRoot.root)
      .withPluginClasspath()
      .withGradleVersion(oldVersion)
      .buildAndFail()
    assertThat(result.output).contains("Fladle requires at minimum version Gradle 5.5. Detected version Gradle 5.3.1")
  }

  @Test
  fun testGradleSixZero() {
    writeBuildGradle(
      """plugins {
             |  id "com.osacky.fladle"
             |}""".trimMargin()
    )
    val result = GradleRunner.create()
      .withProjectDir(testProjectRoot.root)
      .withPluginClasspath()
      .withGradleVersion("6.0")
      .build()

    assertThat(result.output).contains("SUCCESS")
  }

  @Test
  fun testMinSupportedGradleVersionWorks() {
    writeBuildGradle(
      """plugins {
             |  id "com.osacky.fladle"
             |}""".trimMargin()
    )
    GradleRunner.create()
      .withProjectDir(testProjectRoot.root)
      .withPluginClasspath()
      .withGradleVersion(minSupportGradleVersion)
      .build()
  }

  @Test
  fun testMissingServiceAccountWithProjectId() {
    writeBuildGradle(
      """plugins {
             |  id "com.osacky.fladle"
             |}
             |
             |fladle {
             |  projectId = "foo-project"
             |  debugApk = "foo"
             |  instrumentationApk = "fakeInstrument.apk"
             |}""".trimMargin()
    )
    GradleRunner.create()
      .withProjectDir(testProjectRoot.root)
      .withPluginClasspath()
      .withGradleVersion(minSupportGradleVersion)
      .withArguments("printYml")
      .build()
  }

  @Test
  fun testMissingServiceAccountFailsBuild() {
    writeBuildGradle(
      """plugins {
             |  id "com.osacky.fladle"
             |}
             |
             |fladle {
             |  debugApk = "foo"
             |}""".trimMargin()
    )
    val result = GradleRunner.create()
      .withProjectDir(testProjectRoot.root)
      .withPluginClasspath()
      .withGradleVersion(minSupportGradleVersion)
      .withArguments("printYml")
      .buildAndFail()
    assertThat(result.output).contains("ServiceAccountCredentials in fladle extension not set. https://runningcode.github.io/fladle/configuration/#serviceaccountcredentials")
  }

  @Test
  fun testMissingApkFailsBuild() {
    writeBuildGradle(
      """plugins {
             |  id "com.osacky.fladle"
             |}
             |fladle {
             |  serviceAccountCredentials = project.layout.projectDirectory.file("foo")
             |}
             |""".trimMargin()
    )
    testProjectRoot.newFile("foo").writeText("{}")
    val result = GradleRunner.create()
      .withProjectDir(testProjectRoot.root)
      .withPluginClasspath()
      .withGradleVersion(minSupportGradleVersion)
      .withArguments("runFlank")
      .buildAndFail()
    assertThat(result.output).contains("debugApk must be specified")
  }

  private fun setUpDependOnAssemble(dependsOnAssemble: Boolean): BuildResult {
    writeBuildGradle(
      """plugins {
          id "com.osacky.fladle"
          id "com.android.application"
         }
         repositories {
              google()
              mavenCentral()
          }
         android {
             compileSdkVersion 29
             defaultConfig {
                 applicationId "com.osacky.flank.gradle.sample"
                 minSdkVersion 23
                 targetSdkVersion 29
                 versionCode 1
                 versionName "1.0"
                 testInstrumentationRunner "androidx.test.runner.AndroidJUnitRunner"
             }
             testOptions {
                 execution 'ANDROIDX_TEST_ORCHESTRATOR'
              }
         }
         fladle {
           serviceAccountCredentials = project.layout.projectDirectory.file("foo")
           dependOnAssemble = $dependsOnAssemble
         }
      """.trimIndent()
    )
    testProjectRoot.newFile("foo").writeText("{}")
    testProjectRoot.newFolder("src/main")
    testProjectRoot.newFile("src/main/AndroidManifest.xml").writeText(
      """
        <?xml version="1.0" encoding="utf-8"?>
        <manifest package="com.osacky.flank.gradle.sample" xmlns:android="http://schemas.android.com/apk/res/android" />
      """.trimIndent()
    )
    val result = GradleRunner.create()
      .withProjectDir(testProjectRoot.root)
      .withPluginClasspath()
      .withArguments("runFlank", "--dry-run")
      .build()

    return result
  }

  @Test
  fun testWithDependOnAssemble() {
    val result = setUpDependOnAssemble(true)
    assertThat(result.output).contains(":assembleDebug")
    assertThat(result.output).contains(":assembleDebugAndroidTest")
    assertThat(result.output).doesNotContain(":assembleRelease")
  }

  @Test
  fun testWithOutDependOnAssemble() {
    val result = setUpDependOnAssemble(false)
    assertThat(result.output).doesNotContain(":assembleDebug")
    assertThat(result.output).doesNotContain(":assembleRelease")
    assertThat(result.output).doesNotContain(":assembleDebugAndroidTest")
  }

  @Test
  fun testMissingInstrumentationApkFailsBuild() {
    writeBuildGradle(
      """plugins {
            id "com.osacky.fladle"
           }
           fladle {
             serviceAccountCredentials = project.layout.projectDirectory.file("foo")
             debugApk = "test-debug.apk"
           }
      """.trimIndent()
    )
    testProjectRoot.newFile("foo").writeText("{}")
    val result = GradleRunner.create()
      .withProjectDir(testProjectRoot.root)
      .withPluginClasspath()
      .withGradleVersion(minSupportGradleVersion)
      .withArguments("runFlank")
      .buildAndFail()

    assertThat(result.output).contains("Must specify either a instrumentationApk file or a roboScript file.")
  }

  @Test
  fun testSpecifyingBothInstrumentationAndRoboscriptFailsBuild() {
    writeBuildGradle(
      """plugins {
            id "com.osacky.fladle"
           }
           fladle {
             serviceAccountCredentials = project.layout.projectDirectory.file("foo")
             debugApk = "test-debug.apk"
             instrumentationApk = "instrumentation-debug.apk"
             roboScript = "foo.script"
           }
      """.trimIndent()
    )
    testProjectRoot.newFile("foo").writeText("{}")
    val result = GradleRunner.create()
      .withProjectDir(testProjectRoot.root)
      .withPluginClasspath()
      .withGradleVersion(minSupportGradleVersion)
      .withArguments("printYml")
      .buildAndFail()

    assertThat(result.output).contains("Both instrumentationApk file and roboScript file were specified, but only one is expected.")
  }

  @Test
  fun testGradleSevenCompat() {
    writeBuildGradle(
      """plugins {
           id "com.osacky.fladle"
         }
         fladle {
           serviceAccountCredentials = project.layout.projectDirectory.file("foo")
           debugApk = "test-debug.apk"
           instrumentationApk = "instrumentation-debug.apk"
           configs {
             fooConfig {
             }
           }
         }
         """.trimMargin()
    )
    testProjectRoot.newFile("foo").writeText("{}")
    val result = GradleRunner.create()
      .withProjectDir(testProjectRoot.root)
      .withPluginClasspath()
      .withGradleVersion("7.0-milestone-3")
      .withArguments("printYmlFooConfig")
      .build()
    assertThat(result.task(":printYmlFooConfig")!!.outcome).isEqualTo(TaskOutcome.SUCCESS)
  }
}
