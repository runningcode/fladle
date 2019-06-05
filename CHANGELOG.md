# Changelog

## 0.2.9

* Add support for multiple build targets. [PR](https://github.com/runningcode/fladle/pull/9). Thanks [winterDroid](https://github.com/winterDroid).
* Add support for testShards and repeatTests flank options. See README for configuration options.

## 0.2.10

* Add support for specifying Flank snapshot versions. See README for configuration options.

## 0.2.11

* Add minimum Gradle version check. (4.9 is required because we use the lazy task configuration API))

## 0.2.12

* Fix broken flankDoctor task.

## 0.3.0

* Add support for multiple test configurations.

## 0.3.1

* Maybe fix bug similar to: https://github.com/michel-kraemer/gradle-download-task/issues/58

## 0.3.2

* Actually fix gradle download task bug.

## 0.3.3

* Add support for setting smartFlankGcsPath
* Capitalize task names.

## 0.3.4

* Add support for setting results-history-name.
* Add support for selecting variant to test instead of apk path.

## 0.3.5

* Automatically configure the use of test orchestrator.

## 0.3.6

* Add support for environment variable clearPackageData. Thanks @anderssu !

## 0.3.7

* Add support for flakyTestAttempts.

## 0.3.8

* Fix broken flakyTestAttempts.

## 0.4.0

* Add support for all configuration options. Thanks [PR](https://github.com/runningcode/fladle/pull/26/) [winterDroid](https://github.com/winterDroid).

### Breaking Changes:
Previous users of `clearPackageData = true` will now need to use:
```
environmentVariables = [
  "clearPackageData": "true"
]
```

## 0.4.1

* Detect AndroidX test orchestrator
* Fix detection of debug and instrumentation apk paths.

## 0.5.0

* Use flank as a maven artifact.
* Group Fladle Tasks

## 0.5.1

* Test multipleconfig to actually write yml Thanks [PR](https://github.com/runningcode/fladle/pull/40/) [winterDroid](https://github.com/winterDroid).
* JavaExec uses classpath instead of jar directly. Thanks [PR](https://github.com/runningcode/fladle/pull/37/) [winterDroid](https://github.com/winterDroid).

## 0.5.2

* Improve error messages for missing arguments in the fladle extension.

## 0.6.0

* Default flank version 5.0.1
* Rename yaml output to match new flank version.
* Add support for shard time.

## 0.6.1
* Fix project id [spacing](https://github.com/runningcode/fladle/issues/49) Thanks [andersu](https://github.com/andersu) for reporting.

## 0.6.2
* [Fix shardTime config property not written to flank.yml](https://github.com/runningcode/fladle/pull/53) Thanks [nnoel-grubhub](https://github.com/nnoel-grubhub)

## 0.6.3
* Allow service credentials to be set [using environment variables](https://github.com/runningcode/fladle/pull/58). [Fixes #55](https://github.com/runningcode/fladle/issues/55)
* Fix not being able to [set flankVersion](https://github.com/runningcode/fladle/pull/57). [Fixes #56](https://github.com/runningcode/fladle/issues/56)
