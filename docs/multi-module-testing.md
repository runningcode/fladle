# Multi-module testing

Multi module testing can be done by manually specifying [additionalTestApks](/fladle/configuration/#additionaltestapks) or applying the Fulladle plugin to automacally gather all the additional test apks.

## Fulladle Plugin

!!! Warning
    Fulladle is still under development and is not guaranteed to work and may change at any moment.

1. Apply the Fulladle plugin at the root of the project.

    === "Groovy"
        ``` groovy
        plugins {
            id 'com.osacky.fulladle' version '{{ fladle.current_release }}'
        }
        ```
    === "Kotlin"
        ``` kotlin
        plugins {
            id 'com.osacky.fulladle' version '{{ fladle.current_release }}'
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
        If using buildFlavors or testing against a non default variant, Fulladle might not test the variant you are expecting.

3. Run the tests.   
    First assemble all your debug apks and test apks.
    ``` bash
    ./gradlew assembleDebug assembleDebugAndroidTest
    ```
   
    !!! note
        When using flavors, make sure to assemble your buildVariants as well.
        
        `./gradlew :app:assembleFreeDebug :app:assembleFreeDebugAndroidTest` 

    Run Flank!
    ``` bash
    ./gradlew runFlank
    ```


## Troubleshooting
Fulladle isn't ready yet, but we'd love feedback. Please join us in the [Firebase Community Slack](https://firebase.community/) with any feedback you may have.
You can also file [Fladle Github issues](https://github.com/runningcode/fladle/issues).

When filing a bug report, please include the Flank version number, the Fladle version number and the output of the following:

`./gradlew printYml`

`./gradlew runFlank -PdumpShards`


