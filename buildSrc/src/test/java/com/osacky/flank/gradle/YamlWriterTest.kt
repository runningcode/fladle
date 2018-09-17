package com.osacky.flank.gradle

import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test

class YamlWriterTest {

  internal val yamlWriter = YamlWriter()

  @Test
  fun testWriteSingleDevice() {
    val devices = listOf(
        Device("NexusLowRes", 28)
    )
    val deviceString = yamlWriter.createDeviceString(devices)
    val expected = """
      |  device:
      |  - model: NexusLowRes
      |    version: 28
      |
    """.trimMargin()
    assertEquals(expected, deviceString)
  }

  @Test
  fun testWriteTwoDevices() {
    val devices = listOf(
        Device("NexusLowRes", 28),
        Device("Nexus5", 23)
    )
    val deviceString = yamlWriter.createDeviceString(devices)
    val expected = """
      |  device:
      |  - model: NexusLowRes
      |    version: 28
      |  - model: Nexus5
      |    version: 23
      |
    """.trimMargin()
    assertEquals(expected, deviceString)
  }

  @Test
  fun testWriteTwoCustomDevices() {
    val devices = listOf(
        Device("NexusLowRes", 23, orientation = "portrait"),
        Device("Nexus5", orientation = "landscape", version = 28)
    )
    val deviceString = yamlWriter.createDeviceString(devices)
    val expected = """
      |  device:
      |  - model: NexusLowRes
      |    version: 23
      |    orientation: portrait
      |  - model: Nexus5
      |    version: 28
      |    orientation: landscape
      |
    """.trimMargin()
    assertEquals(expected, deviceString)
  }

  @Test
  fun verifyDebugApkThrowsError() {
    val extension = FlankGradleExtension()
    try {
      yamlWriter.createConfigProps(extension)
      fail()
    } catch (expected: IllegalStateException) {
      assertEquals("debugApk cannot be null", expected.message)
    }
  }

  @Test
  fun verifyInstrumentationApkThrowsError() {
    val extension = FlankGradleExtension().apply {
      debugApk = "path"
    }
    try {
      yamlWriter.createConfigProps(extension)
      fail()
    } catch (expected: IllegalStateException) {
      assertEquals("instrumentationApk cannot be null", expected.message)
    }
  }

  @Test
  fun writeTestTargets() {
    val extension = FlankGradleExtension().apply {
      testTargets = "class com.example.Foo#testThing"
    }

    assertEquals("  test-targets:\n" +
        "  - class com.example.Foo#testThing", yamlWriter.writeAdditionalProperties(extension))
  }
}