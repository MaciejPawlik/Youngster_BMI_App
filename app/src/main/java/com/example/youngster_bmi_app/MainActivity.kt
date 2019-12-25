package com.example.youngster_bmi_app

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.youngster_bmi_app.centile.Centile
import com.example.youngster_bmi_app.centile.Gender
import com.example.youngster_bmi_app.centile.Standard
import com.example.youngster_bmi_app.centile.Type
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.floor
import kotlin.math.pow
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun actionCentile(view: View) {
        val standards = getCentiles()
        val pickedRadioGenderId = findViewById<RadioGroup>(R.id.gender).checkedRadioButtonId
        val genderRadioButton = findViewById<RadioButton>(pickedRadioGenderId)
        val birthDate = findViewById<TextView>(R.id.pickedDate).text.toString()
        val months = countMonths(birthDate)
        val weightText = findViewById<EditText>(R.id.weight).text.toString()
        val heightText = findViewById<EditText>(R.id.height).text.toString()
        val weight = if (weightText.isNotEmpty()) weightText.toDouble() else 0.0
        val height = if (heightText.isNotEmpty()) heightText.toDouble() else 0.1 // to avoid dividing by 0 in BMI
        val gender = Gender.valueOf(genderRadioButton.hint.toString())
        val bmi = getBmi(weight, height)
        val results = findCentile(standards, gender, months, weight, height, bmi)
        val ageValue = findViewById<TextView>(R.id.age).apply {
            text = ("Wiek: ").plus(formatAge(months))
        }
        val bmiValue = findViewById<TextView>(R.id.BMI).apply {
            text = ("BMI: ").plus("%.2f".format(bmi))
        }
        val centileWeight = findViewById<TextView>(R.id.centileWeight).apply {
            text = ("Centyl waga: ").plus(getResult(results[Type.WEIGHT]!!))
        }
        val centileHeight = findViewById<TextView>(R.id.centileHeight).apply {
            text = ("Centyl wzrost: ").plus(getResult(results[Type.HEIGHT]!!))
        }
        val centileBmi = findViewById<TextView>(R.id.centileBmi).apply {
            text = ("Centyl BMI: ").plus(getResult(results[Type.BMI]!!))
        }
    }

    private fun countMonths(date: String): Int {
        val birthDate = SimpleDateFormat("dd.MM.yyyy").parse(date).time
        val days = Date().time.minus(birthDate) / (1000 * 3600 * 24)
        return (days / 30.5f).roundToInt()
    }

    private fun formatAge(months: Int): String {
        val years = floor(months.toDouble() / 12).toInt()
        val months = months.rem(12)
        val yearsFormatted = when (years) {
            0 -> { "" }
            1 -> { "Rok" }
            in 2..4 -> { years.toString().plus(" lata") }
            else -> {years.toString().plus(" lat")}
        }
        val monthsFormatted = when (months) {
            0 -> { "" }
            1 -> { "miesiąc" }
            in 2..4 -> { months.toString().plus(" miesiące") }
            else -> {months.toString().plus(" miesięcy")}
        }
        return yearsFormatted.plus(if (months > 0 && years > 0) " i " else "").plus(monthsFormatted)
    }

    private fun getResult(result: Int): String {
        return if (result > -1) result.toString() else "brak danych dla tego wieku"
    }

    private fun findCentile(standardData: List<Standard>, gender: Gender, age: Int, weight: Double, height: Double, bmi: Double): Map<Type, Int> {
        val typeToCentile = mutableMapOf(Type.WEIGHT to -1, Type.HEIGHT to -1, Type.BMI to -1)

        val standards = standardData.filter { it.gender == gender && it.age == age }

        if (standards.isNotEmpty()) {
            val typeToValue = mapOf(Type.WEIGHT to weight, Type.HEIGHT to height, Type.BMI to bmi)
            standards.forEach { typeToCentile[it.type] = getPercentile(it.centiles, typeToValue[it.type]!!) }
        }
        return typeToCentile
    }

    private fun getPercentile(centiles: List<Centile>, value: Double) : Int {
        val centile = centiles.lastOrNull { it.value <= value }
        return centile?.percentile ?: 1
    }

    private fun getBmi(weight: Double, height: Double): Double {
        return weight / (height / 100).pow(2.0)
    }

    private fun getCentiles(): List<Standard> {
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

    fun showDatePickerDialog(v: View) {
        val datePicker = DatePickerFragment()
        datePicker.show(supportFragmentManager, "datePicker")
    }

}
