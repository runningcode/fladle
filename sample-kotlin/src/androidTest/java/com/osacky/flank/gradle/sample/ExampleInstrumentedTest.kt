package com.osacky.flank.gradle.sample


import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.rule.ActivityTestRule
import androidx.test.runner.AndroidJUnit4

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.action.ViewActions.typeText
import java.lang.RuntimeException

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
