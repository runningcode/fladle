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
  fun writeNoTestShards() {
    val extension = FlankGradleExtension().apply {
    }

    assertEquals("", yamlWriter.writeFlankProperties(extension))
  }

  @Test
  fun writeTestShardOption() {
    val extension = FlankGradleExtension().apply {
      testShards = 5
    }

    assertEquals("flank:\n" +
        "  testShards: 5\n", yamlWriter.writeFlankProperties(extension))
  }

  @Test
  fun writeNoTestRepeats() {
    val extension = FlankGradleExtension().apply {
      repeatTests = null
    }

    assertEquals("", yamlWriter.writeFlankProperties(extension))
  }

  @Test
  fun writeTestRepeats() {
    val extension = FlankGradleExtension().apply {
      repeatTests = 5
    }

    assertEquals("flank:\n" +
        "  repeatTests: 5\n", yamlWriter.writeFlankProperties(extension))
  }

  @Test
  fun writeTestShardAndRepeatOption() {
    val extension = FlankGradleExtension().apply {
      testShards = 5
      repeatTests = 2
    }

    assertEquals("flank:\n" +
        "  testShards: 5\n" +
        "  repeatTests: 2\n", yamlWriter.writeFlankProperties(extension))
  }

  @Test
  fun writeNoTestTargets() {
    val extension = FlankGradleExtension().apply {
      testTargets = listOf()
    }

    assertEquals("", yamlWriter.writeAdditionalProperties(extension))
  }

  @Test
  fun writeSingleTestTargets() {
    val extension = FlankGradleExtension().apply {
      testTargets = listOf("class com.example.Foo#testThing")
    }

    assertEquals("  test-targets:\n" +
        "  - class com.example.Foo#testThing\n", yamlWriter.writeAdditionalProperties(extension))
  }

  @Test
  fun writeMultipleTestTargets() {
    val extension = FlankGradleExtension().apply {
      testTargets = listOf("class com.example.Foo#testThing", "class com.example.Foo#testThing2")
    }

    assertEquals("  test-targets:\n" +
        "  - class com.example.Foo#testThing\n" +
        "  - class com.example.Foo#testThing2\n",
      yamlWriter.writeAdditionalProperties(extension))
  }
}