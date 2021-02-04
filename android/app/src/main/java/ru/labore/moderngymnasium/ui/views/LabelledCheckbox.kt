package ru.labore.moderngymnasium.ui.views

import android.content.Context
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.TextView

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

        label = TextView(context)
        checkbox = CheckBox(context)
        label.layoutParams = LayoutParams(
            0,
            LayoutParams.WRAP_CONTENT,
            1F
        )
        checkbox.layoutParams = LayoutParams(
            LayoutParams.WRAP_CONTENT,
            LayoutParams.WRAP_CONTENT,
            0F
        )

        label.text = labelText

        addView(label)
        addView(checkbox)

        checkbox.setOnClickListener { view ->
            innerCheckedChangeHandler((view as CheckBox).isChecked)
            outerCheckedChangeHandler(view.isChecked)
        }
    }
}