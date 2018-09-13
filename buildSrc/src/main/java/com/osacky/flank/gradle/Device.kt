package com.osacky.flank.gradle

data class Device(val model: String, val version: Int, val orientation: String? = null, val locale: String? = null)