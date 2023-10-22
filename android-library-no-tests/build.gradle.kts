plugins {
  id("com.android.library")
  kotlin("android")
}

android {
  compileSdk = 29
  namespace = "com.osacky.flank.gradle.sample.library"
  defaultConfig {
    minSdk = 23
    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }
}

fulladleModuleConfig {
  debugApk.set(rootProject.file("dummy_app.apk").path)
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

