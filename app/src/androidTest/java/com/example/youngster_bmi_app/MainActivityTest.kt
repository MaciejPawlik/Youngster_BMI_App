package com.example.youngster_bmi_app

import android.content.Context
import android.content.SharedPreferences
import androidx.test.espresso.Espresso.onData
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.withDecorView
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import androidx.test.runner.AndroidJUnit4
import org.hamcrest.CoreMatchers.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class MainActivityTest {

    private lateinit var context: Context
    private lateinit var year: String
    private lateinit var month: String
    private lateinit var height: String
    private lateinit var weight: String
    private lateinit var childName: String
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var preferencesKeys: List<String>

    @get:Rule
    val activityRule = ActivityTestRule(MainActivity::class.java)

    @Before
    fun setUp() {
        context = activityRule.activity.applicationContext
        year = "10"
        month = "10"
        height = "141"
        weight = "30"
        childName = "James"
        sharedPreferences = activityRule.activity.getPreferences(Context.MODE_PRIVATE)
        preferencesKeys = listOf(
            R.id.genderRadioGroup.toString(),
            R.id.ageRadioGroup.toString(),
            R.id.childNameEditText.toString(),
            R.id.pickedDateTextView.toString(),
            R.id.yearSpinner.toString(),
            R.id.monthSpinner.toString()
        )
    }

    @After
    fun shutDown() {
        sharedPreferences.edit().apply {
            clear()
            commit()
        }
        context.deleteFile(context.getString(R.string.resultsFile))
    }

    @Test
    fun checkInitialStateAndToastMessage() {
        onView(withId(R.id.listResultsButton))
            .check(matches(not(isDisplayed())))

        onView(withId(R.id.saveResultsButton))
            .check(matches(not(isDisplayed())))

        onView(withId(R.id.calculateButton))
            .perform(click())

        onView(withSubstring(context.getString(R.string.fillFields)))
            .inRoot(withDecorView(not(`is`(activityRule.activity.window.decorView))))
            .check(matches(isDisplayed()))
    }

    @Test
    fun calculateCentiles() {
        assert(sharedPreferences.all.isEmpty())

       //action:
        onView(withId(R.id.BOY))
            .perform(click())
            .check(matches(isChecked()))

        onView((withId(R.id.yearSpinner)))
            .perform(click())
        onData(allOf(`is`(instanceOf(String::class.java)),
            `is`(year))).perform(click())

        onView((withId(R.id.monthSpinner)))
            .perform(click())
        onData(allOf(`is`(instanceOf(String::class.java)),
            `is`(month))).perform(click())

        onView(withId(R.id.weightEditText))
            .perform(typeText(weight))

        onView(withId(R.id.heightEditText))
            .perform(typeText(height))

        onView(withId(R.id.childNameEditText))
            .perform(typeText(childName))

        onView(withId(R.id.calculateButton))
            .perform(click())

        //results:
        onView(withId(R.id.BMITextView))
            .check(matches(withText(context.getString(R.string.BMI).plus(": ").plus("15,09"))))

        onView(withId(R.id.centileWeightTextView))
            .check(matches(withText(context.getString(R.string.centileWeight).plus(": ").plus(context.getString(R.string.noDataForThisAgeLong)))))

        onView(withId(R.id.centileHeightTextView))
            .check(matches(withText(context.getString(R.string.centileHeight).plus(": ").plus("25"))))

        onView(withId(R.id.centileBmiTextView))
            .check(matches(withText(context.getString(R.string.centileBmi).plus(": ").plus("10"))))

        onView(withId(R.id.calculateButton))
            .check(matches(not(isDisplayed())))

        onView(withId(R.id.saveResultsButton))
            .check(matches(isDisplayed()))
            .perform(click())

        assert(sharedPreferences.all.keys.containsAll(preferencesKeys))

        //edit data:
        onView(withId(R.id.weightEditText))
            .perform(clearText())
            .perform(replaceText(weight))

        onView(withId(R.id.calculateButton))
            .check(matches(isDisplayed()))

        onView(withId(R.id.saveResultsButton))
            .check(matches(not(isDisplayed())))

        onView(withId(R.id.calculateButton))
            .check(matches(not(isDisplayed())))

        //open results:
        onView(withId(R.id.listResultsButton))
            .perform(click())
    }
}