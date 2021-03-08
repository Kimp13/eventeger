package ru.labore.moderngymnasium.ui.views

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.TextView
import ru.labore.moderngymnasium.R

class LabelledCheckbox(
    context: Context,
    labelText: String,
    specialLayoutParams: ViewGroup.LayoutParams? = null
) : LinearLayout(context) {
    private val checkbox: CheckBox
    private val label: TextView
    var innerCheckedChangeHandler: ((Boolean) -> Unit) = {}
    var outerCheckedChangeHandler: ((Boolean) -> Unit) = {}
    var isChecked: Boolean = true
        set(value) {
            field = value
            checkbox.isChecked = value
            outerCheckedChangeHandler(value)
        }

    init {
        layoutParams = specialLayoutParams
            ?: LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
            )

        orientation = HORIZONTAL

        label = LayoutInflater
            .from(context)
            .inflate(
                R.layout.labelled_checkbox_label,
                this,
                false
            ) as TextView
        checkbox = LayoutInflater
            .from(context)
            .inflate(
                R.layout.labelled_checkbox,
                this,
                false
            ) as CheckBox

        label.text = labelText

        addView(label)
        addView(checkbox)

        checkbox.setOnClickListener { view ->
            innerCheckedChangeHandler((view as CheckBox).isChecked)
            outerCheckedChangeHandler(view.isChecked)
        }
    }
}