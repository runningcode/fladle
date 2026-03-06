# Multi-module testing

Multi module testing can be done by manually specifying [additionalTestApks](/fladle/configuration/#additionaltestapks) or applying the Fulladle plugin to automacally gather all the additional test apks. See also [this article](https://proandroiddev.com/android-code-coverage-on-firebase-test-lab-part-3-ci-cd-integration-10f729996c29) for a full setup and instructions including integration with CI.

## Fulladle Plugin

There are two ways to set up Fulladle: with or without the settings plugin.

**The settings plugin (`com.osacky.fulladle.settings`) is the recommended approach** for projects using Gradle's [configuration cache](https://docs.gradle.org/current/userguide/configuration_cache.html). It avoids cross-project configuration by passing module metadata through Gradle's dependency management system. Without the settings plugin, Fulladle falls back to a legacy approach that uses `subprojects {}` — this still works but is not compatible with future Gradle best practices.

### Setup

1. *(Recommended)* Apply the settings plugin in `settings.gradle`:

    === "Groovy"
        ``` groovy
        plugins {
            id 'com.osacky.fulladle.settings' version '{{ fladle.current_release }}'
        }
        ```
    === "Kotlin"
        ``` kotlin
        plugins {
            id("com.osacky.fulladle.settings") version "{{ fladle.current_release }}"
        }
        ```

    The settings plugin automatically applies the module plugin to every subproject, so no per-module setup is needed.

2. Apply the Fulladle plugin at the root of the project.

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

3. Configure the Fladle extension.

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
        If using buildFlavors or testing against a non default variant, you will need to specify the variant you want to test in the fulladleModuleConfig block.

4. Run the tests.
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
Fulladle will pick Flank configurations from the `fladle` block in the root `build.gradle` file. You may want to override some of these configurations for certain modules, you can add the following block to any Android library module to override its configurations:


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
      variant = "vanillaDebug"
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
      variant.set("vanillaDebug")
    }
    ```
All of the above configurations are optional, Flank will default to the top-level configurations if you don't override anything here. For details about these configurations, refer to [configuration docs](./configuration.md).

#### Disabling a module
You may want to exclude a library module from testing when using Fulladle. You can do so by setting the `enabled` configuration in the module's `fulladleModuleConfig` block like so:


=== "Groovy"
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


### Overriding root-level config
Fulladle does not provide the ability to control which module ends up as the root-level module or as an additional module. Either one of app modules or library modules can become a root-level module. If a library module ends up as a root-level module, it needs to specify a `debugApk` in its `fladle` or `fulladleModuleConfig` block.

The root-level configuration (e.g. `maxTestShards`) can also be overridden in the `fulladleModuleConfig` block of whatever module gets picked as the root module.

## Troubleshooting
Fulladle might still have some rough edges, but we'd love feedback. Please join us in the [Firebase Community Slack](https://firebase.community/) with any feedback you may have.
You can also file [Fladle Github issues](https://github.com/runningcode/fladle/issues).

When filing a bug report, please include the Flank version number, the Fladle version number and the output of the following:

`./gradlew printYml`

`./gradlew runFlank -PdumpShards`


