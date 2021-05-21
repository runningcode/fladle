# Multi-module testing

Multi module testing can be done by manually specifying [additionalTestApks](/fladle/configuration/#additionaltestapks) or applying the Fulladle plugin to automacally gather all the additional test apks.

## Fulladle Plugin

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
            id("com.osacky.fulladle") version "{{ fladle.current_release }}"
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

### Overriding configurations in modules
Fulladle will pick Flank configurations from the `fladle` block in the root level `build.gradle` file. You may want to override some of these configurations for certain modules, you can add the following block to any Android library module to override its configurations:
    ===! "Groovy"
        ``` groovy
       fulladleModuleConfig {
          clientDetails = [
              "test-type": "PR",
              "build-number": "132"
          ]
          maxTestShards = 3
          environmentVariables = [
              "clearPackageData": "true"
          ]
          debugApk = "app.apk"
        }
        ```
    === "Kotlin"
        ``` kotlin
        fulladleModuleConfig {
          clientDetails.set(mapOf(
            "test-type" to "PR",
            "build-number" to "132",
          ))
          maxTestShards.set(3)
          environmentVariables.set(mapOf(
            "clearPackageData" to "true"
          ))
          debugApk.set("app.apk")
        }
        ```
All of the above configurations are optional, Flank will default to the top-level configurations if you don't override anything here. For details about what these configurations, refer to [configuration docs](./configuration.md).

#### Disabling a module
You may want to exclude a library module from testing when using Fulladle. You can do so by setting the `enabled` configuration in the module's `fulladleModuleConfig` block like so:
    ===! "Groovy"
        ``` groovy
       fulladleModuleConfig {
          enabled = false
        }
        ```
    === "Kotlin"
        ``` kotlin
        fulladleModuleConfig {
            enabled.set(false)
        }
        ```
## Troubleshooting
Fulladle might still have some rough edges, but we'd love feedback. Please join us in the [Firebase Community Slack](https://firebase.community/) with any feedback you may have.
You can also file [Fladle Github issues](https://github.com/runningcode/fladle/issues).

When filing a bug report, please include the Flank version number, the Fladle version number and the output of the following:

`./gradlew printYml`

`./gradlew runFlank -PdumpShards`


