package com.osacky.flank.gradle.integration

import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

private const val commonScriptPart = """
  plugins {
    id 'com.android.application'
    id 'com.osacky.fladle'
  }

  android {
    compileSdk 33
     namespace "com.osacky.flank.gradle.sample"
    defaultConfig {
      applicationId "com.osacky.flank.gradle.sample"
      minSdk 23
      targetSdk 29
      versionCode 1
      versionName "1.0"
      testInstrumentationRunner "androidx.test.runner.AndroidJUnitRunner"
    }
    testOptions {
      execution 'ANDROIDX_TEST_ORCHESTRATOR'
    }
  }
"""

class SanityWithAutoConfigureTest {
  @get:Rule
  var testProjectRoot = TemporaryFolder()

  @Before
  fun setUp() {
    testProjectRoot.newFile("flank-gradle-service.json").writeText("{}")
    testProjectRoot.newFile("local.properties").writeText("sdk.dir=${androidHome()}\n")
    testProjectRoot.newFile("gradle.properties").writeText("android.useAndroidX=true")
  }

  @Test
  fun `test auto configuration with sanityRobo set (inner config)`() {
    testProjectRoot.writeBuildDotGradle(
      """
            $commonScriptPart

            fladle {
              serviceAccountCredentials = project.layout.projectDirectory.file("flank-gradle-service.json")
              useOrchestrator = true
              environmentVariables = [
                "clearPackageData": "true"
              ]
              testTargets = [
                "class com.osacky.flank.gradle.sample.ExampleInstrumentedTest#seeView"
              ]
              devices = [
                [ "model": "Pixel2", "version": "26" ],
                [ "model": "Nexus5", "version": "23" ]
              ]
              smartFlankGcsPath = "gs://test-lab-yr9w6qsdvy45q-iurp80dm95h8a/flank/test_app_android.xml"
              configs {
                sanity {
                  clearPropertiesForSanityRobo()
                  useOrchestrator.set(false)
                  testTargets.set(project.provider { [
                    "class com.osacky.flank.gradle.sample.ExampleInstrumentedTest#runAndFail"
                  ] })
                  flakyTestAttempts.set(3)
                }
                oranges {
                  useOrchestrator.set(false)
                  testTargets.set(project.provider { [
                    "class com.osacky.flank.gradle.sample.ExampleInstrumentedTest#runAndFail"
                  ] })
                  flakyTestAttempts.set(6)
                }
              }
            }
      """.trimMargin(),
    )

    val runner = testProjectRoot.gradleRunner()
    val baseResult = runner.withArguments("printYml").build()

    assertThat(baseResult.output).contains("BUILD SUCCESSFUL")
    assertThat(baseResult.output).containsMatch(
      """
      gcloud:
        app: [0-9a-zA-Z\/_]*/build/outputs/apk/debug/[0-9a-zA-Z\/_]*-debug.apk
        test: [0-9a-zA-Z\/_]*/build/outputs/apk/androidTest/debug/[0-9a-zA-Z\/_]*-debug-androidTest.apk
        device:
        - model: Pixel2
          version: 26
        - model: Nexus5
          version: 23

        use-orchestrator: true
        auto-google-login: false
        record-video: true
        performance-metrics: true
        timeout: 15m
        environment-variables:
          clearPackageData: true
        test-targets:
        - class com.osacky.flank.gradle.sample.ExampleInstrumentedTest#seeView
        num-flaky-test-attempts: 0

      flank:
        smart-flank-gcs-path: gs://test-lab-yr9w6qsdvy45q-iurp80dm95h8a/flank/test_app_android.xml
        keep-file-path: false
        ignore-failed-tests: false
        disable-sharding: false
        smart-flank-disable-upload: false
        legacy-junit-result: false
        full-junit-result: false
        output-style: single
      """.trimIndent(),
    )

    val sanityResult = runner.withArguments("printYmlSanity").build()

    assertThat(sanityResult.output).contains("BUILD SUCCESSFUL")
    assertThat(sanityResult.output).containsMatch(
      """
      gcloud:
        app: [0-9a-zA-Z\/_]*/build/outputs/apk/debug/[0-9a-zA-Z\/_]*-debug.apk
        device:
        - model: Pixel2
          version: 26
        - model: Nexus5
          version: 23

        use-orchestrator: false
        auto-google-login: false
        record-video: true
        performance-metrics: true
        timeout: 15m
        environment-variables:
          clearPackageData: true
        test-targets:
        - class com.osacky.flank.gradle.sample.ExampleInstrumentedTest#runAndFail
        num-flaky-test-attempts: 3

      flank:
        smart-flank-gcs-path: gs://test-lab-yr9w6qsdvy45q-iurp80dm95h8a/flank/test_app_android.xml
        keep-file-path: false
        ignore-failed-tests: false
        disable-sharding: false
        smart-flank-disable-upload: false
        legacy-junit-result: false
        full-junit-result: false
        output-style: single
      """.trimIndent(),
    )

    val orangesResult = runner.withArguments("printYmlOranges").build()

    assertThat(orangesResult.output).contains("BUILD SUCCESSFUL")
    assertThat(orangesResult.output).containsMatch(
      """
      gcloud:
        app: [0-9a-zA-Z\/_]*/build/outputs/apk/debug/[0-9a-zA-Z\/_]*-debug.apk
        test: [0-9a-zA-Z\/_]*/build/outputs/apk/androidTest/debug/[0-9a-zA-Z\/_]*-debug-androidTest.apk
        device:
        - model: Pixel2
          version: 26
        - model: Nexus5
          version: 23

        use-orchestrator: false
        auto-google-login: false
        record-video: true
        performance-metrics: true
        timeout: 15m
        environment-variables:
          clearPackageData: true
        test-targets:
        - class com.osacky.flank.gradle.sample.ExampleInstrumentedTest#runAndFail
        num-flaky-test-attempts: 6

      flank:
        smart-flank-gcs-path: gs://test-lab-yr9w6qsdvy45q-iurp80dm95h8a/flank/test_app_android.xml
        keep-file-path: false
        ignore-failed-tests: false
        disable-sharding: false
        smart-flank-disable-upload: false
        legacy-junit-result: false
        full-junit-result: false
        output-style: single
      """.trimIndent(),
    )
  }

