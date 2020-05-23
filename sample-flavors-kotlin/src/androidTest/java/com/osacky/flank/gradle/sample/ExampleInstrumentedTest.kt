package com.osacky.flank.gradle.sample

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import com.osacky.flank.gradle.sample.kotlin.R
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {

  @Rule
  @JvmField val testRule = ActivityTestRule(MainActivity::class.java)

  @Test
  fun seeView() {
    onView(withId(R.id.text_view_hello)).check(matches(isDisplayed()))
  }

  @Test
  fun runAndFail() {
    throw RuntimeException("Test failed")
  }
}
