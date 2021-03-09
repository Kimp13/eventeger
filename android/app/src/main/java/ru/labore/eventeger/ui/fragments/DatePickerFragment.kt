package ru.labore.eventeger.ui.fragments

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.DatePicker
import androidx.fragment.app.DialogFragment
import ru.labore.eventeger.R
import java.util.*

class DatePickerFragment(
    changeListener: (Int, Int, Int) -> Unit = { _, _, _ -> }
) : DialogFragment(), DatePickerDialog.OnDateSetListener {
    var years: Int
    var months: Int
    var days: Int
    var minDate: Long = 0
        set(value) {
            field = value

            val dlg = dialog as DatePickerDialog?

            if (dlg != null)
                dlg.datePicker.minDate = value
        }

    var maxDate: Long = Long.MAX_VALUE
        set(value) {
            field = value

            val dlg = dialog as DatePickerDialog?

            if (dlg != null)
                dlg.datePicker.maxDate = value
        }

    var dateChangeListener: (Int, Int, Int) -> Unit

    init {
        val calendar = Calendar.getInstance()
        years = calendar.get(Calendar.YEAR)
        months = calendar.get(Calendar.MONTH)
        days = calendar.get(Calendar.DAY_OF_MONTH)
        dateChangeListener = changeListener
    }

    fun updateDate(year: Int, month: Int, day: Int) {
        years = year
        months = month - 1
        days = day

        (dialog as DatePickerDialog?)?.updateDate(years, months, days)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val cnt = context

        return if (cnt != null) {
            println("$years, $months, $days")
            val dialog = DatePickerDialog(
                cnt,
                R.style.DatePickerStyle,
                this,
                years,
                months,
                days
            )

            dialog.datePicker.minDate = minDate
            dialog.datePicker.maxDate = maxDate

            dialog
        } else {
            super.onCreateDialog(savedInstanceState)
        }
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        dateChangeListener(year, month, dayOfMonth)
    }
}