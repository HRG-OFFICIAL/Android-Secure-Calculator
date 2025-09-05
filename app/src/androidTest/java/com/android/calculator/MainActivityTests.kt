package com.android.calculator

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.calculator.activities.MainActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivityTests {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun testBasicCalculation() {
        // Click on number buttons to input "2+2"
        onView(withId(R.id.twoButton)).perform(click())
        onView(withId(R.id.addButton)).perform(click())
        onView(withId(R.id.twoButton)).perform(click())
        onView(withId(R.id.equalsButton)).perform(click())

        // Verify the result is displayed
        onView(withId(R.id.resultDisplay)).check(matches(withText("4")))
    }

    @Test
    fun testClearButton() {
        // Input some numbers
        onView(withId(R.id.oneButton)).perform(click())
        onView(withId(R.id.twoButton)).perform(click())
        onView(withId(R.id.threeButton)).perform(click())

        // Click clear button
        onView(withId(R.id.clearButton)).perform(click())

        // Verify input is cleared
        onView(withId(R.id.input)).check(matches(withText("")))
    }

    @Test
    fun testMainActivityIsDisplayed() {
        // Verify that the main activity is displayed
        onView(withId(R.id.input)).check(matches(isDisplayed()))
        onView(withId(R.id.resultDisplay)).check(matches(isDisplayed()))
        onView(withId(R.id.zeroButton)).check(matches(isDisplayed()))
        onView(withId(R.id.equalsButton)).check(matches(isDisplayed()))
    }
}
