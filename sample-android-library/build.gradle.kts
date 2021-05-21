plugins {
  id("com.android.library")
  kotlin("android")
}

fulladleModuleConfig {
  enabled.set(true)
  clientDetails.set(mapOf(
    "test-type" to "PR",
    "build-number" to "132",
    "module-name" to project.path,
  ))
  maxTestShard.set(3)
  environmentVariables.set(mapOf(
    "clearPackageData" to "true"
  ))
}

android {
  compileSdkVersion(29)
  defaultConfig {
    minSdkVersion(23)
    targetSdkVersion(28)
    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }
  testOptions.execution = "ANDROIDX_TEST_ORCHESTRATOR"
}

dependencies {
  implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk7")
  implementation("androidx.appcompat:appcompat:1.1.0")
  implementation("androidx.navigation:navigation-fragment-ktx:2.3.0")
  implementation("androidx.constraintlayout:constraintlayout:1.1.3")
  testImplementation("junit:junit:4.13")
  androidTestImplementation("androidx.test.ext:junit:1.1.1")
  androidTestImplementation("androidx.test:rules:1.2.0")
  androidTestImplementation("androidx.test.espresso:espresso-core:3.2.0")
}

