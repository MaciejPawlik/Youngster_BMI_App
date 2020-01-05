package com.example.youngster_bmi_app.impl

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.youngster_bmi_app.R
import com.example.youngster_bmi_app.model.ControlCentile
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ControlServiceTest {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val centileService = CentileService(context)
    val resultFileName = context.getString(R.string.resultsFile)
    val noDataMessage = context.getString(R.string.noDataForThisAgeShort)
    val tested = ControlService(centileService, resultFileName, context.getString(R.string.noDataForThisAgeShort))

    @Test
    fun getControlResults() {
        //given
        val mockedData = arrayOf("BOY", "130", "James", "2020-01-05", "30", "141")
        context.openFileOutput(resultFileName, Context.MODE_APPEND).writer().apply {
            appendln(mockedData.joinToString(","))
            close()
        }
        val expected = listOf(ControlCentile("BOY", "10l. 10m.", "James", "2020-01-05", "30", "141", "15.09", "25", noDataMessage, "10"))
        //when
        val result = tested.getControlResults()
        //then
        assertEquals(expected, result)
    }
}