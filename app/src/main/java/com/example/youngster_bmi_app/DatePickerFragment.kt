package com.example.youngster_bmi_app

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.DatePicker
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import java.util.*

class DatePickerFragment : DialogFragment(), DatePickerDialog.OnDateSetListener {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)
        return DatePickerDialog(this.requireContext(), this, year, month, day)
    }

    override fun onDateSet(view: DatePicker, year: Int, month: Int, day: Int) {
        val date = activity?.findViewById<TextView>(R.id.pickedDate)?.apply {
            text = (if (day < 10) "0".plus(day.toString()) else day.toString())
                .plus(if (month + 1 < 10) ".0" else ".").plus((month + 1).toString())
                .plus(".").plus(year.toString())
        }
    }
}
