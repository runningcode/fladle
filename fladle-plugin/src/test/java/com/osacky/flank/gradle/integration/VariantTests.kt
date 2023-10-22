package com.osacky.flank.gradle.integration

import com.google.common.truth.Truth.assertThat
import org.gradle.testkit.runner.BuildResult
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class VariantTests {

  @get:Rule
  var testProjectRoot = TemporaryFolder()

  @Test
  fun testWithDependOnAssembleNoFlavors() {
    val result = setUpDependOnAssemble(true, withFlavors = false)
    assertThat(result.output).contains(":assembleDebug")
    assertThat(result.output).contains(":assembleDebugAndroidTest")
    assertThat(result.output).doesNotContain(":assembleRelease")
  }

  @Test
  fun testWithOutDependOnAssembleNoFlavors() {
    val result = setUpDependOnAssemble(false, withFlavors = false)
    assertThat(result.output).doesNotContain(":assembleDebug")
    assertThat(result.output).doesNotContain(":assembleDebugAndroidTest")
    assertThat(result.output).doesNotContain(":assembleRelease")
  }

  @Test
  fun testWithDependOnAssembleAndFlavors() {
    val result = setUpDependOnAssemble(true, withFlavors = true)
    assertThat(result.output).contains(":assembleChocolateDebug")
    assertThat(result.output).contains(":assembleChocolateDebugAndroidTest")
    assertThat(result.output).doesNotContain(":assembleChocolateRelease")
    assertThat(result.output).doesNotContain(":assembleVanilla")
  }

  @Test
  fun testWithOutDependOnAssembleAndFlavors() {
    val result = setUpDependOnAssemble(false, withFlavors = true)
    assertThat(result.output).doesNotMatch(":assemble.*")
    assertThat(result.output).doesNotContain(":assembleChocolateDebug")
    assertThat(result.output).doesNotContain(":assembleDebug")
    assertThat(result.output).doesNotContain(":assembleRelease")
    assertThat(result.output).doesNotContain(":assembleDebugAndroidTest")
  }

  @Test
  fun testAdditionalFladleConfigForVariant() {
    val result = setUpDependOnAssemble(
      dependsOnAssemble = true, withFlavors = true,
      withFladleConfig = """
      configs {
          vanilla {
              variant.set("vanillaDebug")
          }
      }
      """.trimIndent(),
      withTask = "runFlankVanilla"
    )
    assertThat(result.output).contains(":assembleVanillaDebug")
    assertThat(result.output).contains(":assembleVanillaDebugAndroidTest")
    assertThat(result.output).doesNotContain(":assembleVanillaRelease")
    assertThat(result.output).doesNotContain(":assembleChocolate")

    // See #60 https://github.com/runningcode/fladle/issues/60
    /**
     testProjectRoot.writeEmptyServiceCredential()
     val resultPrint = testProjectRoot.gradleRunner()
     .withArguments("printYmlVanilla")
     .build()
     assertThat(resultPrint.output).contains("build/outputs/apk/vanilla/debug/chocovanilla-vanilla-debug.apk")
     assertThat(resultPrint.output).contains("build/outputs/apk/androidTest/vanilla/debug/chocovanilla-vanilla-debug-androidTest.apk")
     **/
  }

  @Test
  fun testAbiSplits() {
    val result = setUpDependOnAssemble(true, withAbiSplit = true, withTask = "printYml", dryRun = false)
    assertThat(result.output).containsMatch("""\s+app: \S*-x86-debug\.apk""")
    // Test APKs do not use ABI splits.
    assertThat(result.output).doesNotContainMatch("""\s+test: \S*-x86-\S*androidTest\.apk""")
  }

  @Test
  fun testAbiSplitsWithVariants() {
    val result = setUpDependOnAssemble(true, withFlavors = true, withAbiSplit = true, withTask = "printYml", dryRun = false)
    assertThat(result.output).containsMatch("""\s+app: \S*-chocolate-x86-debug\.apk""")
    assertThat(result.output).doesNotContainMatch("""\s+test: \S*-x86-\S*androidTest\.apk""")
  }

  private fun setUpDependOnAssemble(
    dependsOnAssemble: Boolean,
    withFlavors: Boolean = false,
    withAbiSplit: Boolean = false,
    withFladleConfig: String = "",
    withTask: String = "runFlank",
    dryRun: Boolean = true,
  ): BuildResult {
    testProjectRoot.newFile("settings.gradle").writeText("""rootProject.name = 'chocovanilla'
      |include ':android-project'
    """.trimMargin())
    testProjectRoot.setupFixture("android-project")
    val flavors = if (withFlavors) {
      """
             flavorDimensions "flavor"
             productFlavors {
                 chocolate {
                     dimension "flavor"
                 }
                 vanilla {
                     dimension "flavor"
                 }
             }
      """.trimIndent()
    } else { "" }
    val abiSplits = if (withAbiSplit) {
      """
      splits {
          abi {
            enable true
            reset()
            include "x86", "x86_64"
            universalApk false
          }
      }
      """.trimIndent()
    } else ""
    val variant = if (withFlavors) { """variant = "chocolateDebug"""" } else { "" }
    val abi = if (withAbiSplit) { "abi = \"x86\"" } else ""
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
             $flavors
             $abiSplits
         }
         fladle {
           serviceAccountCredentials = project.layout.projectDirectory.file("flank-gradle-5cf02dc90531.json")
           dependOnAssemble = $dependsOnAssemble
           $variant
           $abi
           $withFladleConfig
         }
      """.trimIndent()
    )

    val arguments = mutableListOf(withTask)
    if (dryRun) {
      arguments.add("--dry-run")
    }
    // print directory structure for debugging
    testProjectRoot.root.walkTopDown().forEach { println(it) }
    return testProjectRoot.gradleRunner()
      .withArguments(arguments)
      .build()
  }

  private fun writeBuildGradle(build: String) {
    // Overwrite existing build.gradle file in "android-project" directory with new text
    val file = File(testProjectRoot.root, "android-project/build.gradle")
    file.writeText(build)
  }
}
