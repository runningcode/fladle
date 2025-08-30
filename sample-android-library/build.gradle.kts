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
  maxTestShards.set(13)
  environmentVariables.set(mapOf(
    "clearPackageData" to "true"
  ))
}

android {
  namespace = "com.osacky.flank.gradle.sample"
  compileSdk = 33
  defaultConfig {
    minSdk = 23
    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }
  testOptions.execution = "ANDROIDX_TEST_ORCHESTRATOR"
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }
}

java {
  toolchain {
    languageVersion = JavaLanguageVersion.of(11)
  }
}

dependencies {
  implementation(libs.appcompat)
  implementation(libs.navigation.fragment.ktx)
  implementation(libs.constraintlayout)
  testImplementation(libs.junit)
  androidTestImplementation(libs.androidx.test.rules)
  androidTestImplementation(libs.androidx.test.ext.junit)
  androidTestImplementation(libs.espresso.core)
}

