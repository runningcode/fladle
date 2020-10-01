# Quick Start

Using Fladle takes 3 steps:

1. Apply the Fladle plugin. Follow instructions [here](https://plugins.gradle.org/plugin/com.osacky.fladle)

    Root `build.gradle`

    === "Groovy"
        ``` groovy
        buildscript {
          dependencies {
            classpath "com.osacky.flank.gradle:fladle:{{ fladle.current_release }}"
          }
        }
        ```
    === "Kotlin"
        ``` kotlin
        buildscript {
          dependencies {
            classpath("com.osacky.flank.gradle:fladle:{{ fladle.current_release }}")
          }
        }
        ```

    Application module `build.gradle`

    ===! "Groovy"
        ``` groovy
        apply plugin: "com.android.application"
        apply plugin: "com.osacky.fladle"
        ```

    === "Kotlin"
        ``` kotlin
        plugins {
            id ("com.android.application")
            id ("com.osacky.fladle")
        }
        ```

2. Configure [Authentication using these steps.](../authentication)

    !!! Warning
        If using buildFlavors or testing against a non default variant, [variant must also configured](/fladle/configuration#variant)

3. Run your tests!

    First assemble your debug apk and test apk.
    ``` bash
    ./gradlew :app:assembleDebug :app:assembleDebugAndroidTest
    ```

    !!! note
        When using flavors, make sure to assemble your buildVariants.

        `./gradlew :app:assembleFreeDebug :app:assembleFreeDebugAndroidTest`

    Run Flank!
    ``` bash
    ./gradlew runFlank
    ```

