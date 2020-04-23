package com.osacky.flank.gradle

import org.gradle.api.Project
import org.gradle.api.file.Directory
import org.gradle.api.provider.Provider

internal val Project.fladleDir: Provider<Directory>
  get() = layout.buildDirectory.dir("fladle")
