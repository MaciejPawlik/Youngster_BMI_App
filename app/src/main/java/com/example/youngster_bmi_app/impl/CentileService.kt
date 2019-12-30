package com.example.youngster_bmi_app.impl

import android.content.Context
import com.example.youngster_bmi_app.model.Centile
import com.example.youngster_bmi_app.model.Gender
import com.example.youngster_bmi_app.model.Standard
import com.example.youngster_bmi_app.model.Type
import kotlin.math.pow

private const val FILE_NAME: String = "centiles.csv"

class CentileService(val context: Context) {

    fun findCentile(standards: List<Standard>, gender: Gender, age: Int, typeToValue: Pair<Type, Double>): Int {
        val standard = standards.filter { it.gender == gender && it.age == age && it.type == typeToValue.first }
        return if (standard.isNotEmpty()) getPercentile(standard.first().centiles, typeToValue.second) else -1
    }

    fun getBmi(weight: Double, height: Double): Double {
        return weight / (height / 100).pow(2.0)
    }

    private fun getPercentile(centiles: List<Centile>, value: Double) : Int {
        val centile = centiles.lastOrNull { it.value <= value }
        return centile?.percentile ?: 1
    }

    fun getCentiles(): List<Standard> {
        val centiles = mutableListOf<Standard>()
        val reader = context.assets.open(FILE_NAME).bufferedReader()
        for (line in reader.readLines()) {
            val dataField = line.split(",")
            centiles.add(
                Standard(
                    Gender.valueOf(dataField[0]),
                    Type.valueOf(dataField[1]),
                    dataField[2].toInt(),
                    listOf(
                        Centile(1, dataField[3].toDouble()),
                        Centile(3, dataField[4].toDouble()),
                        Centile(5, dataField[5].toDouble()),
                        Centile(10, dataField[6].toDouble()),
                        Centile(15, dataField[7].toDouble()),
                        Centile(25, dataField[8].toDouble()),
                        Centile(50, dataField[9].toDouble()),
                        Centile(75, dataField[10].toDouble()),
                        Centile(85, dataField[11].toDouble()),
                        Centile(90, dataField[12].toDouble()),
                        Centile(95, dataField[13].toDouble()),
                        Centile(97, dataField[14].toDouble()),
                        Centile(99, dataField[15].toDouble())
                    )
                )
            )
        }
        reader.close()
        return centiles
    }
}