package com.example.youngster_bmi_app

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import android.widget.Toast.LENGTH_SHORT
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.widget.addTextChangedListener
import com.example.youngster_bmi_app.centile.Centile
import com.example.youngster_bmi_app.centile.Gender
import com.example.youngster_bmi_app.centile.Standard
import com.example.youngster_bmi_app.centile.Type
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.pow
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        updateSpinners()
        addListeners()
    }

    private fun updateSpinners() {
        val year = findViewById<Spinner>(R.id.year)
        ArrayAdapter.createFromResource(this, R.array.year_array, android.R.layout.simple_spinner_dropdown_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            year.adapter = adapter
        }
        val month = findViewById<Spinner>(R.id.month)
        ArrayAdapter.createFromResource(this, R.array.month_array, android.R.layout.simple_spinner_dropdown_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            month.adapter = adapter
        }
        year.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                goToRecalculation()
            }
        }
        month.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                goToRecalculation()
            }
        }
    }

    private fun addListeners() {
        findViewById<RadioGroup>(R.id.genderRadioGroup).setOnCheckedChangeListener { group, checkedId -> goToRecalculation() }
        findViewById<RadioGroup>(R.id.ageRadioGroup).setOnCheckedChangeListener { group, checkedId -> goToRecalculation() }
        findViewById<EditText>(R.id.weight).addTextChangedListener { goToRecalculation() }
        findViewById<EditText>(R.id.height).addTextChangedListener { goToRecalculation() }
        findViewById<TextView>(R.id.pickedDate).addTextChangedListener { goToRecalculation() }
    }

    private fun goToRecalculation() {
        val calculateButton = findViewById<Button>(R.id.calculate)
        if (calculateButton.visibility == View.GONE) {
            findViewById<Button>(R.id.save).visibility = View.GONE
            calculateButton.visibility = View.VISIBLE
        }
    }

    fun toggleAgeInput(view: View) {
        val birth = findViewById<ConstraintLayout>(R.id.linearLayoutBirth)
        val birthVisibility = birth.visibility
        val age = findViewById<ConstraintLayout>(R.id.linearLayoutAge)
        val ageVisibility = age.visibility
        birth.visibility = ageVisibility
        age.visibility = birthVisibility
    }

    fun calculateCentile(view: View) {
        val standards = getCentiles()
        val pickedRadioGenderId = findViewById<RadioGroup>(R.id.genderRadioGroup).checkedRadioButtonId
        val genderRadioButton = findViewById<RadioButton>(pickedRadioGenderId)
        val age = getAge()
        val weightText = findViewById<EditText>(R.id.weight).text.toString()
        val heightText = findViewById<EditText>(R.id.height).text.toString()
        val weight = if (weightText.isNotEmpty()) weightText.toDouble() else 0.0
        val height = if (heightText.isNotEmpty()) heightText.toDouble() else 0.1 // to avoid dividing by 0 in BMI
        val gender = Gender.valueOf(genderRadioButton.hint.toString())
        val bmi = getBmi(weight, height)
        val results = findCentile(standards, gender, age, weight, height, bmi)

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
        findViewById<Button>(R.id.calculate).visibility = View.GONE
        findViewById<Button>(R.id.save).visibility = View.VISIBLE
        closeKeyboard(view)
    }

    private fun closeKeyboard(view: View) {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun getAge(): Int = if (findViewById<RadioButton>(R.id.monthYears).isChecked) {
        val months = findViewById<Spinner>(R.id.month).selectedItem.toString().toInt()
        val years = findViewById<Spinner>(R.id.year).selectedItem.toString().toInt()
        years * 12 + months
    } else {
        countMonths()
    }

    private fun countMonths(): Int {
        val date = findViewById<TextView>(R.id.pickedDate).text.toString()
        val birthDate = SimpleDateFormat("dd.MM.yyyy").parse(date).time
        val days = Date().time.minus(birthDate) / (1000 * 3600 * 24)
        return (days / 30.5f).roundToInt() // average days in months
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
        val reader = applicationContext.assets.open("centiles.csv").bufferedReader()
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

    fun saveResults(view: View) {
        val pickedRadioGenderId = findViewById<RadioGroup>(R.id.genderRadioGroup).checkedRadioButtonId
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR).toString()
        val month = if (c.get(Calendar.MONTH) + 1 > 9) c.get(Calendar.MONTH).toString() else "0".plus(c.get(Calendar.MONTH).toString())
        val day = if (c.get(Calendar.DAY_OF_MONTH) > 9) c.get(Calendar.DAY_OF_MONTH).toString() else "0".plus(c.get(Calendar.DAY_OF_MONTH).toString())
        val separator = "."
        val results = arrayOf(
            findViewById<RadioButton>(pickedRadioGenderId).hint.toString(),
            getAge().toString(),
            findViewById<EditText>(R.id.childName).text.toString(),
            year.plus(separator).plus(month).plus(separator).plus(day),
            findViewById<EditText>(R.id.weight).text.toString(),
            findViewById<EditText>(R.id.height).text.toString()
            )

        try {
            writeToFile(results)
            findViewById<Button>(R.id.save).visibility = View.GONE
            closeKeyboard(view)
            Toast.makeText(applicationContext, "Zapisano wyniki", LENGTH_SHORT).show()
        } catch (e: IOException) {
            Toast.makeText(applicationContext, "Błąd zapisu wyników", LENGTH_SHORT).show()
        }
    }

    private fun writeToFile(results: Array<String>) {
        val writer = applicationContext.openFileOutput("results.csv", Context.MODE_APPEND)
            .writer()
            .append(results.joinToString(","))
        writer.close()
    }

    fun showDatePickerDialog(v: View) {
        val datePicker = DatePickerFragment()
        datePicker.show(supportFragmentManager, "datePicker")
    }

}
