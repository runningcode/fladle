package com.osacky.flank.gradle.integration

import org.gradle.testkit.runner.GradleRunner
import org.junit.rules.TemporaryFolder
import java.io.File

fun TemporaryFolder.setupFixture(fixtureName: String) {
  File(this::class.java.classLoader.getResource(fixtureName)!!.file).copyRecursively(newFile(fixtureName), true)
}

internal fun makeGradleFile(where: TemporaryFolder, stringProvider: () -> String) = where
  .newFile("build.gradle")
  .writeText(stringProvider().trimMargin())

internal fun gradleRun(block: GradleRunFladle.() -> Unit) = GradleRunFladle().apply(block).run {
  GradleRunner.create()
    .withPluginClasspath()
    .withArguments(arguments)
    .forwardOutput()
    .withProjectDir(projectDir)
    .build()
}

internal data class GradleRunFladle(
  var arguments: List<String> = emptyList(),
  var projectDir: File? = null
)
