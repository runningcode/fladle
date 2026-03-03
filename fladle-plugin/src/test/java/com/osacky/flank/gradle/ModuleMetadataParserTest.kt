package com.osacky.flank.gradle

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.io.File

class ModuleMetadataParserTest {
  @Test
  fun `parse valid metadata JSON`() {
    val json =
      """
      {
        "modulePath": ":app",
        "moduleType": "application",
        "enabled": true,
        "hasAndroidTestDir": true,
        "maxTestShards": 4,
        "variant": "debug",
        "debugApk": "/path/to/debug.apk",
        "clientDetails": {
          "test-type": "PR",
          "build-number": "132"
        },
        "environmentVariables": {
          "clearPackageData": "true"
        },
        "variants": [
          {"variantName":"debug","testApkPath":"/path/to/test.apk","appApkPath":"/path/to/app.apk"}
        ]
      }
      """.trimIndent()

    val result = ModuleMetadataParser.parseJson(json)

    assertThat(result.modulePath).isEqualTo(":app")
    assertThat(result.moduleType).isEqualTo("application")
    assertThat(result.enabled).isTrue()
    assertThat(result.hasAndroidTestDir).isTrue()
    assertThat(result.maxTestShards).isEqualTo(4)
    assertThat(result.variant).isEqualTo("debug")
    assertThat(result.debugApk).isEqualTo("/path/to/debug.apk")
    assertThat(result.clientDetails).containsExactly("test-type", "PR", "build-number", "132")
    assertThat(result.environmentVariables).containsExactly("clearPackageData", "true")
    assertThat(result.variants).hasSize(1)
    assertThat(result.variants[0].variantName).isEqualTo("debug")
    assertThat(result.variants[0].testApkPath).isEqualTo("/path/to/test.apk")
    assertThat(result.variants[0].appApkPath).isEqualTo("/path/to/app.apk")
    assertThat(result.variants[0].abiName).isNull()
  }

  @Test
  fun `parse metadata without optional fields`() {
    val json =
      """
      {
        "modulePath": ":lib",
        "moduleType": "library",
        "enabled": true,
        "hasAndroidTestDir": true,
        "variants": [
          {"variantName":"debug","testApkPath":"/path/to/test.apk"}
        ]
      }
      """.trimIndent()

    val result = ModuleMetadataParser.parseJson(json)

    assertThat(result.modulePath).isEqualTo(":lib")
    assertThat(result.moduleType).isEqualTo("library")
    assertThat(result.maxTestShards).isNull()
    assertThat(result.variant).isNull()
    assertThat(result.debugApk).isNull()
    assertThat(result.clientDetails).isEmpty()
    assertThat(result.environmentVariables).isEmpty()
    assertThat(result.variants).hasSize(1)
    assertThat(result.variants[0].appApkPath).isNull()
    assertThat(result.variants[0].abiName).isNull()
  }

  @Test
  fun `parse metadata with ABI split`() {
    val json =
      """
      {
        "modulePath": ":app",
        "moduleType": "application",
        "enabled": true,
        "hasAndroidTestDir": true,
        "variants": [
          {"variantName":"debug","testApkPath":"/test.apk","appApkPath":"/app-x86.apk","abiName":"x86"},
          {"variantName":"debug","testApkPath":"/test.apk","appApkPath":"/app-arm.apk","abiName":"armeabi-v7a"}
        ]
      }
      """.trimIndent()

    val result = ModuleMetadataParser.parseJson(json)

    assertThat(result.variants).hasSize(2)
    assertThat(result.variants[0].abiName).isEqualTo("x86")
    assertThat(result.variants[1].abiName).isEqualTo("armeabi-v7a")
  }

  @Test
  fun `assemble config with app and library modules`() {
    val appModule =
      ModuleMetadata(
        modulePath = ":app",
        moduleType = "application",
        enabled = true,
        hasAndroidTestDir = true,
        maxTestShards = null,
        variant = null,
        debugApk = null,
        clientDetails = emptyMap(),
        environmentVariables = emptyMap(),
        variants =
          listOf(
            VariantMetadata("debug", "/app/debug.apk", "/app/test.apk", null),
          ),
      )
    val libModule =
      ModuleMetadata(
        modulePath = ":lib",
        moduleType = "library",
        enabled = true,
        hasAndroidTestDir = true,
        maxTestShards = null,
        variant = null,
        debugApk = null,
        clientDetails = emptyMap(),
        environmentVariables = emptyMap(),
        variants =
          listOf(
            VariantMetadata("debug", null, "/lib/test.apk", null),
          ),
      )

    val result = ModuleMetadataParser.assembleFulladleConfig(listOf(appModule, libModule), null)

    assertThat(result.debugApk).isEqualTo("/app/debug.apk")
    assertThat(result.instrumentationApk).isEqualTo("/app/test.apk")
    assertThat(result.additionalTestApks).hasSize(1)
    assertThat(result.additionalTestApks[0]).contains("- test: /lib/test.apk")
  }

  @Test
  fun `assemble config with disabled module`() {
    val enabledModule =
      ModuleMetadata(
        modulePath = ":app",
        moduleType = "application",
        enabled = true,
        hasAndroidTestDir = true,
        maxTestShards = null,
        variant = null,
        debugApk = null,
        clientDetails = emptyMap(),
        environmentVariables = emptyMap(),
        variants =
          listOf(
            VariantMetadata("debug", "/app/debug.apk", "/app/test.apk", null),
          ),
      )
    val disabledModule =
      ModuleMetadata(
        modulePath = ":lib",
        moduleType = "library",
        enabled = false,
        hasAndroidTestDir = true,
        maxTestShards = null,
        variant = null,
        debugApk = null,
        clientDetails = emptyMap(),
        environmentVariables = emptyMap(),
        variants =
          listOf(
            VariantMetadata("debug", null, "/lib/test.apk", null),
          ),
      )

    val result = ModuleMetadataParser.assembleFulladleConfig(listOf(enabledModule, disabledModule), null)

    assertThat(result.debugApk).isEqualTo("/app/debug.apk")
    assertThat(result.instrumentationApk).isEqualTo("/app/test.apk")
    assertThat(result.additionalTestApks).isEmpty()
  }

