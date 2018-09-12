package com.osacky.flank.gradle

open class FlankGradleExtension {
  var flankVersion: String = "3.0.0"
  var projectId: String? = null
  var serviceAccountCredentials: String? = null
  var debugApk: String? = null
  var instrumentationApk: String? = null
}