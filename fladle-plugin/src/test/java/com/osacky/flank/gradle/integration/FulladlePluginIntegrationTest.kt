package com.osacky.flank.gradle.integration

import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class FulladlePluginIntegrationTest {
  @get:Rule
  var testProjectRoot = TemporaryFolder()

  val agpDependency: String = "com.android.tools.build:gradle:4.2.1"

  fun writeBuildGradle(build: String) {
    val file = testProjectRoot.newFile("build.gradle")
    file.writeText(build)
  }

  @Test
  fun fulladleSmokeTest() {
    writeBuildGradle(
      """plugins {
             |  id "com.osacky.fulladle"
             |}
      """.trimMargin(),
    )
    val result =
      testProjectRoot.gradleRunner()
        .withArguments("help")
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
      """.trimIndent(),
    )
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

    // Configure second included project to ignore fulladle module
    File(testProjectRoot.root, "$ignoredLibraryProject/build.gradle").appendText(
      """
      fulladleModuleConfig {
        enabled = false
      }
      """.trimIndent(),
    )

    val result =
      testProjectRoot.gradleRunner()
        .withArguments(":printYml")
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
        - model: SmallPhone.arm
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
      """.trimIndent(),
    )
  }

  @Test
  fun fulladleWithNonAndroidModule() {
    val appFixture = "android-project"
    val libraryFixture = "android-library-project"
    val nonAndroidFixture = "lib1"
    testProjectRoot.newFile("settings.gradle").writeText(
      """
      include '$appFixture'
      include '$libraryFixture'
      include '$nonAndroidFixture'

      dependencyResolutionManagement {
        repositories {
          mavenCentral()
          google()
        }
      }
      """.trimIndent(),
    )
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

    // Configure second included project to ignore fulladle module
    File(testProjectRoot.root, "$nonAndroidFixture/build.gradle").appendText(
      """
      fulladleModuleConfig {
        enabled = false
      }
      """.trimIndent(),
    )

    File(testProjectRoot.root, "$nonAndroidFixture/build.gradle").writeText(
      """
      apply plugin: 'java-library'

      """.trimIndent(),
    )

    val result =
      testProjectRoot.gradleRunner()
        .withArguments(":printYml")
        .build()

    assertThat(result.output).contains("SUCCESS")
  }

  @Test
  fun fulladleWithSubmoduleOverrides() {
    val appFixture = "android-project"
    val appFixture2 = "android-project2"
    val libraryFixture = "android-library-project"
    val libraryFixture2 = "android-lib2"
    testProjectRoot.newFile("settings.gradle").writeText(
      """
      include '$appFixture'
      include '$appFixture2'
      include '$libraryFixture'
      include '$libraryFixture2'

      dependencyResolutionManagement {
        repositories {
          mavenCentral()
          google()
        }
      }
      """.trimIndent(),
    )
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
      testProjectRoot.gradleRunner()
        .withArguments(":printYml")
        .build()

    assertThat(result.output).contains("SUCCESS")
    assertThat(result.output).containsMatch(
      """
      > Task :printYml
      gcloud:
        app: [0-9a-zA-Z\/_]*/android-project/build/outputs/apk/debug/android-project-debug.apk
        test: [0-9a-zA-Z\/_]*/android-project/build/outputs/apk/androidTest/debug/android-project-debug-androidTest.apk
        device:
        - model: SmallPhone.arm
          version: 28

        use-orchestrator: false
        auto-google-login: false
        record-video: true
        performance-metrics: true
        timeout: 15m
        environment-variables:
          clearPackageData: true
          listener: com.osacky.flank.sample.Listener
        num-flaky-test-attempts: 0

      flank:
        keep-file-path: false
        additional-app-test-apks:
          - app: [0-9a-zA-Z\/_]*/android-project2/build/outputs/apk/debug/android-project2-debug.apk
            test: [0-9a-zA-Z\/_]*/android-project2/build/outputs/apk/androidTest/debug/android-project2-debug-androidTest.apk
            max-test-shards: 5
            environment-variables:
              clearPackageData: false

          - test: [0-9a-zA-Z\/_]*/$libraryFixture2/build/outputs/apk/androidTest/debug/android-lib2-debug-androidTest.apk
            max-test-shards: 4
            client-details:
              test-type: PR
              build-number: 132

          - app: dummy_app.apk
            test: [0-9a-zA-Z\/_]*/$libraryFixture/build/outputs/apk/androidTest/debug/android-library-project-debug-androidTest.apk
            max-test-shards: 7
            environment-variables:
              clearPackageData: false
              listener: com.osacky.flank.sample.Listener.Different

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
  fun fulladleWithSpecificFlavor() {
    val appFixture = "android-project"
    val libraryFixture = "android-library-project"
    val flavourProject = "android-project-flavors"
    val flavourLibrary = "android-library-project-flavors"
    testProjectRoot.newFile("settings.gradle").writeText(
      """
      include '$appFixture'
      include '$libraryFixture'       
      include '$flavourProject'
      include '$flavourLibrary'     
      
      dependencyResolutionManagement {
        repositories {
          mavenCentral()
          google()
        }
      }
      """.trimIndent(),
    )
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

    // Configure flavors in project and library
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
      testProjectRoot.gradleRunner()
        .withArguments(":printYml")
        .build()

    assertThat(result.output).contains("SUCCESS")
    assertThat(result.output).containsMatch(
      """
      > Task :printYml
      gcloud:
        app: [0-9a-zA-Z\/_]*/android-project/build/outputs/apk/debug/android-project-debug.apk
        test: [0-9a-zA-Z\/_]*/android-project/build/outputs/apk/androidTest/debug/android-project-debug-androidTest.apk
        device:
        - model: SmallPhone.arm
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
          - app: [0-9a-zA-Z\/_]*/android-project-flavors/build/outputs/apk/vanilla/debug/android-project-flavors-vanilla-debug.apk
            test: [0-9a-zA-Z\/_]*/android-project-flavors/build/outputs/apk/androidTest/vanilla/debug/android-project-flavors-vanilla-debug-androidTest.apk

          - test: [0-9a-zA-Z\/_]*/android-library-project/build/outputs/apk/androidTest/debug/android-library-project-debug-androidTest.apk

          - test: [0-9a-zA-Z\/_]*/android-library-project-flavors/build/outputs/apk/androidTest/strawberry/debug/android-library-project-flavors-strawberry-debug-androidTest.apk

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
  fun fulladleWithDefaultFlavor() {
    val appFixture = "android-project"
    val libraryFixture = "android-library-project"
    val flavourProject = "android-project-flavors"
    val flavourLibrary = "android-library-project-flavors"
    testProjectRoot.newFile("settings.gradle").writeText(
      """
      include '$appFixture'
      include '$libraryFixture'
      include '$flavourProject'
      include '$flavourLibrary'
      
      dependencyResolutionManagement {
        repositories {
          mavenCentral()
          google()
        }
      }
      """.trimIndent(),
    )
    testProjectRoot.setupFixture(appFixture)
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

    val result =
      testProjectRoot.gradleRunner()
        .withArguments(":printYml")
        .build()

    assertThat(result.output).contains("SUCCESS")
    assertThat(result.output).containsMatch(
      """
      > Task :printYml
      gcloud:
        app: [0-9a-zA-Z\/_]*/android-project/build/outputs/apk/debug/android-project-debug.apk
        test: [0-9a-zA-Z\/_]*/android-project/build/outputs/apk/androidTest/debug/android-project-debug-androidTest.apk
        device:
        - model: SmallPhone.arm
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
          - app: [0-9a-zA-Z\/_]*/android-project-flavors/build/outputs/apk/chocolate/debug/android-project-flavors-chocolate-debug.apk
            test: [0-9a-zA-Z\/_]*/android-project-flavors/build/outputs/apk/androidTest/chocolate/debug/android-project-flavors-chocolate-debug-androidTest.apk

          - test: [0-9a-zA-Z\/_]*/android-library-project-flavors/build/outputs/apk/androidTest/lemon/debug/android-library-project-flavors-lemon-debug-androidTest.apk

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
  fun `test root level module overrides with fulladleModuleConfig`() {
    val appFixture = "android-project"
    testProjectRoot.newFile("settings.gradle").writeText(
      """
      include '$appFixture'

      dependencyResolutionManagement {
        repositories {
          mavenCentral()
          google()
        }
      }
      """.trimIndent(),
    )
    testProjectRoot.setupFixture(appFixture)

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
        maxTestShards = 4
      }
      """.trimIndent(),
    )

    File(testProjectRoot.root, "$appFixture/build.gradle").appendText(
      """
      fulladleModuleConfig {
        enabled = true
        maxTestShards = 7
      }
      """.trimIndent(),
    )

    val result =
      testProjectRoot.gradleRunner()
        .withArguments(":printYml")
        .build()
    assertThat(result.output).doesNotContain("max-test-shards: 4")
    assertThat(result.output).contains("max-test-shards: 7")
    assertThat(result.output).doesNotContain("additional-app-test-apks")
  }

  @Test
  fun testAllModulesDisabled() {
    val appFixture = "android-project"
    testProjectRoot.newFile("settings.gradle").writeText(
      """
      include '$appFixture'

      dependencyResolutionManagement {
        repositories {
          mavenCentral()
          google()
        }
      }
      """.trimIndent(),
    )
    testProjectRoot.setupFixture(appFixture)

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

    File(testProjectRoot.root, "$appFixture/build.gradle").appendText(
      """
      fulladleModuleConfig {
        enabled = false
      }
      """.trimIndent(),
    )

    val result =
      testProjectRoot.gradleRunner()
        .withArguments(":printYml")
        .buildAndFail()

    assertThat(result.output).contains("Task :configureFulladle FAILED")
    assertThat(result.output).contains("All modules were disabled for testing in fulladleModuleConfig or the enabled modules had no tests")
  }

  /**
   * this test has all app modules disabled
   * and specifies no debugApk for library module.
   * No module is appropriate for root level flank
   * config, thus we have an illegal state
   */
  @Test
  fun `test fulladle with app modules disabled and library module without debug apk`() {
    val appFixture = "android-project"
    val appFixture2 = "android-project2"
    val libraryFixture = "android-library-project"
    testProjectRoot.newFile("settings.gradle").writeText(
      """
      include '$appFixture'
      include '$appFixture2'
      include '$libraryFixture'

      dependencyResolutionManagement {
        repositories {
          mavenCentral()
          google()
        }
      }
      """.trimIndent(),
    )
    testProjectRoot.setupFixture(appFixture)
    testProjectRoot.setupFixture(appFixture2)
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

    File(testProjectRoot.root, "$appFixture/build.gradle").appendText(
      """
      fulladleModuleConfig {
        enabled = false
      }
      """.trimIndent(),
    )

    File(testProjectRoot.root, "$appFixture2/build.gradle").appendText(
      """
      fulladleModuleConfig {
        enabled = false
      }
      """.trimIndent(),
    )

    val result =
      testProjectRoot.gradleRunner()
        .withArguments(":printYml")
        .buildAndFail()

    assertThat(result.output).contains("Task :configureFulladle FAILED")
    assertThat(result.output).contains(
      "Library module :android-library-project did not specify a debug apk. Library modules do not " +
        "generate a debug apk and one needs to be specified in the fulladleModuleConfig block",
    )
    assertThat(result.output).contains(
      "This is a required parameter in FTL which remains unused for library modules under test, and you can use a dummy apk here",
    )
  }

  /**
   * this test has all app modules disabled
   * and specifies debugApk for library module.
   * the library module is appropriate for
   * root level flank config, thus we have SUCCESS
   */
  @Test
  fun `test fulladle with app modules disabled and library module with debug apk`() {
    val appFixture = "android-project"
    val appFixture2 = "android-project2"
    val libraryFixture = "android-library-project"
    testProjectRoot.newFile("settings.gradle").writeText(
      """
      include '$appFixture'
      include '$appFixture2'
      include '$libraryFixture'

      dependencyResolutionManagement {
        repositories {
          mavenCentral()
          google()
        }
      }
      """.trimIndent(),
    )
    testProjectRoot.setupFixture(appFixture)
    testProjectRoot.setupFixture(appFixture2)
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

    File(testProjectRoot.root, "$appFixture/build.gradle").appendText(
      """
      fulladleModuleConfig {
        enabled = false
      }
      """.trimIndent(),
    )

    File(testProjectRoot.root, "$appFixture2/build.gradle").appendText(
      """
      fulladleModuleConfig {
        enabled = false
      }
      """.trimIndent(),
    )
    File(testProjectRoot.root, "$libraryFixture/build.gradle").appendText(
      """
      fulladleModuleConfig {
         debugApk = "dummy_app.apk"
      }
      """.trimIndent(),
    )

    val result =
      testProjectRoot.gradleRunner()
        .withArguments(":printYml")
        .build()

    assertThat(result.output).doesNotContain("additional-app-test-apks")
    assertThat(result.output).containsMatch(
      """
      > Task :printYml
      gcloud:
        app: dummy_app.apk
        test: [0-9a-zA-Z\/_]*/$libraryFixture/build/outputs/apk/androidTest/debug/android-library-project-debug-androidTest.apk
      """.trimIndent(),
    )
    assertThat(result.output).contains("SUCCESS")
  }

  /**
   * this test has only one of two app module enabled
   * and specifies no debugApk for library module.
   * the enabled app module is appropriate for
   * root level flank config, thus we have SUCCESS
   */
  @Test
  fun `test fulladle with app module enabled and library module without debug apk`() {
    val appFixture = "android-project"
    val appFixture2 = "android-project2"
    val libraryFixture = "android-library-project"
    testProjectRoot.newFile("settings.gradle").writeText(
      """
      include '$appFixture'
      include '$appFixture2'
      include '$libraryFixture'

      dependencyResolutionManagement {
        repositories {
          mavenCentral()
          google()
        }
      }
      """.trimIndent(),
    )
    testProjectRoot.setupFixture(appFixture)
    testProjectRoot.setupFixture(appFixture2)
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

    File(testProjectRoot.root, "$appFixture/build.gradle").appendText(
      """
      fulladleModuleConfig {
        enabled = true
      }
      """.trimIndent(),
    )

    File(testProjectRoot.root, "$appFixture2/build.gradle").appendText(
      """
      fulladleModuleConfig {
        enabled = false
      }
      """.trimIndent(),
    )

    val result =
      testProjectRoot.gradleRunner()
        .withArguments(":printYml")
        .build()

    assertThat(result.output).contains("BUILD SUCCESSFUL")
  }

  @Test
  fun `configureFulladle task is compatible with configuration cache`() {
    val appFixture = "android-project"
    val libraryFixture = "android-library-project"
    testProjectRoot.newFile("settings.gradle").writeText(
      """
      include '$appFixture'
      include '$libraryFixture'
      
      dependencyResolutionManagement {
        repositories {
          mavenCentral()
          google()
        }
      }
      """.trimIndent(),
    )
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

    // First run with configuration cache - should store the cache
    val firstResult = testProjectRoot.gradleRunner()
      .withArguments("configureFulladle", "--configuration-cache")
      .build()

    assertThat(firstResult.output).contains("BUILD SUCCESSFUL")
    assertThat(firstResult.output).contains("Configuration cache entry stored")

    // Second run with configuration cache - should reuse the cache
    val secondResult = testProjectRoot.gradleRunner()
      .withArguments("configureFulladle", "--configuration-cache")
      .build()

    assertThat(secondResult.output).contains("BUILD SUCCESSFUL")
    assertThat(secondResult.output).contains("Configuration cache entry reused")
  }

  @Test
  fun fulladleWithAbiSplits() {
    val appFixtureWithAbiSplits = "android-project-with-abi-splits"
    val appFixture = "android-project"
    val appFixtureTwo = "android-project2"
    val libraryFixture = "android-library-project"
    testProjectRoot.newFile("settings.gradle").writeText(
      """
      include '$appFixtureWithAbiSplits'
      include '$appFixture'
      include '$appFixtureTwo'
      include '$libraryFixture'

      dependencyResolutionManagement {
        repositories {
          mavenCentral()
          google()
        }
      }
      """.trimIndent(),
    )
    testProjectRoot.setupFixture(appFixture)
    testProjectRoot.setupFixture(appFixtureTwo)
    testProjectRoot.setupFixture(libraryFixture)
    File(testProjectRoot.root, appFixture).copyRecursively(testProjectRoot.newFile(appFixtureWithAbiSplits), overwrite = true)

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
        abi = "armeabi-v7a"
      }
      """.trimIndent(),
    )

    // Overwrite android-project fixture to include ABI splits.
    File(testProjectRoot.root, "$appFixtureWithAbiSplits/build.gradle").writeText(
      """
      plugins {
          id "com.android.application"
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
         splits {
             abi {
                enable true
                reset()
                include "x86", "armeabi-v7a", "arm64-v8a", "x86_64"
                universalApk false
             }
         }
      }
      """.trimIndent(),
    )

    val result =
      testProjectRoot.gradleRunner()
        .withArguments(":printYml")
        .build()

    assertThat(result.output).contains("SUCCESS")
    // Ensure that:
    // - Any application modules that use ABI splits use APKs for the specified split (armeabi-v7a).
    // - Any application modules that don't use ABI splits are still present in additional-app-test-apks.
    assertThat(result.output).containsMatch(
      """
      > Task :printYml
      gcloud:
        app: [0-9a-zA-Z\/_]*/android-project/build/outputs/apk/debug/android-project-debug.apk
        test: [0-9a-zA-Z\/_]*/android-project/build/outputs/apk/androidTest/debug/android-project-debug-androidTest.apk
        device:
        - model: SmallPhone.arm
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
          - app: [0-9a-zA-Z\/_]*/android-project-with-abi-splits/build/outputs/apk/debug/android-project-with-abi-splits-armeabi-v7a-debug.apk
            test: [0-9a-zA-Z\/_]*/android-project-with-abi-splits/build/outputs/apk/androidTest/debug/android-project-with-abi-splits-debug-androidTest.apk

          - app: [0-9a-zA-Z\/_]*/android-project2/build/outputs/apk/debug/android-project2-debug.apk
            test: [0-9a-zA-Z\/_]*/android-project2/build/outputs/apk/androidTest/debug/android-project2-debug-androidTest.apk
            max-test-shards: 5
            environment-variables:
              clearPackageData: false

          - test: [0-9a-zA-Z\/_]*/android-library-project/build/outputs/apk/androidTest/debug/android-library-project-debug-androidTest.apk

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