  @Test
  fun `test auto configuration with sanityRobo set (base config)`() {
    testProjectRoot.writeBuildDotGradle(
      """
            $commonScriptPart

            fladle {
              sanityRobo = true
              serviceAccountCredentials = project.layout.projectDirectory.file("flank-gradle-service.json")
              useOrchestrator = true
              environmentVariables = [
                "clearPackageData": "true"
              ]
              testTargets = [
                "class com.osacky.flank.gradle.sample.ExampleInstrumentedTest#seeView"
              ]
              devices = [
                [ "model": "Pixel2", "version": "26" ],
                [ "model": "Nexus5", "version": "23" ]
              ]
              smartFlankGcsPath = "gs://test-lab-yr9w6qsdvy45q-iurp80dm95h8a/flank/test_app_android.xml"
              configs {
                oranges {
                  instrumentationApk.set("instrumentation-apk-not-detected-from-root.apk")
                  useOrchestrator.set(false)
                  testTargets.set(project.provider { [
                    "class com.osacky.flank.gradle.sample.ExampleInstrumentedTest#runAndFail"
                  ] })
                  flakyTestAttempts.set(3)
                  sanityRobo.set(false)
                }
              }
            }
      """.trimMargin(),
    )

    val runner = testProjectRoot.gradleRunner()
    val baseResult = runner.withArguments("printYml").build()

    assertThat(baseResult.output).contains("BUILD SUCCESSFUL")
    assertThat(baseResult.output).containsMatch(
      """
      gcloud:
        app: [0-9a-zA-Z\/_]*/build/outputs/apk/debug/[0-9a-zA-Z\/_]*-debug.apk
        device:
        - model: Pixel2
          version: 26
        - model: Nexus5
          version: 23

        use-orchestrator: true
        auto-google-login: false
        record-video: true
        performance-metrics: true
        timeout: 15m
        environment-variables:
          clearPackageData: true
        test-targets:
        - class com.osacky.flank.gradle.sample.ExampleInstrumentedTest#seeView
        num-flaky-test-attempts: 0

      flank:
        smart-flank-gcs-path: gs://test-lab-yr9w6qsdvy45q-iurp80dm95h8a/flank/test_app_android.xml
        keep-file-path: false
        ignore-failed-tests: false
        disable-sharding: false
        smart-flank-disable-upload: false
        legacy-junit-result: false
        full-junit-result: false
        output-style: single
      """.trimIndent(),
    )

    val orangesResult = runner.withArguments("printYmlOranges").build()

    assertThat(orangesResult.output).contains("BUILD SUCCESSFUL")
    assertThat(orangesResult.output).containsMatch(
      """
      gcloud:
        app: [0-9a-zA-Z\/_]*/build/outputs/apk/debug/[0-9a-zA-Z\/_]*-debug.apk
        test: instrumentation-apk-not-detected-from-root.apk
        device:
        - model: Pixel2
          version: 26
        - model: Nexus5
          version: 23

        use-orchestrator: false
        auto-google-login: false
        record-video: true
        performance-metrics: true
        timeout: 15m
        environment-variables:
          clearPackageData: true
        test-targets:
        - class com.osacky.flank.gradle.sample.ExampleInstrumentedTest#runAndFail
        num-flaky-test-attempts: 3

      flank:
        smart-flank-gcs-path: gs://test-lab-yr9w6qsdvy45q-iurp80dm95h8a/flank/test_app_android.xml
        keep-file-path: false
        ignore-failed-tests: false
        disable-sharding: false
        smart-flank-disable-upload: false
        legacy-junit-result: false
        full-junit-result: false
        output-style: single
      """.trimIndent(),
    )
  }
}
