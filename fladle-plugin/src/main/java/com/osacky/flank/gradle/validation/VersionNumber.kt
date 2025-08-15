package com.osacky.flank.gradle.validation

/*
* Copyright 2012 the original author or authors.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

/**
 * Copied and trimmed from [org.gradle.util.VersionNumber].
 */
class VersionNumber private constructor(
  private val major: Int,
  private val minor: Int,
  private val micro: Int,
  private val patch: Int,
  private val qualifier: String?,
  private val scheme: AbstractScheme,
) : Comparable<VersionNumber> {
  override fun compareTo(other: VersionNumber): Int {
    if (major != other.major) {
      return major - other.major
    }
    if (minor != other.minor) {
      return minor - other.minor
    }
    if (micro != other.micro) {
      return micro - other.micro
    }
    if (patch != other.patch) {
      return patch - other.patch
    }
    return qualifier.orEmpty().lowercase()
      .compareTo(other.qualifier.orEmpty().lowercase())
  }

  override fun equals(other: Any?): Boolean {
    return other is VersionNumber && compareTo(other) == 0
  }

  override fun hashCode(): Int {
    var result = major
    result = 31 * result + minor
    result = 31 * result + micro
    result = 31 * result + patch
    result = 31 * result + qualifier.hashCode()
    return result
  }

  override fun toString(): String {
    return scheme.format(this)
  }

  /**
   * Returns the version number scheme.
   */
  interface Scheme {
    fun parse(versionString: String): VersionNumber

    fun format(versionNumber: VersionNumber): String
  }

  private abstract class AbstractScheme protected constructor(val depth: Int) : Scheme {
    override fun parse(versionString: String): VersionNumber {
      if (versionString.isEmpty()) {
        return UNKNOWN
      }
      val scanner = Scanner(versionString)

      if (!scanner.hasDigit()) {
        return UNKNOWN
      }
      var minor = 0
      var micro = 0
      var patch = 0
      val major = scanner.scanDigit()
      if (scanner.isSeparatorAndDigit('.')) {
        scanner.skipSeparator()
        minor = scanner.scanDigit()
        if (scanner.isSeparatorAndDigit('.')) {
          scanner.skipSeparator()
          micro = scanner.scanDigit()
          if (depth > 3 && scanner.isSeparatorAndDigit('.', '_')) {
            scanner.skipSeparator()
            patch = scanner.scanDigit()
          }
        }
      }

      if (scanner.isEnd) {
        return VersionNumber(major, minor, micro, patch, null, this)
      }

      if (scanner.isQualifier) {
        scanner.skipSeparator()
        return VersionNumber(major, minor, micro, patch, scanner.remainder(), this)
      }

      return UNKNOWN
    }

    private class Scanner(val str: String) {
      var pos: Int = 0

      fun hasDigit(): Boolean {
        return pos < str.length && Character.isDigit(str.get(pos))
      }

      fun isSeparatorAndDigit(vararg separators: Char): Boolean {
        return pos < str.length - 1 && oneOf(*separators) && Character.isDigit(str.get(pos + 1))
      }

      fun oneOf(vararg separators: Char): Boolean {
        val current = str.get(pos)
        for (separator in separators) {
          if (current == separator) {
            return true
          }
        }
        return false
      }

      val isQualifier: Boolean
        get() = pos < str.length - 1 && oneOf('.', '-')

      fun scanDigit(): Int {
        val start = pos
        while (hasDigit()) {
          pos++
        }
        return str.substring(start, pos).toInt()
      }

      val isEnd: Boolean
        get() = pos == str.length

      fun skipSeparator() {
        pos++
      }

      fun remainder(): String? {
        return if (pos == str.length) null else str.substring(pos)
      }
    }
  }

  private class DefaultScheme : AbstractScheme(3) {
    override fun format(versionNumber: VersionNumber): String {
      return String.format(
        VERSION_TEMPLATE,
        versionNumber.major,
        versionNumber.minor,
        versionNumber.micro,
        if (versionNumber.qualifier == null) "" else "-" + versionNumber.qualifier,
      )
    }

    companion object {
      private const val VERSION_TEMPLATE = "%d.%d.%d%s"
    }
  }

  companion object {
    private val DEFAULT_SCHEME = DefaultScheme()
    val UNKNOWN: VersionNumber = version(0)

    @JvmOverloads
    fun version(
      major: Int,
      minor: Int = 0,
    ): VersionNumber {
      return VersionNumber(
        major = major,
        minor = minor,
        micro = 0,
        patch = 0,
        qualifier = null,
        scheme = DEFAULT_SCHEME,
      )
    }

    fun parse(versionString: String): VersionNumber {
      return DEFAULT_SCHEME.parse(versionString)
    }
  }
}
