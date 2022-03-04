plugins {
  id("com.android.library")
  kotlin("android")
}

android {
  compileSdkVersion(29)
  defaultConfig {
    minSdkVersion(23)
    targetSdkVersion(29)
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

