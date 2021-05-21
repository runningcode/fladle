package com.osacky.flank.gradle

import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.mapProperty
import org.gradle.kotlin.dsl.property
import javax.inject.Inject

open class FulladleModuleExtension @Inject constructor(objects: ObjectFactory) {

  /**
   * When set to false, Fulladle will not automatically add this module to additionalTestApks.
   *
   * Default: true
   */
  val enabled: Property<Boolean> = objects.property<Boolean>().convention(true)

  /**
   * The maximum number of shards to be used for this specific test apk.
   */
  val maxTestShards: Property<Int> = objects.property<Int>().convention(null as Int?)

  /**
   * A key-value map of additional details to attach to the test matrix results file.
   * Arbitrary key-value pairs may be attached to a test matrix to provide additional context about the tests being run.
   * When consuming the test results, such as in Cloud Functions or a CI system,
   * these details can add additional context such as a link to the corresponding pull request.
   */
  val clientDetails: MapProperty<String, String> = objects.mapProperty()

  /**
   * The environment variables are mirrored as extra options to the am instrument -e KEY1 VALUE1 … command and
   * passed to your test runner (typically AndroidJUnitRunner)
   */
  val environmentVariables: MapProperty<String, String> = objects.mapProperty()

  /**
   * the app under test
   */
  val debugApk: Property<String> = objects.property<String>().convention(null as String?)
}
