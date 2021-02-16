package ru.labore.moderngymnasium.ui.views

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.widget.SwitchCompat
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.children
import androidx.core.view.doOnLayout
import ru.labore.moderngymnasium.R

class CollapsingSwitchLayout(
    context: Context,
    attrs: AttributeSet
) : CollapsingLayout(context, attrs) {
    companion object {
        const val SWITCHED_OFF_ALPHA = .6F
    }

    private lateinit var switch: SwitchCompat
    var switchListener: ((Boolean) -> Unit)? = null

    override fun inflateToolbar(): ConstraintLayout {
        return LayoutInflater
            .from(context)
            .inflate(
                R.layout.collapsing_switch_layout,
                this,
                false
            ) as ConstraintLayout
    }

    private fun expand() {
        collapsed = false

        expandContent()
        rotateShevronBack()
    }

    private fun collapse() {
        collapsed = true

        collapseContent()
        rotateShevron()
    }

    private fun switchListener(
        isChecked: Boolean = switch.isChecked
    ) {
        switchListener?.invoke(isChecked)

        if (isChecked) {
            toolbar.children.last().alpha = 1F

            if (collapsed)
                expand()
        } else {
            toolbar.children.last().alpha = SWITCHED_OFF_ALPHA

            if (!collapsed)
                collapse()
        }
    }

    override fun initializeToolbar() {
        toolbar.setOnClickListener {
            if (switch.isChecked) {
                if (collapsed)
                    expand()
                else
                    collapse()
            }
        }

        switch = toolbar.getChildAt(1) as SwitchCompat

        switch.setOnCheckedChangeListener { _, isChecked ->
            switchListener(isChecked)
        }
    }

    init {
        doOnLayout {
            switchListener()
        }
    }
}