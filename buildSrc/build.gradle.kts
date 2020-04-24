group = "com.osacky.flank.gradle"
version = "0.9.1"

repositories {
  google()
  mavenCentral()
  jcenter()
}

plugins {
  `kotlin-dsl`
  id("java-gradle-plugin")
  id("com.gradle.plugin-publish") version "0.11.0"
  id("org.jmailen.kotlinter") version "2.3.2"
  `maven-publish`
  signing
}

dependencies {
  compileOnly(gradleApi())
  implementation("com.android.tools.build:gradle:3.6.3")

  testImplementation(gradleTestKit())
  testImplementation("junit:junit:4.13")
  testImplementation("com.google.truth:truth:1.0.1")
}

kotlinter {
  indentSize = 2
}

pluginBundle {
  website = "https://github.com/runningcode/fladle"
  vcsUrl = "https://github.com/runningcode/fladle"
  tags = listOf("flank", "testing", "android", "fladle")

  mavenCoordinates {
    artifactId = "fladle"
    groupId = project.group.toString()
  }
}

gradlePlugin {
  plugins {
    create("fladle") {
      id = "com.osacky.fladle"
      displayName = "Fladle"
      description = "The Gradle Plugin for Flank"
      implementationClass = "com.osacky.flank.gradle.FlankGradlePlugin"
    }
  }
}

kotlinDslPluginOptions {
  experimentalWarning.set(false)
}

tasks.register<Jar>("sourcesJar") {
  from(sourceSets.main.get().allSource)
  archiveClassifier.set("sources")
}

tasks.register<Jar>("javadocJar") {
  from(tasks.javadoc)
  archiveClassifier.set("javadoc")
}

val isReleaseBuild : Boolean = !version.toString().endsWith("SNAPSHOT")

val sonatypeUsername : String? by project
val sonatypePassword : String? by project

publishing {
  repositories {
    repositories {
      maven {
        val releasesRepoUrl = uri("https://oss.sonatype.org/service/local/staging/deploy/maven2/")
        val snapshotsRepoUrl = uri("https://oss.sonatype.org/content/repositories/snapshots/")
        url = if (isReleaseBuild) releasesRepoUrl else snapshotsRepoUrl
        credentials {
          username = sonatypeUsername
          password = sonatypePassword
        }
      }
    }
  }
  publications {
    create<MavenPublication>("mavenJava") {
      from(components["java"])
      artifact(tasks["sourcesJar"])
      artifact(tasks["javadocJar"])
      pom {
        name.set("Fladle")
        description.set("The Gradle Plugin for Flank")
        url.set("https://github.com/runningcode/fladle")
        licenses {
          license {
            name.set("The Apache License, Version 2.0")
            url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
          }
        }
        developers {
          developer {
            id.set("runningcode")
            name.set("Nelson Osacky")
          }
        }
        scm {
          connection.set("scm:git:git://github.com/runningcode/fladle.git")
          developerConnection.set("scm:git:ssh://github.com/runningcode/fladle.git")
          url.set("https://github.com/runningcode/fladle")
        }
      }
    }
  }
}

signing {
  setRequired(isReleaseBuild)
  sign(publishing.publications["mavenJava"])
}
