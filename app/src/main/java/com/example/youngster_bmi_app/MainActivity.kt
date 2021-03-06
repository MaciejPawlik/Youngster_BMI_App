package com.example.youngster_bmi_app

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import android.widget.Toast.LENGTH_SHORT
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import com.example.youngster_bmi_app.impl.CentileService
import com.example.youngster_bmi_app.impl.ControlService
import com.example.youngster_bmi_app.model.*
import kotlinx.android.synthetic.main.activity_main.*
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {

    private lateinit var calculateButton: Button
    private lateinit var saveButton: Button
    private lateinit var yearSpinner: Spinner
    private lateinit var monthSpinner: Spinner
    private lateinit var ageRadioGroup: RadioGroup
    private lateinit var genderRadioGroup: RadioGroup
    private lateinit var weightEditText: EditText
    private lateinit var heightEditText: EditText
    private lateinit var childNameEditText: EditText
    private lateinit var pickedDateTexView: TextView
    private lateinit var bmiTexView: TextView
    private lateinit var centileWeightTexView: TextView
    private lateinit var centileHeightTexView: TextView
    private lateinit var centileBmiTexView: TextView
    private lateinit var centileService: CentileService
    private lateinit var controlService: ControlService
    private lateinit var listResultMenuItem: MenuItem
    private var defaultTextViewColor = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))

        centileService = CentileService(applicationContext, getString(R.string.noDataForThisAgeLong))
        controlService = ControlService(centileService, getString(R.string.resultsFile), getString(R.string.noDataForThisAgeLong))
        calculateButton = findViewById(R.id.calculateButton)
        saveButton = findViewById(R.id.saveResultsButton)
        ageRadioGroup = findViewById(R.id.ageRadioGroup)
        genderRadioGroup = findViewById(R.id.genderRadioGroup)
        heightEditText = findViewById(R.id.heightEditText)
        weightEditText = findViewById(R.id.weightEditText)
        childNameEditText = findViewById(R.id.childNameEditText)
        pickedDateTexView = findViewById(R.id.pickedDateTextView)
        bmiTexView = findViewById(R.id.BMITextView)
        centileBmiTexView = findViewById(R.id.centileBmiTextView)
        centileHeightTexView = findViewById(R.id.centileHeightTextView)
        centileWeightTexView = findViewById(R.id.centileWeightTextView)
        defaultTextViewColor = centileBmiTexView.currentTextColor

        updateSpinners()
        addOnChangeListeners()
        readLastInputs()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        if (!controlService.isFileAvailableForReading()) menu?.findItem(R.id.resultListIcon)?.isVisible = false
        listResultMenuItem = menu?.findItem(R.id.resultListIcon)!!
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.resultListIcon -> {
            startActivity(Intent(applicationContext, ResultListActivity::class.java))
            true
        }

        else -> {
            super.onOptionsItemSelected(item)
        }
    }


    private fun updateSpinners() {
        yearSpinner = findViewById(R.id.yearSpinner)
        ArrayAdapter.createFromResource(this, R.array.year_array, android.R.layout.simple_spinner_dropdown_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            yearSpinner.adapter = adapter
        }
        monthSpinner = findViewById(R.id.monthSpinner)
        ArrayAdapter.createFromResource(this, R.array.monthArray, android.R.layout.simple_spinner_dropdown_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            monthSpinner.adapter = adapter
        }
        yearSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                goToRecalculationState()
            }
        }
        monthSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                goToRecalculationState()
            }
        }
    }

    private fun addOnChangeListeners() {
        genderRadioGroup.setOnCheckedChangeListener { group, checkedId -> goToRecalculationState() }
        ageRadioGroup.setOnCheckedChangeListener { group, checkedId -> goToRecalculationState() }
        weightEditText.addTextChangedListener { goToRecalculationState() }
        heightEditText.addTextChangedListener { goToRecalculationState() }
        pickedDateTexView.addTextChangedListener { goToRecalculationState() }
        centileBmiTexView.addTextChangedListener { setOnTextViewChangeColor(centileBmiTextView) }
        centileWeightTexView.addTextChangedListener { setOnTextViewChangeColor(centileWeightTextView) }
        centileHeightTexView.addTextChangedListener { setOnTextViewChangeColor(centileHeightTextView) }
    }

    private fun goToRecalculationState() {
        if (calculateButton.visibility == View.GONE) {
            saveButton.visibility = View.GONE
            calculateButton.visibility = View.VISIBLE
        }
    }

    private fun setOnTextViewChangeColor(resultTextView: TextView) {
        val color = if (resources.getStringArray(R.array.resultsOK).toList().any { resultTextView.text.contains(it) })
            defaultTextViewColor else ContextCompat.getColor(applicationContext, R.color.colorAccent)
        resultTextView.setTextColor(color)
    }

    private fun readLastInputs() {
        val inputs = this.getPreferences(Context.MODE_PRIVATE) ?: return
        genderRadioGroup.check(inputs.getInt(R.id.genderRadioGroup.toString(), findViewById<RadioButton>(R.id.GIRL).id))
        ageRadioGroup.check(inputs.getInt(R.id.ageRadioGroup.toString(), findViewById<RadioButton>(R.id.monthYears).id))
        childNameEditText.setText(inputs.getString(R.id.childNameEditText.toString(), ""))
        if (inputs.getInt(R.id.ageRadioGroup.toString(), 0) == findViewById<RadioButton>(R.id.birthDate).id) {
            switchAgeInput()
            pickedDateTexView.text = inputs.getString(R.id.pickedDateTextView.toString(), "")
        } else {
            yearSpinner.setSelection(applicationContext.resources.getTextArray(R.array.year_array)
                .indexOfFirst { year -> year == inputs.getString(R.id.yearSpinner.toString(), "0") })
            monthSpinner.setSelection(applicationContext.resources.getTextArray(R.array.monthArray)
                .indexOfFirst { month -> month == inputs.getString(R.id.monthSpinner.toString(), "0") })
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
        val pickedRadioGenderId = genderRadioGroup.checkedRadioButtonId
        val genderRadioButton = findViewById<RadioButton>(pickedRadioGenderId)
        val age = getAge()
        val weightText = weightEditText.text.toString()
        val heightText = heightEditText.text.toString()
        val weight = if (weightText.isNotEmpty()) weightText.toDouble() else 0.0
        val height = if (heightText.isNotEmpty()) heightText.toDouble() else 0.0
        val gender = Gender.valueOf(genderRadioButton.hint.toString())

        closeKeyboard(view)
        val wrongInputs = mapOf(getString(R.string.age) to age.toDouble(), getString(R.string.weight) to weight, getString(R.string.height) to height)
            .filter { it.value == 0.0  }
        if (wrongInputs.isEmpty()) {
            publishCentile(gender, age, weight, height)
        } else {
            Toast.makeText(applicationContext, getString(R.string.fillFields).plus(wrongInputs.keys.joinToString(", ")), Toast.LENGTH_LONG).show()
        }
    }

    private fun publishCentile(gender: Gender, age: Int, weight: Double, height: Double) {
        val bmi = centileService.getBmi(weight, height)
        BMITextView.text = getString(R.string.BMI).plus(": ").plus("%.2f".format(bmi))
        centileWeightTexView.text = centileService.formatCentile(getString(R.string.centileWeight), gender, age, Type.WEIGHT to weight)
        centileHeightTexView.text = centileService.formatCentile(getString(R.string.centileHeight), gender, age, Type.HEIGHT to height)
        centileBmiTexView.text = centileService.formatCentile(getString(R.string.centileBmi), gender, age, Type.BMI to bmi)
        saveInputs()
        saveButton.visibility = View.VISIBLE
        calculateButton.visibility = View.GONE
        bmiTexView.visibility = View.VISIBLE
        centileHeightTexView.visibility = View.VISIBLE
        centileWeightTexView.visibility = View.VISIBLE
        centileBmiTexView.visibility = View.VISIBLE
    }

    private fun saveInputs() {
        val initialData = this.getPreferences(Context.MODE_PRIVATE) ?: return
        with (initialData.edit()) {
            putInt(R.id.genderRadioGroup.toString(), genderRadioGroup.checkedRadioButtonId)
            putInt(R.id.ageRadioGroup.toString(), ageRadioGroup.checkedRadioButtonId)
            putString(R.id.childNameEditText.toString(), childNameEditText.text.toString())
            putString(R.id.pickedDateTextView.toString(), pickedDateTexView.text.toString())
            putString(R.id.yearSpinner.toString(), yearSpinner.selectedItem.toString())
            putString(R.id.monthSpinner.toString(), monthSpinner.selectedItem.toString())
            commit()
        }
    }

    private fun closeKeyboard(view: View) {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun getAge(): Int = if (findViewById<RadioButton>(R.id.monthYears).isChecked) {
        val months = monthSpinner.selectedItem.toString().toInt()
        val years = yearSpinner.selectedItem.toString().toInt()
        years * 12 + months
    } else {
        countMonths()
    }

    private fun countMonths(): Int {
        var date = pickedDateTexView.text.toString()
        if (date.isBlank()) date = getToday(false)
        val birthDate = SimpleDateFormat("dd.MM.yyyy").parse(date).time
        val days = Date().time.minus(birthDate) / (1000 * 3600 * 24)
        return (days / 30.5f).roundToInt() // average days in months
    }

    private fun getToday(startWithYear: Boolean): String {
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR).toString()
        val month = if (c.get(Calendar.MONTH) + 1 > 9) (c.get(Calendar.MONTH) + 1).toString() else "0".plus((c.get(Calendar.MONTH) + 1).toString())
        val day = if (c.get(Calendar.DAY_OF_MONTH) > 9) c.get(Calendar.DAY_OF_MONTH).toString() else "0".plus(c.get(Calendar.DAY_OF_MONTH).toString())
        val separator = "."
        return if (startWithYear) year.plus(separator).plus(month).plus(separator).plus(day) else day.plus(separator).plus(month).plus(separator).plus(year)
    }

    fun saveResults(view: View) {
        val pickedRadioGenderId = genderRadioGroup.checkedRadioButtonId
        val results = arrayOf(
            findViewById<RadioButton>(pickedRadioGenderId).hint.toString(),
            getAge().toString(),
            childNameEditText.text.toString(),
            getToday(true),
            weightEditText.text.toString(),
            heightEditText.text.toString()
            )

        try {
            controlService.saveControlResults(results)
            saveResultsButton.visibility = View.GONE
            closeKeyboard(view)
            listResultMenuItem.isVisible = true
            Toast.makeText(applicationContext, R.string.resultsSaved, LENGTH_SHORT).show()
        } catch (e: IOException) {
            Toast.makeText(applicationContext, R.string.resultsNotSaved, LENGTH_SHORT).show()
        }
    }

    fun showDatePickerDialog(view: View) {
        val datePicker = DatePickerFragment()
        datePicker.show(supportFragmentManager, "datePicker")
    }
}
