package com.example.youngster_bmi_app

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import java.io.File

class YesNoDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            builder.setMessage(R.string.deleteQuestion)
                .setPositiveButton(R.string.yesAnswer) { _, _ ->
                    run {
                        File(activity!!.filesDir, getString(R.string.resultsFile)).delete()
                        activity!!.finish()
                    }
                }
                .setNegativeButton(R.string.noAnswer) { _, _ -> dismiss() }
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}