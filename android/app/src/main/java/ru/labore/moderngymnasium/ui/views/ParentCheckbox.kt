package ru.labore.moderngymnasium.ui.views

import android.content.Context
import android.util.AttributeSet
import kotlinx.android.synthetic.main.activity_login.view.*
import ru.labore.moderngymnasium.R

class ParentCheckbox : androidx.appcompat.widget.AppCompatCheckBox {
    companion object {
        private const val UNKNOWN = -1
        private const val UNCHECKED = 0
        private const val CHECKED = 1
    }

    private var state = 0
        set(value) {
            field = value
            updateButton()
        }

    constructor(context: Context): super(context) {
        init()
    }

    constructor(context: Context, attributeSet: AttributeSet):
            super(context, attributeSet) {
        init()
    }

    constructor(context: Context, attributeSet: AttributeSet, defStyleAttr: Int):
        super(context, attributeSet, defStyleAttr) {
        init()
    }

    private fun init() {
        state = UNKNOWN
        updateButton()
        
        setOnCheckedChangeListener { _, _ ->
            state = when(state) {
                UNKNOWN -> UNCHECKED
                UNCHECKED -> CHECKED
                else -> UNKNOWN
            }

            updateButton()
        }
    }

    private fun updateButton() {
        
    }
}