  @Test(expected = IllegalStateException::class)
  fun `assemble config throws when all modules disabled`() {
    val disabledModule =
      ModuleMetadata(
        modulePath = ":app",
        moduleType = "application",
        enabled = false,
        hasAndroidTestDir = true,
        maxTestShards = null,
        variant = null,
        debugApk = null,
        clientDetails = emptyMap(),
        environmentVariables = emptyMap(),
        variants =
          listOf(
            VariantMetadata("debug", "/app/debug.apk", "/app/test.apk", null),
          ),
      )

    ModuleMetadataParser.assembleFulladleConfig(listOf(disabledModule), null)
  }

  @Test
  fun `assemble config with ABI filter`() {
    val module =
      ModuleMetadata(
        modulePath = ":app",
        moduleType = "application",
        enabled = true,
        hasAndroidTestDir = true,
        maxTestShards = null,
        variant = null,
        debugApk = null,
        clientDetails = emptyMap(),
        environmentVariables = emptyMap(),
        variants =
          listOf(
            VariantMetadata("debug", "/app-x86.apk", "/test.apk", "x86"),
            VariantMetadata("debug", "/app-arm.apk", "/test.apk", "armeabi-v7a"),
          ),
      )

    val result = ModuleMetadataParser.assembleFulladleConfig(listOf(module), "armeabi-v7a")

    assertThat(result.debugApk).isEqualTo("/app-arm.apk")
  }

  @Test
  fun `assemble config with module-level overrides`() {
    val appModule =
      ModuleMetadata(
        modulePath = ":app",
        moduleType = "application",
        enabled = true,
        hasAndroidTestDir = true,
        maxTestShards = null,
        variant = null,
        debugApk = null,
        clientDetails = emptyMap(),
        environmentVariables = emptyMap(),
        variants =
          listOf(
            VariantMetadata("debug", "/app/debug.apk", "/app/test.apk", null),
          ),
      )
    val libModule =
      ModuleMetadata(
        modulePath = ":lib",
        moduleType = "library",
        enabled = true,
        hasAndroidTestDir = true,
        maxTestShards = 4,
        variant = null,
        debugApk = null,
        clientDetails = mapOf("test-type" to "PR"),
        environmentVariables = mapOf("clearPackageData" to "false"),
        variants =
          listOf(
            VariantMetadata("debug", null, "/lib/test.apk", null),
          ),
      )

    val result = ModuleMetadataParser.assembleFulladleConfig(listOf(appModule, libModule), null)

    assertThat(result.additionalTestApks).hasSize(1)
    val apkEntry = result.additionalTestApks[0]
    assertThat(apkEntry).contains("max-test-shards: 4")
    assertThat(apkEntry).contains("client-details:")
    assertThat(apkEntry).contains("test-type: PR")
    assertThat(apkEntry).contains("environment-variables:")
    assertThat(apkEntry).contains("clearPackageData: false")
  }

  @Test(expected = IllegalStateException::class)
  fun `library module without debugApk as root throws`() {
    val libModule =
      ModuleMetadata(
        modulePath = ":lib",
        moduleType = "library",
        enabled = true,
        hasAndroidTestDir = true,
        maxTestShards = null,
        variant = null,
        debugApk = null,
        clientDetails = emptyMap(),
        environmentVariables = emptyMap(),
        variants =
          listOf(
            VariantMetadata("debug", null, "/lib/test.apk", null),
          ),
      )

    ModuleMetadataParser.assembleFulladleConfig(listOf(libModule), null)
  }

  @Test
  fun `library module with debugApk as root succeeds`() {
    val libModule =
      ModuleMetadata(
        modulePath = ":lib",
        moduleType = "library",
        enabled = true,
        hasAndroidTestDir = true,
        maxTestShards = null,
        variant = null,
        debugApk = "dummy_app.apk",
        clientDetails = emptyMap(),
        environmentVariables = emptyMap(),
        variants =
          listOf(
            VariantMetadata("debug", null, "/lib/test.apk", null),
          ),
      )

    val result = ModuleMetadataParser.assembleFulladleConfig(listOf(libModule), null)

    assertThat(result.debugApk).isEqualTo("dummy_app.apk")
    assertThat(result.instrumentationApk).isEqualTo("/lib/test.apk")
    assertThat(result.additionalTestApks).isEmpty()
  }

  @Test
  fun `parse metadata files from disk`() {
    val tempDir = File(System.getProperty("java.io.tmpdir"), "fulladle-test-${System.nanoTime()}")
    tempDir.mkdirs()
    try {
      val file1 =
        File(tempDir, "module1.json").apply {
          writeText(
            """
            {
              "modulePath": ":app",
              "moduleType": "application",
              "enabled": true,
              "hasAndroidTestDir": true,
              "variants": [
                {"variantName":"debug","testApkPath":"/test.apk","appApkPath":"/app.apk"}
              ]
            }
            """.trimIndent(),
          )
        }

      val results = ModuleMetadataParser.parseModuleMetadata(setOf(file1))
      assertThat(results).hasSize(1)
      assertThat(results[0].modulePath).isEqualTo(":app")
    } finally {
      tempDir.deleteRecursively()
    }
  }
}
