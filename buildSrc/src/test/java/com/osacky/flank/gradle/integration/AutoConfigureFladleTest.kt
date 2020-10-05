package com.osacky.flank.gradle.integration

import com.google.common.truth.Truth.assertThat
import org.gradle.testkit.runner.GradleRunner
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class AutoConfigureFladleTest {

  @get:Rule
  var testProjectRoot = TemporaryFolder()
  fun writeBuildGradle(build: String) {
    val file = testProjectRoot.newFile("build.gradle")
    file.writeText(build)
  }

  @Test
  fun testAndroidProject() {
    val fixtureName = "android-project"
    testProjectRoot.newFile("local.properties").writeText("sdk.dir=${androidHome()}\n")
    testProjectRoot.newFile("gradle.properties").writeText("android.useAndroidX=true")
    writeBuildGradle(
      """
            allprojects {
              repositories {
                google()
                mavenCentral()
              }
            }
      """.trimIndent()
    )
    testProjectRoot.newFile("settings.gradle").writeText(
      """
        include '$fixtureName'
      """.trimIndent()
    )

    testProjectRoot.setupFixture(fixtureName)
    testProjectRoot.root.walk().forEach {
      println(it)
    }

    val result = GradleRunner.create()
      .withProjectDir(testProjectRoot.root)
      .withPluginClasspath()
      .withArguments("assembleDebug", "assembleDebugAndroidTest", "printYml", "--stacktrace")
      .build()

    assertThat(result.output).contains("BUILD SUCCESSFUL")
    assertThat(result.output).containsMatch(
      """
        > Task :android-project:printYml
        gcloud:
          app: [0-9a-zA-Z\/_]*/android-project/build/outputs/apk/debug/android-project-debug.apk
          test: [0-9a-zA-Z\/_]*/android-project/build/outputs/apk/androidTest/debug/android-project-debug-androidTest.apk
          device:
          - model: Pixel2
            version: 26
          - model: Nexus5
            version: 23

          use-orchestrator: true
          environment-variables:
            clearPackageData: true
          test-targets:
          - class com.osacky.flank.gradle.sample.ExampleInstrumentedTest#seeView

        flank:
          smart-flank-gcs-path: gs://test-lab-yr9w6qsdvy45q-iurp80dm95h8a/flank/test_app_android.xml
      """.trimIndent()
    )
  }
}
