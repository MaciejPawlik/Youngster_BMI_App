package com.example.youngster_bmi_app

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import com.example.youngster_bmi_app.centile.Centile
import com.example.youngster_bmi_app.centile.Gender
import com.example.youngster_bmi_app.centile.Standard
import com.example.youngster_bmi_app.centile.Type
import kotlinx.android.synthetic.main.activity_main.view.*
import kotlin.math.pow

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun actionCentile(view: View) {
        val standards = getCentiles()
        val pickedRadioGenderId = findViewById<RadioGroup>(R.id.gender).checkedRadioButtonId
        val genderRadioButton = findViewById<RadioButton>(pickedRadioGenderId)
        val months = findViewById<EditText>(R.id.month).text.toString().toInt()
        val weight = findViewById<EditText>(R.id.weight).text.toString().toDouble()
        val height = findViewById<EditText>(R.id.height).text.toString().toDouble()
        val gender = Gender.valueOf(genderRadioButton.hint.toString())
        val results = findCentile(standards, gender, months, weight, height)
        val centileWeight = findViewById<TextView>(R.id.centileWeight).apply {
            text = text.toString().plus(": ").plus(results[Type.WEIGHT].toString())
        }
        val centileHeight = findViewById<TextView>(R.id.centileHeight).apply {
            text = text.toString().plus(": ").plus(results[Type.HEIGHT].toString())
        }
        val centileBmi = findViewById<TextView>(R.id.centileBmi).apply {
            text = text.toString().plus(": ").plus(results[Type.BMI].toString())
        }
    }

    fun findCentile(standardData: List<Standard>, gender: Gender, age: Int, weight: Double, height: Double): Map<Type, Int> {
        val typeToCentile = mutableMapOf(Type.WEIGHT to -1, Type.HEIGHT to -1, Type.BMI to -1)

        val standards = standardData.filter { it.gender == gender && it.age == age }

        if (standards.isNotEmpty()) {
            val bmi = weight / (height / 100).pow(2.0)
            val typeToValue = mapOf(Type.WEIGHT to weight, Type.HEIGHT to height, Type.BMI to bmi)
            standards.forEach { typeToCentile[it.type] = getPercentile(it.centiles, typeToValue[it.type]!!) }
        }
        return typeToCentile
    }

    fun getPercentile(centiles: List<Centile>, value: Double) : Int {
        val centile = centiles.lastOrNull { it.value <= value }
        return centile?.percentile ?: 1
    }

    fun getCentiles(): List<Standard> {
        val centiles = mutableListOf<Standard>()
        val reader = applicationContext.assets.open("centiles.csv").bufferedReader().readLines()
        for (line in reader) {
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
        return centiles
    }
}
