package com.osacky.flank.gradle

interface FladleConfig {
  var flankVersion: String
  // Project id is automatically discovered by default. Use this to override the project id.
  var projectId: String?
  var serviceAccountCredentials: String?
  var useOrchestrator: Boolean
  var autoGoogleLogin: Boolean
  var devices: List<Device>

  // https://cloud.google.com/sdk/gcloud/reference/firebase/test/android/run
  var testTargets: List<String>

  var testShards: Int?
  var repeatTests: Int?

  var smartFlankGcsPath: String?
}