package com.osacky.flank.gradle.validation

import com.osacky.flank.gradle.FladleConfig
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import kotlin.reflect.full.memberProperties

internal fun FladleConfig.getPresentProperties() = this::class.memberProperties
  .filter {
    when (val prop = it.call(this)) {
      is Property<*> -> prop.isPresent
      is MapProperty<*, *> -> prop.isPresent && prop.get().isNotEmpty()
      is ListProperty<*> -> prop.isPresent && prop.get().isNotEmpty()
      else -> false
    }
  }
