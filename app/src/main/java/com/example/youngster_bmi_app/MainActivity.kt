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
        readLastInputs()
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
        switchAgeInput()
    }

    private fun switchAgeInput() {
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
        val height = if (heightText.isNotEmpty()) heightText.toDouble() else 0.0
        val gender = Gender.valueOf(genderRadioButton.hint.toString())

        val wrongInputs = mapOf(getString(R.string.age) to age.toDouble(), getString(R.string.weight) to weight, getString(R.string.height) to height)
            .filter { it.value == 0.0  }

        if (wrongInputs.isEmpty()) {
            val bmi = getBmi(weight, height)
            val results = findCentile(standards, gender, age, weight, height, bmi)
            findViewById<TextView>(R.id.BMI).text = ("BMI: ").plus("%.2f".format(bmi))
            findViewById<TextView>(R.id.centileWeight).text = ("Centyl waga: ").plus(getResult(results[Type.WEIGHT]!!))
            findViewById<TextView>(R.id.centileHeight).text = ("Centyl wzrost: ").plus(getResult(results[Type.HEIGHT]!!))
            findViewById<TextView>(R.id.centileBmi).text = ("Centyl BMI: ").plus(getResult(results[Type.BMI]!!))
            saveInputs()
            findViewById<Button>(R.id.calculate).visibility = View.GONE
            findViewById<Button>(R.id.save).visibility = View.VISIBLE
        } else {
            Toast.makeText(applicationContext, getString(R.string.fillFields).plus(wrongInputs.keys.joinToString(", ")), Toast.LENGTH_LONG).show()
        }
        closeKeyboard(view)
    }

    private fun readLastInputs() {
        val inputs = this.getPreferences(Context.MODE_PRIVATE) ?: return
        findViewById<RadioGroup>(R.id.genderRadioGroup).check(inputs.getInt(R.id.genderRadioGroup.toString(), findViewById<RadioButton>(R.id.GIRL).id))
        findViewById<RadioGroup>(R.id.ageRadioGroup).check(inputs.getInt(R.id.ageRadioGroup.toString(), findViewById<RadioButton>(R.id.monthYears).id))
        findViewById<EditText>(R.id.childName).setText(inputs.getString(R.id.childName.toString(), ""))
        if (inputs.getInt(R.id.ageRadioGroup.toString(), 0) == findViewById<RadioButton>(R.id.birthDate).id) {
            switchAgeInput()
            findViewById<TextView>(R.id.pickedDate).text = inputs.getString(R.id.pickedDate.toString(), "")
        } else {
            findViewById<Spinner>(R.id.year).setSelection(applicationContext.resources.getTextArray(R.array.year_array)
                .indexOfFirst { year -> year == inputs.getString(R.id.year.toString(), "0") })
            findViewById<Spinner>(R.id.month).setSelection(applicationContext.resources.getTextArray(R.array.month_array)
                .indexOfFirst { month -> month == inputs.getString(R.id.month.toString(), "0") })
        }
    }

    private fun saveInputs() {
        val initialData = this.getPreferences(Context.MODE_PRIVATE) ?: return
        with (initialData.edit()) {
            putInt(R.id.genderRadioGroup.toString(), findViewById<RadioGroup>(R.id.genderRadioGroup).checkedRadioButtonId)
            putInt(R.id.ageRadioGroup.toString(), findViewById<RadioGroup>(R.id.ageRadioGroup).checkedRadioButtonId)
            putString(R.id.childName.toString(), findViewById<EditText>(R.id.childName).text.toString())
            putString(R.id.pickedDate.toString(), findViewById<TextView>(R.id.pickedDate).text.toString())
            putString(R.id.year.toString(), findViewById<Spinner>(R.id.year).selectedItem.toString())
            putString(R.id.month.toString(), findViewById<Spinner>(R.id.month).selectedItem.toString())
            commit()
        }
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
        var date = findViewById<TextView>(R.id.pickedDate).text.toString()
        if (date.isBlank()) date = getToday(false)
        val birthDate = SimpleDateFormat("dd.MM.yyyy").parse(date).time
        val days = Date().time.minus(birthDate) / (1000 * 3600 * 24)
        return (days / 30.5f).roundToInt() // average days in months
    }

    private fun getResult(result: Int): String {
        return if (result > -1) result.toString() else getString(R.string.noDataForThisAge)
    }

    private fun getToday(startWithYear: Boolean): String {
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR).toString()
        val month = if (c.get(Calendar.MONTH) + 1 > 9) (c.get(Calendar.MONTH) + 1).toString() else "0".plus((c.get(Calendar.MONTH) + 1).toString())
        val day = if (c.get(Calendar.DAY_OF_MONTH) > 9) c.get(Calendar.DAY_OF_MONTH).toString() else "0".plus(c.get(Calendar.DAY_OF_MONTH).toString())
        val separator = "."
        return if (startWithYear) year.plus(separator).plus(month).plus(separator).plus(day) else day.plus(separator).plus(month).plus(separator).plus(year)
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
        val reader = applicationContext.assets.open(getString(R.string.centilesFile)).bufferedReader()
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
        val results = arrayOf(
            findViewById<RadioButton>(pickedRadioGenderId).hint.toString(),
            getAge().toString(),
            findViewById<EditText>(R.id.childName).text.toString(),
            getToday(true),
            findViewById<EditText>(R.id.weight).text.toString(),
            findViewById<EditText>(R.id.height).text.toString()
            )

        try {
            writeToFile(results)
            findViewById<Button>(R.id.save).visibility = View.GONE
            closeKeyboard(view)
            Toast.makeText(applicationContext, R.string.resultsSaved, LENGTH_SHORT).show()
        } catch (e: IOException) {
            Toast.makeText(applicationContext, R.string.resultsNotSaved, LENGTH_SHORT).show()
        }
    }

    private fun writeToFile(results: Array<String>) {
        val writer = applicationContext.openFileOutput(getString(R.string.resultsFile), Context.MODE_APPEND)
            .writer()
            .append(results.joinToString(","))
        writer.close()
    }

    fun showDatePickerDialog(v: View) {
        val datePicker = DatePickerFragment()
        datePicker.show(supportFragmentManager, "datePicker")
    }
}
