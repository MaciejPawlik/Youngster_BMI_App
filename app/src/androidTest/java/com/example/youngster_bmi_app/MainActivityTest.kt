package com.example.youngster_bmi_app

import androidx.test.espresso.Espresso.onData
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.filters.LargeTest
import androidx.test.filters.SmallTest
import androidx.test.rule.ActivityTestRule
import androidx.test.runner.AndroidJUnit4
import org.hamcrest.CoreMatchers.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class MainActivityTest {

    @get:Rule
    val activityRule = ActivityTestRule(MainActivity::class.java)

    @Test
    fun calculateCentiles() {
        val context = activityRule.activity.applicationContext

        onView(withId(R.id.BOY))
            .perform(click())
            .check(matches(isChecked()))

        onView((withId(R.id.yearSpinner)))
            .perform(click())
        onData(allOf(`is`(instanceOf(String::class.java)),
            `is`("10"))).perform(click())

        onView((withId(R.id.monthSpinner)))
            .perform(click())
        onData(allOf(`is`(instanceOf(String::class.java)),
            `is`("10"))).perform(click())

        onView(withId(R.id.weightEditText))
            .perform(typeText("30"))

        onView(withId(R.id.heightEditText))
            .perform(typeText("141"))

        onView(withId(R.id.childNameEditText))
            .perform(typeText("James"))

        onView(withId(R.id.calculateButton))
            .perform(click())

        onView(withId(R.id.BMITextView))
            .check(matches(withSubstring(context.getString(R.string.BMI))))

        onView(withId(R.id.centileWeightTextView))
            .check(matches(withSubstring(context.getString(R.string.centileWeight))))

        onView(withId(R.id.centileHeightTextView))
            .check(matches(withSubstring(context.getString(R.string.centileHeight))))

        onView(withId(R.id.centileBmiTextView))
            .check(matches(withSubstring(context.getString(R.string.centileBmi))))

    }
}