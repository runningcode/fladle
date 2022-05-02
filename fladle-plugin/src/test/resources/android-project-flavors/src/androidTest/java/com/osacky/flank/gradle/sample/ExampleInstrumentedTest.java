package com.osacky.flank.gradle.sample;


import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import androidx.test.rule.ActivityTestRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.action.ViewActions.typeText;
import java.lang.RuntimeException;

@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {

  @Rule
  private final ActivityTestRule testRule = new ActivityTestRule(MainActivity.class);

  @Test
  public void seeView() {
    onView(withId(R.id.text_view_hello)).check(matches(isDisplayed()));
  }

  @Test
  public void runAndFail() {
    throw new RuntimeException("Test failed");
  }
}
