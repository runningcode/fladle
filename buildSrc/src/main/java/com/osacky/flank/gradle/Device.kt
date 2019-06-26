package com.osacky.flank.gradle

/*
 * The version is a String so also unreleased Android versions can be specified. (e.g. Q-beta-3)
 */
data class Device(val model: String, val version: String, val orientation: String? = null, val locale: String? = null) {

    /*
     * Additional constructor for backwards compatibility, so Android versions can both be
     * specified as a Int (most common case) and as a String.
     */
    constructor(model: String, version: Int, orientation: String? = null, locale: String? = null) :
            this(model, "$version", orientation, locale)
}