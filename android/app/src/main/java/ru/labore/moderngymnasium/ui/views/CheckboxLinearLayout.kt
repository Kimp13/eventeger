package ru.labore.moderngymnasium.ui.views

import android.content.Context
import android.view.View
import android.widget.LinearLayout

class CheckboxLinearLayout(
    context: Context,
    private val parent: ParentCheckbox
) : LinearLayout(context) {
    init {
        orientation = VERTICAL
        setPadding(40, 10, 0, 10)
        layoutParams = LayoutParams(
            LayoutParams.MATCH_PARENT,
            LayoutParams.WRAP_CONTENT
        )
    }

    override fun onViewAdded(child: View?) {
        super.onViewAdded(child)
        parent.onViewToLayoutAdded(child)
    }
}