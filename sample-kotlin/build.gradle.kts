plugins {
    id ("com.android.application")
    kotlin("android")
    id ("com.osacky.fladle")
}

android {
  namespace = "com.osacky.flank.gradle.sample.kotlin"
  compileSdk = 33
  defaultConfig {
      applicationId = "com.osacky.flank.gradle.sample.kotlin"
      minSdk = 23
      versionCode = 1
      versionName = "1.0"
      testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }
  testOptions.execution = "ANDROIDX_TEST_ORCHESTRATOR"
}

java {
  toolchain {
    languageVersion = JavaLanguageVersion.of(11)
  }
}

fladle {
    flankVersion.set("23.10.1")
    // Project Id is not needed if serviceAccountCredentials are set.
    projectId.set("flank-gradle")
    useOrchestrator.set(true)
    environmentVariables.set(project.provider { mapOf("clearPackageData" to "true") })
    maxTestShards.set(10)
    testTargets.set(listOf(
            "class com.osacky.flank.gradle.sample.ExampleInstrumentedTest#seeView"
    ))
    devices.set(listOf(
        mapOf("model" to "SmallPhone.arm", "version" to "26" ),
        mapOf("model" to "MediumPhone.arm", "version" to "33" )
    ))
    smartFlankGcsPath.set("gs://test-lab-yr9w6qsdvy45q-iurp80dm95h8a/flank/test_app_android.xml")
    configs {
        create("oranges") {
            useOrchestrator.set(false)
            testTargets.set(project.provider { listOf(
                    "class com.osacky.flank.gradle.sample.ExampleInstrumentedTest#runAndFail"
            ) })
            flakyTestAttempts.set(3)
        }
        create("additionalTests") {
            additionalTestApks.value(project.provider { listOf(
                "app: ../main/app/build/output/apk/debug/app.apk",
                "test: ../main/app/build/output/apk/androidTest/debug/app-test.apk",
                "app: ../sample/app/build/output/apk/debug/sample-app.apk",
                "test: ../sample/app/build/output/apk/androidTest/debug/sample-app-test.apk",
                "test: ../feature/room/build/output/apk/androidTest/debug/feature-room-test.apk",
                "test: ../library/databases/build/output/apk/androidTest/debug/sample-databases-test.apk"
            )})
        }
        create("perfTests") {
            devices.set(listOf(mapOf("model" to "SmallPhone.arm", "version" to "28" ), mapOf("model" to "MediumPhone.arm", "version" to "33")))
            testTargets.set(listOf(
                "class com.sample.MyPerformanceTest"
            ))
        }
        create("regressionTests") {
            devices.set(listOf(mapOf("model" to "SmallPhone.arm", "version" to "28" )))
            testTargets.set(listOf(
                "class com.sample.MyRegressionTest"
            ))
        }
    }
    flakyTestAttempts.set(1)
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

