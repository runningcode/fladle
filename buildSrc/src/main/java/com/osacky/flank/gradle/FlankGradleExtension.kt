package com.osacky.flank.gradle

open class FlankGradleExtension {
  var flankVersion: String = "3.1.1"
  // Project id is automatically discovered by default. Use this to override the project id.
  var projectId: String? = null
  var serviceAccountCredentials: String? = null
  var debugApk: String? = null
  var instrumentationApk: String? = null
  var useOrchestrator: Boolean = false
  var autoGoogleLogin: Boolean = false
  var devices: List<Device> = listOf(Device("NexusLowRes", 28))

  // https://cloud.google.com/sdk/gcloud/reference/firebase/test/android/run
  var testTargets: List<String> = emptyList()
}