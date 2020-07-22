package com.osacky.flank.gradle

import org.gradle.api.file.Directory
import org.gradle.api.file.ProjectLayout
import org.gradle.api.provider.Provider

internal val ProjectLayout.fladleDir: Provider<Directory>
  get() = buildDirectory.dir("fladle")
