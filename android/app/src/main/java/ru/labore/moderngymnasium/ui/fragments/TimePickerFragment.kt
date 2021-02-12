package ru.labore.moderngymnasium.ui.fragments

import android.app.Dialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.TimePicker
import androidx.fragment.app.DialogFragment
import java.util.*

class TimePickerFragment(
    changeListener: (Int, Int) -> Unit = { _, _ -> }
) : DialogFragment(), TimePickerDialog.OnTimeSetListener {
    var hours: Int
    var minutes: Int
    var is24HourFormat: Boolean
    var timeChangeListener: (Int, Int) -> Unit

    init {
        val calendar = Calendar.getInstance()
        hours = calendar.get(Calendar.HOUR_OF_DAY)
        minutes = calendar.get(Calendar.MINUTE)
        is24HourFormat = true
        timeChangeListener = changeListener
    }

    fun updateTime(hour: Int, minute: Int) =
        (dialog as TimePickerDialog?)?.updateTime(hour, minute)

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return TimePickerDialog(
            activity,
            this,
            hours,
            minutes,
            is24HourFormat
        )
    }

    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
        timeChangeListener(hourOfDay, minute)
    }
}