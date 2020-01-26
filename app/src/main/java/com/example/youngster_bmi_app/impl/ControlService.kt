package com.example.youngster_bmi_app.impl

import android.content.Context
import com.example.youngster_bmi_app.model.*
import java.io.File

class ControlService(
    private val centileService: CentileService,
    private val fileNameResults: String,
    private val noDataMessage: String
) {

    fun isFileAvailableForReading(): Boolean {
        return File(centileService.context.filesDir, fileNameResults).canRead()
    }

    fun saveControlResults(results: Array<String>) {
        centileService.context.openFileOutput(fileNameResults, Context.MODE_APPEND).writer().apply {
            appendln(results.joinToString(","))
            close()
        }
    }

    fun getControlResults(): List<ControlCentile> {
        val standards = centileService.getCentiles()
        return readResults().map { it.toControlCentile(standards) }
    }

    private fun Control.toControlCentile(standards: List<Standard>) = ControlCentile(
        gender = gender,
        age = formatAge(age.toInt()),
        childName = childName,
        date = date,
        weight = weight,
        height = height,
        bmi = "%.2f".format(centileService.getBmi(weight.toDouble(), height.toDouble())),
        centileWeight = formatResults(centileService.findCentile(standards, Gender.valueOf(gender), age.toInt(),Type.WEIGHT to weight.toDouble())),
        centileHeight = formatResults(centileService.findCentile(standards, Gender.valueOf(gender), age.toInt(),Type.HEIGHT to height.toDouble())),
        centileBmi = formatResults(centileService.findCentile(standards, Gender.valueOf(gender), age.toInt(),Type.BMI to centileService.getBmi(weight.toDouble(), height.toDouble())))
    )

    private fun formatResults(results: Int): String {
        return if (results > 0) results.toString() else noDataMessage
    }

    private fun formatAge(age: Int): String {
        val months = age.rem(12)
        val yearsFormatted = when (val years = age / 12) {
            0 -> ""
            1 -> "1r. "
            else -> years.toString().plus("l. ")
        }
        return yearsFormatted.plus(months.toString().plus("m."))
    }

    private fun readResults(): List<Control> {
        val results = mutableListOf<Control>()
        val reader = centileService.context.openFileInput(fileNameResults).bufferedReader()
        for (line in reader.readLines()) {
            val dataField = line.split(",")
            results.add(
                Control(
                    dataField[0],
                    dataField[1],
                    dataField[2],
                    dataField[3],
                    dataField[4],
                    dataField[5]
                )
            )
        }
        reader.close()
        return results
    }
}