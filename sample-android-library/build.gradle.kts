plugins {
  id("com.android.library")
  kotlin("android")
}

android {
  compileSdkVersion(28)
  defaultConfig {
    minSdkVersion(23)
    targetSdkVersion(28)
    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }
  testOptions {
    execution = "ANDROIDX_TEST_ORCHESTRATOR"
  }
}

dependencies {
  implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk7")
  implementation("androidx.appcompat:appcompat:1.1.0")
  implementation("android.arch.navigation:navigation-fragment-ktx:1.0.0")
  implementation("androidx.constraintlayout:constraintlayout:1.1.3")
  testImplementation("junit:junit:4.13")
  androidTestImplementation("androidx.test:runner:1.2.0")
  androidTestImplementation("androidx.test:rules:1.2.0")
  androidTestImplementation("androidx.test.espresso:espresso-core:3.2.0")
}

