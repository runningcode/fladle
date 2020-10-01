package com.osacky.flank.gradle.integration

import org.gradle.testkit.runner.GradleRunner
import org.junit.rules.TemporaryFolder
import java.io.File

fun TemporaryFolder.setupFixture(fixtureName: String) {
  File(this::class.java.classLoader.getResource(fixtureName)!!.file).copyRecursively(newFile(fixtureName), true)
}

internal fun makeBuildDotGradle(where: TemporaryFolder, buildScript: String) = where
  .newFile("build.gradle")
  .writeText(buildScript.trimMargin())

internal fun gradleRun(projectDir: File, arguments: List<String> = emptyList()) =
  commonGradleRunConfig(projectDir, arguments).build()

internal fun failedGradleRun(projectDir: File, arguments: List<String> = emptyList()) =
  commonGradleRunConfig(projectDir, arguments).buildAndFail()

private fun commonGradleRunConfig(projectDir: File, arguments: List<String>) =
  GradleRunner.create()
    .withPluginClasspath()
    .withArguments(arguments)
    .forwardOutput()
    .withProjectDir(projectDir)
