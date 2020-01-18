package com.example.youngster_bmi_app

import android.content.Context
import android.content.SharedPreferences
import androidx.test.espresso.Espresso.onData
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.withDecorView
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import androidx.test.runner.AndroidJUnit4
import com.example.youngster_bmi_app.model.ControlCentile
import com.example.youngster_bmi_app.model.Gender
import org.hamcrest.CoreMatchers.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@RunWith(AndroidJUnit4::class)
@LargeTest
class MainActivityTest {

    private val year = "10"
    private val month = "10"
    private val height = "141"
    private val weight = "30"
    private val bmi = "15,09"
    private val childName = "James"
    private val centileHeight = "25"
    private val centileBmi = "10"

    private lateinit var context: Context
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var preferencesKeys: List<String>
    private lateinit var expectedBmi: String
    private lateinit var expectedCentileWeight: String
    private lateinit var expectedCentileHeight: String
    private lateinit var expectedCentileBmi: String
    private lateinit var expectedControlCentile: ControlCentile

    @get:Rule
    val activityRule = ActivityTestRule(MainActivity::class.java)

    @Before
    fun setUp() {
        context = activityRule.activity.applicationContext
        sharedPreferences = activityRule.activity.getPreferences(Context.MODE_PRIVATE)
        preferencesKeys = listOf(
            R.id.genderRadioGroup.toString(),
            R.id.ageRadioGroup.toString(),
            R.id.childNameEditText.toString(),
            R.id.pickedDateTextView.toString(),
            R.id.yearSpinner.toString(),
            R.id.monthSpinner.toString()
        )
        expectedBmi = context.getString(R.string.BMI).plus(": ").plus(bmi)
        expectedCentileWeight = context.getString(R.string.centileWeight).plus(": ").plus(context.getString(R.string.noDataForThisAgeLong))
        expectedCentileHeight = context.getString(R.string.centileHeight).plus(": ").plus(centileHeight)
        expectedCentileBmi = context.getString(R.string.centileBmi).plus(": ").plus(centileBmi)
        expectedControlCentile = ControlCentile(
            Gender.BOY.toString(),
            year.plus("l. ").plus(month).plus("m."),
            childName,
            LocalDate.now().format(DateTimeFormatter.ISO_DATE),
            weight,
            height,
            bmi,
            context.getString(R.string.noDataForThisAgeShort),
            centileHeight,
            centileBmi
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
        onView(withId(R.id.resultListIcon))
            .check(doesNotExist())

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
            .check(matches(isDisplayed()))
            .check(matches(withText(expectedBmi)))

        onView(withId(R.id.centileWeightTextView))
            .check(matches(isDisplayed()))
            .check(matches(withText(expectedCentileWeight)))

        onView(withId(R.id.centileHeightTextView))
            .check(matches(isDisplayed()))
            .check(matches(withText(expectedCentileHeight)))

        onView(withId(R.id.centileBmiTextView))
            .check(matches(isDisplayed()))
            .check(matches(withText(expectedCentileBmi)))

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
            .check(matches(isDisplayed()))

        onView(withSubstring(context.getString(R.string.resultsSaved)))
            .inRoot(withDecorView(not(`is`(activityRule.activity.window.decorView))))
            .check(matches(isDisplayed()))

        //open results:
        onView(withId(R.id.resultListIcon))
            .perform(click())

        onData(allOf(`is`(instanceOf(ControlCentile::class.java)), hasItem(expectedControlCentile)))
    }
}