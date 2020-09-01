# Changelog

## Unreleased

## 0.12.1
* Don't override debug or instrumentation apk if already specified. Fixes [#172](https://github.com/runningcode/fladle/issues/172).

## 0.12.0
* Don't override property values in individual configurations. Fixes [#158](https://github.com/runningcode/fladle/issues/158).
* Update [Flank to 20.08.3](https://github.com/Flank/flank/releases/tag/v20.08.3).
* Use Github actions instead of CircleCI
* Write Yaml file to task specific output directory and add up-to-date checks. [#159](https://github.com/runningcode/fladle/pull/159) [Fixes #147](https://github.com/runningcode/fladle/issues/147) Thanks [CristianGM](https://github.com/CristianGM)
* Add support for `full-junit-result` and `legacy-junit-result`. [#170](https://github.com/runningcode/fladle/pull/170) Fixes [#157](https://github.com/runningcode/fladle/issues/157). Thanks [MatthewTPage](https://github.com/MatthewTPage)

## 0.11.0
* Update [Flank to 20.07.0](https://github.com/Flank/flank/releases/tag/v20.07.0).
* Only add `additional-test-apks` for modules that have tests. [PR](https://github.com/runningcode/fladle/pull/150)
* Experimental configuration caching support. [PR1](https://github.com/runningcode/fladle/pull/153)[PR2](https://github.com/runningcode/fladle/pull/154)

## 0.10.2
* Update [Flank to 20.06.2](https://github.com/Flank/flank/releases/tag/v20.06.2).
* Fix duplicated status messages in console. [PR](https://github.com/runningcode/fladle/pull/142) Thanks [francescocervone](https://github.com/francescocervone)

!!! Warning  "Breaking API change"
    Add time units for timeout. `timeoutMin` has been renamed to `testTimeout`. [PR](https://github.com/runningcode/fladle/pull/137) Thanks [pawelpasterz](https://github.com/pawelpasterz)

## 0.10.1
* [Fix] Allow for specifying roboScript without specifying instrumentationApk. Fixes [#128](https://github.com/runningcode/fladle/issues/128).

## 0.10.0

* Allow for debugging using [--dump-shards](/fladle/faq/#debugging)
* Fix naming for variant discovery of apk and instrumentation apk. Instead of chocolate-debug, variant must now be set as chocolateDebug.
* Update [Flank to 20.05.2](https://github.com/Flank/flank/releases/tag/v20.05.2).
* [Fulladle Preview](/fladle/multi-module-testing)

!!! Warning "Breaking API Change"
    [additionalTestApks](/fladle/configuration/#additionaltestapks) now uses ListProperty instead of the previous Map. This is to allow for lazy configuration of the provided files.
!!! Warning
    Minimum supported Gradle version is now 5.4.

## 0.9.4
* Update [Flank to 20.05.1](https://github.com/Flank/flank/releases/tag/v20.05.0). Huge new release!
* Add support for new flank flags. Thanks [pawelpasterz](https://github.com/pawelpasterz) [PR](https://github.com/runningcode/fladle/pull/88)
* Use compileOnly for AGP version.

## 0.9.2

!!! Warning  "Breaking API change"
    debugApk and instrumentationApk now use Lazy Property API to avoid resolving at configuration time.

## 0.9.1

* Bugfix: ability to set flank version. [PR](https://github.com/runningcode/fladle/pull/97)

!!! Warning "Breaking API Change"
    serviceAccountCredentials now uses [Lazy  Property API](https://docs.gradle.org/current/userguide/lazy_configuration.html#working_with_files_in_lazy_properties). See [Configuration](/configuration#serviceAccountCredentials) for details on how to set it. [PR](https://github.com/runningcode/fladle/pull/97)
!!! Warning
    Minimum required Gradle Version is now 5.1.
!!! Warning
    Dropped support for Flank 7.X and lower.

## 0.9.0
* Do not add flank maven repo. [PR](https://github.com/runningcode/fladle/pull/94)
* Allow specifying custom flank coordinates. [PR](https://github.com/runningcode/fladle/pull/94)
* Change ordering and use file provider. [PR](https://github.com/runningcode/fladle/pull/95)

## 0.8.1
* Add support for `additionalTestApks`. [PR](https://github.com/runningcode/fladle/pull/83) Thanks [japplin](https://github.com/japplin).
* Add support for `resultsDir`. [PR](https://github.com/runningcode/fladle/pull/80)

## 0.8.0
* BREAKING: devices now takes a `List<Map<String, String>>` instead of a `List<Device>`. See the [#README.md] for an example. [PR](https://github.com/runningcode/fladle/pull/76) Thanks [zlippard](https://github.com/zlippard).
* Add support for `keep-file-path`. [PR](https://github.com/runningcode/fladle/pull/77) Thanks [tahirhajizada](https://github.com/tahirhajizada).

## 0.7.0
* Add support for Flank 8 and bump version. [PR](https://github.com/runningcode/fladle/pull/75) Thanks [francescocervone](https://github.com/francescocervone)

## 0.6.7
* Allow using wildcards in debugApk or instrumentationApk path by not checking that file exists. [PR](https://github.com/runningcode/fladle/pull/72)

## 0.6.6
* Bump flank version to 7.0.0
* Publish to mavenCentral()

## 0.6.5
* Add support for results-bucket gcloud config option. [PR](https://github.com/runningcode/fladle/pull/62) Thanks [c-moss](https://github.com/c-moss)
* Default flank version 6.2.3
* Lower build logging level

## 0.6.4
* Allow setting android version number as string to allow for preview versions. [PR](https://github.com/runningcode/fladle/pull/590)Thanks [JeroenMols](https://github.com/JeroenMols).
## 0.6.3
* Allow service credentials to be set [using environment variables](https://github.com/runningcode/fladle/pull/58). [Fixes #55](https://github.com/runningcode/fladle/issues/55)
* Fix not being able to [set flankVersion](https://github.com/runningcode/fladle/pull/57). [Fixes #56](https://github.com/runningcode/fladle/issues/56)

## 0.6.2
* [Fix shardTime config property not written to flank.yml](https://github.com/runningcode/fladle/pull/53) Thanks [nnoel-grubhub](https://github.com/nnoel-grubhub)

## 0.6.1
* Fix project id [spacing](https://github.com/runningcode/fladle/issues/49) Thanks [andersu](https://github.com/andersu) for reporting.

## 0.6.0
* Default flank version 5.0.1
* Rename yaml output to match new flank version.
* Add support for shard time.

## 0.5.2
* Improve error messages for missing arguments in the fladle extension.

## 0.5.1
* Test multipleconfig to actually write yml Thanks [PR](https://github.com/runningcode/fladle/pull/40/) [winterDroid](https://github.com/winterDroid).
* JavaExec uses classpath instead of jar directly. Thanks [PR](https://github.com/runningcode/fladle/pull/37/) [winterDroid](https://github.com/winterDroid).

## 0.5.0
* Use flank as a maven artifact.
* Group Fladle Tasks

## 0.4.1
* Detect AndroidX test orchestrator
* Fix detection of debug and instrumentation apk paths.

## 0.4.0
* Add support for all configuration options. Thanks [PR](https://github.com/runningcode/fladle/pull/26/) [winterDroid](https://github.com/winterDroid).

### Breaking Changes:
Previous users of `clearPackageData = true` will now need to use:
```
environmentVariables = [
  "clearPackageData": "true"
]
```

## 0.3.8
* Fix broken flakyTestAttempts.

## 0.3.7
* Add support for flakyTestAttempts.

## 0.3.6
* Add support for environment variable clearPackageData. Thanks @anderssu !

## 0.3.5
* Automatically configure the use of test orchestrator.

## 0.3.4
* Add support for setting results-history-name.
* Add support for selecting variant to test instead of apk path.

## 0.3.3
* Add support for setting smartFlankGcsPath
* Capitalize task names.

## 0.3.2
* Actually fix gradle download task bug.

## 0.3.1
* Maybe fix bug similar to: https://github.com/michel-kraemer/gradle-download-task/issues/58

## 0.3.0
* Add support for multiple test configurations.

## 0.2.12
* Fix broken flankDoctor task.

## 0.2.11
* Add minimum Gradle version check. (4.9 is required because we use the lazy task configuration API))

## 0.2.10
* Add support for specifying Flank snapshot versions. See README for configuration options.

## 0.2.9
* Add support for multiple build targets. [PR](https://github.com/runningcode/fladle/pull/9). Thanks [winterDroid](https://github.com/winterDroid).
* Add support for testShards and repeatTests flank options. See README for configuration options.

