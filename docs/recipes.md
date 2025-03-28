# Recipes

Here are some recipes to use to achieve various goals in flank. For additional recipes or suggestions, please file an
issue on Github.

## Smartly shard tests in 120 second shards across a maximum of 50 shards.

This recipe will keep track of test durations automatically on firebase test lab and try to split up test runs in to 120 second shards up to maximum of 50 shards.

=== "Groovy"
    ``` groovy
    fladle {
        maxTestShards = 50
        shardTime = 120
        smartFlankGcsPath = "gs://fladle-results/smart-flank/JUnitReport.xml"
    }
    ```
=== "Kotlin"
    ``` kotlin
    fladle {
        maxTestShards.set(50)
        shardTime.set(120)
        smartFlankGcsPath.set("gs://fladle-results/smart-flank/JUnitReport.xml")
    }
    ```


## Run different tests on different devices with different Gradle tasks.

`./gradlew runFlankPerfTests` will execute the performance tests against a MediumPhone.arm
`./gradlew runFlankRegresssionTests` will execute the regressions tests against a SmallPhone.arm

=== "Groovy"
    ``` groovy
    fladle {
        configs {
            perfTests {
                devices.set([
                    ["model" : "MediumPhone.arm", "version" : "28"], 
                    ["model" : "MediumPhone.arm", "version" : "28"]
                ])
                testTargets.set([
                        "class com.sample.MyPerformanceTest"
                ])
            }
            regressionTests {
                devices.set([
                    [ "model" : "SmallPhone.arm", "version" : "28"]
                ])
                testTargets.set([
                    "class com.sample.MyRegressionTest"
                ])
            }
        }
    }
    ```
=== "Kotlin"
    ``` kotlin
    fladle {
        configs {
            create("perfTests") {
                devices.set(listOf(
                    mapOf("model" to "MediumPhone.arm", "version" to "28" ), 
                    mapOf("model" to "MediumPhone.arm", "version" to "28")
                ))
                testTargets.set(listOf(
                    "class com.sample.MyPerformanceTest"
                ))
            }
            create("regressionTests") {
                devices.set(listOf(
                    mapOf("model" to "SmallPhone.arm", "version" to "28" )
                ))
                testTargets.set(listOf(
                    "class com.sample.MyRegressionTest"
                ))
            }
        }
    }
    ```


## Always use the latest version of Flank


Use Gradle's [dynamic version syntax] to declare a dynamic version.


!!! warning
    Dynamic versions lead to non-reproducible builds since Gradle will check for new versions periodically based on [how long versions are cached].

=== "Groovy"
    ``` groovy
    flankVersion = "23.+"
    ```
=== "Kotlin"
    ``` kotlin
    flankVersion.set("23.+")
    ```


[dynamic version syntax]: https://docs.gradle.org/current/userguide/dynamic_versions.html#sub:declaring_dependency_with_dynamic_version
[how long versions are cached]: https://docs.gradle.org/current/userguide/dynamic_versions.html#sec:controlling_dependency_caching_programmatically