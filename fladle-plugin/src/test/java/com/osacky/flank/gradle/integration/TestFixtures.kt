package com.osacky.flank.gradle.integration

import org.gradle.testkit.runner.GradleRunner
import org.junit.rules.TemporaryFolder
import java.io.File

fun TemporaryFolder.setupFixture(fixtureName: String) {
  File(this::class.java.classLoader.getResource(fixtureName)!!.file).copyRecursively(newFile(fixtureName), true)
}

internal fun TemporaryFolder.writeBuildDotGradle(buildScript: String) =
  newFile("build.gradle")
    .writeText(buildScript)

fun TemporaryFolder.gradleRunner() =
  GradleRunner.create()
    .withPluginClasspath()
    .forwardOutput()
    .withProjectDir(root)
