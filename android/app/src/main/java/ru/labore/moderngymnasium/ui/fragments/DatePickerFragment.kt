package ru.labore.moderngymnasium.ui.fragments

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.DatePicker
import androidx.fragment.app.DialogFragment
import java.util.*

class DatePickerFragment(
    changeListener: (Int, Int, Int) -> Unit = { _, _, _ -> }
) : DialogFragment(), DatePickerDialog.OnDateSetListener {
    var years: Int
    var months: Int
    var days: Int
    var dateChangeListener: (Int, Int, Int) -> Unit

    init {
        val calendar = Calendar.getInstance()
        years = calendar.get(Calendar.YEAR)
        months = calendar.get(Calendar.MONTH)
        days = calendar.get(Calendar.DAY_OF_MONTH)
        dateChangeListener = changeListener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val cnt = context

        return if (cnt != null)
            DatePickerDialog(
                cnt,
                this,
                years,
                months,
                days
            )
        else
            super.onCreateDialog(savedInstanceState)
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        dateChangeListener(year, month, dayOfMonth)
    }
}