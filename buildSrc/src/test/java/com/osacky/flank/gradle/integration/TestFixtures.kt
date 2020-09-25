package com.osacky.flank.gradle.integration

import org.gradle.testkit.runner.GradleRunner
import org.junit.rules.TemporaryFolder
import java.io.File

fun TemporaryFolder.setupFixture(fixtureName: String) {
  File(this::class.java.classLoader.getResource(fixtureName)!!.file).copyRecursively(newFile(fixtureName), true)
}

internal fun makeGradleFile(where: TemporaryFolder, buildScript: String) = where
  .newFile("build.gradle")
  .writeText(buildScript.trimMargin())

internal fun gradleRun(projectDir: File, arguments: List<String> = emptyList()) =
  GradleRunner.create()
    .withPluginClasspath()
    .withArguments(arguments)
    .forwardOutput()
    .withProjectDir(projectDir)
    .build()
