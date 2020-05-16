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

2. Configure the Fladle extension.

    ===! "Groovy"
        ``` groovy
        fladle {
            serviceAccountCredentials = project.layout.projectDirectory.file("flank-gradle-service-account.json")
        }
        ```
    === "Kotlin"
        ``` kotlin
        fladle {
            serviceAccountCredentials.set(project.layout.projectDirectory.file("flank-gradle-service-account.json"))
        }
        ```
        
    !!! Warning
        If using buildFlavors or testing against a non default variant, [variant must also configured](/fladle/configuration#variant)

3. Run the Flank Gradle task.
    ``` bash
    ./gradlew runFlank
    ```

