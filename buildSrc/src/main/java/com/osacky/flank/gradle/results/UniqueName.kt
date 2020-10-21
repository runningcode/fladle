package com.osacky.flank.gradle.results

import com.osacky.flank.gradle.fladleDir
import org.gradle.api.Project
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.Random

fun createDefaultResultsDir(project: Project, name: String): String {
  val configPath = project.layout.fladleDir.map {
    if (name == "") {
      it
    } else {
      it.dir(name.toLowerCase(Locale.ROOT))
    }
  }.get().asFile.absolutePath
  return configPath + "/results/${uniqueObjectName()}"
}

private fun uniqueObjectName(): String {
  val bucketName = StringBuilder()
  val instant = Instant.now()

  bucketName.append(
    DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss.")
      .withZone(ZoneOffset.UTC)
      .format(instant)
  )

  val nanoseconds = instant.nano.toString()

  if (nanoseconds.length >= 6) {
    bucketName.append(nanoseconds.substring(0, 6))
  } else {
    bucketName.append(nanoseconds.substring(0, nanoseconds.length - 1))
  }

  bucketName.append("_")

  val random = Random()
  // a-z: 97 - 122
  // A-Z: 65 - 90
  repeat(4) {
    val ascii = random.nextInt(26)
    var letter = (ascii + 'a'.toInt()).toChar()

    if (ascii % 2 == 0) {
      letter -= 32 // upcase
    }

    bucketName.append(letter)
  }

  return bucketName.toString()
}
