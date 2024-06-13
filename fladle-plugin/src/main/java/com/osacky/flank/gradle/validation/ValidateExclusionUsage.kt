package com.osacky.flank.gradle.validation

import com.osacky.flank.gradle.FladleConfig

fun checkForExclusionUsage(config: FladleConfig) {
  val usersProperties =
    config
      .getPresentProperties()
      .map { it.name }

  exclusions.forEach {
    if (usersProperties.contains(it.first) && usersProperties.contains(it.second)) {
      throw IllegalStateException("Options ${it.first} and ${it.second} cannot be used together. Choose one of them.")
    }
  }
}

private val exclusions =
  listOf(
    "testShards" to "maxTestShards",
    "testShards" to "numUniformShards",
    "maxTestShards" to "numUniformShards",
  )
