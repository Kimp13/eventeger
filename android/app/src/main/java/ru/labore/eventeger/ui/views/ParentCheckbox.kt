package ru.labore.eventeger.ui.views

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout

class ParentCheckbox(
    context: Context,
    labelText: String,
    specialLayoutParams: ViewGroup.LayoutParams? = null
) : LinearLayout(context) {
    companion object {
        const val INDETERMINATE = -1
        const val UNCHECKED = 0
        const val CHECKED = 1
    }

    private val checkbox: LabelledCheckbox
    val checkboxLayout: CheckboxLinearLayout

    var checkedChangeHandler: ((Int) -> Unit)? = null
    private val childrenStates = ArrayList<Int>()
    private var state = UNCHECKED
        set(value) {
            if (field != value) {
                field = value
                updateCheckbox(updateState = false, notify = false)
            }
        }

    init {
        layoutParams = specialLayoutParams
            ?: LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
            )

        orientation = VERTICAL

        checkbox = LabelledCheckbox(context, labelText)
        checkboxLayout = CheckboxLinearLayout(context, this)

        addView(checkbox)
        addView(checkboxLayout)

        checkbox.innerCheckedChangeHandler = {
            state = if (it) CHECKED else UNCHECKED
            updateCheckbox(false)
        }
    }

    fun onViewToLayoutAdded(child: View?) {
        val index = childrenStates.size
        childrenStates.add(UNCHECKED)

        if (child is ParentCheckbox) {
            child.checkedChangeHandler = { childState ->
                childrenStates[index] = childState

                updateStateByChildren()
            }
        } else if (child is LabelledCheckbox) {
            child.innerCheckedChangeHandler = { childState ->
                childrenStates[index] =
                    if (childState)
                        CHECKED
                    else
                        UNCHECKED

                updateStateByChildren()
            }
        }

        updateChildren()
    }

    private fun updateStateByChildren() {
        var checkedCount = 0
        var indeterminateCount = 0

        for (i in 0 until childrenStates.size) {
            if (childrenStates[i] == CHECKED) {
                checkedCount += 1
            } else if (childrenStates[i] == INDETERMINATE) {
                indeterminateCount += 1
            }
        }

        val newState = when {
            checkedCount == childrenStates.size -> CHECKED
            checkedCount > 0 || indeterminateCount > 0 -> INDETERMINATE
            else -> UNCHECKED
        }

        if (newState != state) {
            state = newState
            checkedChangeHandler?.invoke(state)
        }
    }

    private fun updateCheckbox(updateState: Boolean = true, notify: Boolean = true) {
        if (updateState) {
            state = if (state == CHECKED) UNCHECKED else CHECKED
        }

        checkbox.isChecked = state == CHECKED

        if (state == CHECKED) {
            checkbox.isChecked = true

            checkboxLayout.visibility = View.GONE
        } else {
            checkbox.isChecked = false
            checkboxLayout.visibility = View.VISIBLE
        }

        if (notify) {
            checkedChangeHandler?.invoke(state)
        }

        updateChildren()
    }

    private fun updateChildren() {
        if (state == CHECKED) {
            for (i in 0 until checkboxLayout.childCount) {
                childrenStates[i] = CHECKED

                val child = checkboxLayout.getChildAt(i)

                if (child is LabelledCheckbox) {
                    child.isChecked = true
                } else if (child is ParentCheckbox) {
                    child.state = CHECKED
                    child.updateChildren()
                }
            }
        } else if (state == UNCHECKED) {
            for (i in 0 until checkboxLayout.childCount) {
                childrenStates[i] = UNCHECKED

                val child = checkboxLayout.getChildAt(i)

                if (child is LabelledCheckbox) {
                    child.isChecked = false
                } else if (child is ParentCheckbox) {
                    child.state = UNCHECKED
                    child.updateChildren()
                }
            }
        }
    }
}
