package com.osacky.flank.gradle

open class FlankGradleExtension {
  var flankVersion: String = "3.1.0"
  var projectId: String? = null
  var serviceAccountCredentials: String? = null
  var debugApk: String? = null
  var instrumentationApk: String? = null
  var useOrchestrator: Boolean = false
  var autoGoogleLogin: Boolean = false
  var devices: List<Device> = listOf(Device("NexusLowRes", 28))
}