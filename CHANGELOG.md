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
