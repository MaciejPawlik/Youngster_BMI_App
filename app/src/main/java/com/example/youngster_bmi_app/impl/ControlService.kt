package com.example.youngster_bmi_app.impl

import android.content.Context
import com.example.youngster_bmi_app.model.*

class ControlService {

    fun loadResults(context: Context, fileName: String): List<Control> {
        val results = mutableListOf<Control>()
        val reader = context.openFileInput(fileName).bufferedReader()
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
        return results
    }
}