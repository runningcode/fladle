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
    flavorDimensions("flavor")

    productFlavors {
        create("chocolate") {
            dimension = "flavor"
        }
        create("vanilla") {
            dimension = "flavor"
        }
    }

}

androidComponents {
    beforeVariants(selector().withName("vanilla")) { variantBuilder ->
        variantBuilder.enabled = false
    }
}

fladle {
    flankVersion.set("20.08.4")
    variant.set("chocolateDebug")
    debugApk.set(project.provider { "${buildDir.toString()}/outputs/apk/chocolate/debug/*.apk" })
    serviceAccountCredentials.set(project.layout.projectDirectory.file("flank-gradle-5cf02dc90531.json"))
    // Project Id is not needed if serviceAccountCredentials are set.
//    projectId("flank-gradle")
    useOrchestrator.set(true)
    environmentVariables.set(project.provider { mapOf("clearPackageData" to "true") })
    testTargets.set(project.provider { listOf(
            "class com.osacky.flank.gradle.sample.ExampleInstrumentedTest#seeView"
    ) })
    devices.set(project.provider { listOf(
        mapOf("model" to "Pixel2", "version" to "26" ),
        mapOf("model" to "Nexus5", "version" to "23" )
    ) })
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
    }
    flakyTestAttempts.set(1)
    dependOnAssemble.set(true)
}

fulladleModuleConfig {
    maxTestShards.set(24)
    clientDetails.set(
        mapOf(
            "key1" to "val1"
        )
    )
    environmentVariables.set(
        mapOf(
            "clearPackageData" to "true"
        )
    )
    debugApk.set("${rootProject.file("dummy_app.apk")}")
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

