package com.osacky.flank.gradle.sample

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {

  @Test
  fun seeView() {
    assert(true)
  }

  @Test
  fun runAndFail() {
    throw RuntimeException("Test failed")
  }
}
