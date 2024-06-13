plugins {
  id("com.android.library")
  kotlin("android")
}

android {
  compileSdk = 33
  namespace = "com.osacky.flank.gradle.sample.library"
  defaultConfig {
    minSdk = 23
    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }
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

