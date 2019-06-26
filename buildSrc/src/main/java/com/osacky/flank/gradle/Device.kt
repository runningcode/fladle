package com.osacky.flank.gradle

data class Device(val model: String, val version: String, val orientation: String? = null, val locale: String? = null) {
    constructor(model: String, version: Int, orientation: String? = null, locale: String? = null) :
            this(model, "$version", orientation, locale)
}