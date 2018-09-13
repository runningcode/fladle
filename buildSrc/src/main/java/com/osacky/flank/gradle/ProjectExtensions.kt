package com.osacky.flank.gradle

import org.gradle.api.Project

internal val Project.fladleDir: String
  get() = "$buildDir/fladle"
