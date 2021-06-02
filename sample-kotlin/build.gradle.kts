plugins {
    id ("com.android.application")
    kotlin("android")
    id ("com.osacky.fladle")
}

android {
    compileSdkVersion(29)
    defaultConfig {
        applicationId = "com.osacky.flank.gradle.sample.kotlin"
        minSdkVersion(23)
        targetSdkVersion(29)
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    testOptions.execution = "ANDROIDX_TEST_ORCHESTRATOR"
}

fladle {
    flankVersion.set("21.06.0")
    // Project Id is not needed if serviceAccountCredentials are set.
    projectId.set("flank-gradle")
    useOrchestrator.set(true)
    environmentVariables.set(project.provider { mapOf("clearPackageData" to "true") })
    maxTestShards.set(10)
    testTargets.set(listOf(
            "class com.osacky.flank.gradle.sample.ExampleInstrumentedTest#seeView"
    ))
    devices.set(listOf(
        mapOf("model" to "Pixel2", "version" to "26" ),
        mapOf("model" to "Nexus5", "version" to "23" )
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
            devices.set(listOf(mapOf("model" to "Nexus5", "version" to "28" ), mapOf("model" to "Nexus5", "version" to "28")))
            testTargets.set(listOf(
                "class com.sample.MyPerformanceTest"
            ))
        }
        create("regressionTests") {
            devices.set(listOf(mapOf("model" to "Nexus5LowRes", "version" to "28" )))
            testTargets.set(listOf(
                "class com.sample.MyRegressionTest"
            ))
        }
    }
    flakyTestAttempts.set(1)
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk7")
    implementation("androidx.appcompat:appcompat:1.2.0")
    implementation("androidx.navigation:navigation-fragment-ktx:2.3.0")
    implementation("androidx.constraintlayout:constraintlayout:1.1.3")
    testImplementation("junit:junit:4.13.1")
    androidTestImplementation("androidx.test.ext:junit:1.1.1")
    androidTestImplementation("androidx.test:rules:1.2.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.2.0")
}

