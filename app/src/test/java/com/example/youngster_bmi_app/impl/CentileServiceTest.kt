package com.example.youngster_bmi_app.impl

import org.junit.Test
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.youngster_bmi_app.R
import com.example.youngster_bmi_app.model.Gender
import com.example.youngster_bmi_app.model.Type
import org.junit.Assert.*
import org.hamcrest.CoreMatchers.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class CentileServiceTest {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val tested = CentileService(context, context.getString(R.string.noDataForThisAgeLong))

    @Test
    fun findCentile() {
        val result = tested.findCentile(tested.getCentiles(), Gender.BOY, 60, Type.HEIGHT to 108.5)
        assertThat("Centile wasn't calculate correctly", result, `is`(25))
    }

    @Test
    fun getCentiles() {
        val result = tested.getCentiles()
        assertThat("Centile data wasn't read", result.size, `is`(not(0)))
        assertNotNull(result)
    }

    @Test
    fun formatCentiles() {
        val result = tested.formatCentile(context.getString(R.string.centileHeight), Gender.BOY, 60, Type.HEIGHT to 108.5)
        val expected = context.getString(R.string.centileHeight).plus(": 25")
        assertEquals(expected, result)
    }
}