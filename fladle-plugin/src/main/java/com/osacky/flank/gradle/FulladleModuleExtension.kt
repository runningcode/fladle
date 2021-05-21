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
  val maxTestShard: Property<Int> = objects.property<Int>().convention(null as Int?)
  val clientDetails: MapProperty<String, String> = objects.mapProperty()
  val environmentVariables: MapProperty<String, String> = objects.mapProperty()
  val debugApk: Property<String> = objects.property<String>().convention(null as String?)
}
