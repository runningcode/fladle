package com.osacky.flank.gradle

data class RequiredDeviceKeyMissingException(val key: String) : Exception("Device should have '$key' key set to a value.")
