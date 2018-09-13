package com.osacky.flank.gradle

import org.junit.Assert.assertEquals
import org.junit.Test

class YamlWriterTest {

  internal val yamlWriter = YamlWriter()

  @Test
  fun testWriteSingleDevice() {
    val devices = listOf(
        Device("NexusLowRes", 28)
    )
    val deviceString = yamlWriter.createDeviceString(devices)
    val result = """
      |  device:
      |  - model: NexusLowRes
      |    version: 28
      |
    """.trimMargin()
    assertEquals(result, deviceString)
  }

  @Test
  fun testWriteTwoDevices() {
    val devices = listOf(
        Device("NexusLowRes", 28),
        Device("Nexus5", 23)
    )
    val deviceString = yamlWriter.createDeviceString(devices)
    val result = """
      |  device:
      |  - model: NexusLowRes
      |    version: 28
      |  - model: Nexus5
      |    version: 23
      |
    """.trimMargin()
    assertEquals(result, deviceString)
  }

  @Test
  fun testWriteTwoCustomDevices() {
    val devices = listOf(
        Device("NexusLowRes", 23, orientation = "portrait"),
        Device("Nexus5", orientation = "landscape", version = 28)
    )
    val deviceString = yamlWriter.createDeviceString(devices)
    val result = """
      |  device:
      |  - model: NexusLowRes
      |    version: 23
      |    orientation: portrait
      |  - model: Nexus5
      |    version: 28
      |    orientation: landscape
      |
    """.trimMargin()
    assertEquals(result, deviceString)
  }
}