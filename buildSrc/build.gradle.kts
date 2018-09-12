group = "com.osacky.flank.gradle"
version = "0.1-SNAPSHOT"

repositories {
  jcenter()
}

plugins {
  `kotlin-dsl`
  id("java-gradle-plugin")
  id("com.gradle.plugin-publish") version "0.10.0"
}

dependencies {
  compileOnly(gradleApi())

  implementation("de.undercouch:gradle-download-task:3.4.3")

  testImplementation(gradleTestKit())

}
