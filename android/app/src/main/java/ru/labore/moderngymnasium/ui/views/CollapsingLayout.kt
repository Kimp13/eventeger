package ru.labore.moderngymnasium.ui.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.children
import ru.labore.moderngymnasium.R

class CollapsingLayout(
    context: Context,
    attrs: AttributeSet
) : LinearLayout(context) {
    private val toolbar: ConstraintLayout
    private var collapsed = false

    init {
        orientation = LinearLayout.VERTICAL

        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.CollapsingLayout,
            0,
            0
        ).apply {
            try {
                val label = getString(
                    R.styleable.CollapsingLayout_label
                ) ?: "CollapsingLayout"

                toolbar = LayoutInflater.from(context).inflate(
                    R.layout.collapsing_layout,
                    this@CollapsingLayout,
                    false
                ) as ConstraintLayout

                toolbar.layoutParams = LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )

                (toolbar.getChildAt(0) as TextView).text = label

                toolbar.setOnClickListener {
                    if (collapsed) {
                        expandContent() // Made in functions for convenience
                        unrotateShevron() // AND kind of encapsulation! ^_^
                    } else {
                        collapseContent() // ditto
                        rotateShevron() // ditto
                    }

                    collapsed = !collapsed
                }

                addView(toolbar, 0)
            } finally {
                recycle()
            }
        }
    }

    /**
     * Expand all children except the toolbar
     *
     * TODO animation
     */
    private fun expandContent() {
        children.forEach {
            println(it)
        }

        for (i in 1 until childCount) {
            getChildAt(i).visibility = View.VISIBLE
        }
    }

    /**
     * Collapse all children except the toolbar
     *
     * TODO animation
     */
    private fun collapseContent() {
        for (i in 1 until childCount) {
            getChildAt(i).visibility = View.GONE
        }
    }

    /**
     * Rotate shevron indicator by 180°
     *
     * TODO animation
     */
    private fun rotateShevron() {
        toolbar.getChildAt(1).rotation = 180F
    }

    /**
     * Rotate shevron back to 0°
     *
     * TODO animation
     */
    private fun unrotateShevron() {
        toolbar.getChildAt(1).rotation = 0F
    }
}