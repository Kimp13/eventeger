package ru.labore.moderngymnasium.ui.views

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.animation.addListener
import androidx.core.view.children
import androidx.core.view.doOnAttach
import ru.labore.moderngymnasium.R

open class CollapsingLayout(
    context: Context,
    attrs: AttributeSet
) : LinearLayout(context, attrs) {
    companion object {
        const val ANIMATION_DURATION = 300L
    }

    protected val toolbar: ConstraintLayout
    protected var collapsed = false
    private val animator = ValueAnimator()
    private val shevronAnimator = ObjectAnimator()
    protected val childrenLayout = LinearLayout(context)
    private val params = layoutParams ?: LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.WRAP_CONTENT
    )

    init {
        orientation = VERTICAL

        // Children
        toolbar = inflateToolbar()
        childrenLayout.layoutParams = params
        childrenLayout.orientation = VERTICAL

        // Animators
        shevronAnimator.target = toolbar.children.last()
        shevronAnimator.setPropertyName("rotation")
        shevronAnimator.duration = ANIMATION_DURATION
        animator.duration = ANIMATION_DURATION

        doOnAttach {
            toolbar.layoutParams = LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )

            initializeToolbar()

            super.addView(toolbar)
            super.addView(childrenLayout)
        }

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

                (toolbar.getChildAt(0) as TextView).text = label
            } finally {
                recycle()
            }
        }
    }

    protected open fun inflateToolbar() =
        LayoutInflater
            .from(context)
            .inflate(
                R.layout.collapsing_layout,
                this,
                false
            ) as ConstraintLayout

    protected open fun initializeToolbar() {
        toolbar.setOnClickListener {
            if (collapsed) {
                expandContent() // Made in functions for convenience
                rotateShevronBack() // AND kind of encapsulation! ^_^
            } else {
                collapseContent() // ditto - a new fancy word
                rotateShevron() // ditto
            }

            collapsed = !collapsed
        }
    }

    override fun addView(child: View?, params: ViewGroup.LayoutParams?) {
        childrenLayout.addView(child, params)
    }

    /**
     * Expand all children except the toolbar
     */
    protected fun expandContent() {
        if (!animator.isRunning)
            childrenLayout.visibility = View.VISIBLE

        this.measure(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        val height = this.measuredHeight

        animator.removeAllListeners()
        animator.setIntValues(this.height, height)
        animator.addUpdateListener {
            params.height = it.animatedValue as Int

            layoutParams = params
        }

        animator.addListener({
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT

            layoutParams = params
        })

        animator.start()
    }

    /**
     * Collapse all children except the toolbar
     */
    protected fun collapseContent() {
        animator.removeAllListeners()
        animator.setIntValues(this.height, toolbar.height)
        animator.addUpdateListener {
            params.height = it.animatedValue as Int

            layoutParams = params
        }

        animator.addListener({
            childrenLayout.visibility = View.GONE
        })

        animator.start()
    }

    /**
     * Rotate shevron indicator by 180°
     */
    protected fun rotateShevron() {
        val start = if (shevronAnimator.isRunning)
            180F * (
                    1 - shevronAnimator.currentPlayTime.toFloat() /
                            ANIMATION_DURATION.toFloat()
                    )
        else
            0F

        shevronAnimator.cancel()
        shevronAnimator.setFloatValues(start, 180F)
        shevronAnimator.start()
    }

    /**
     * Rotate shevron back to 0°
     */
    protected fun rotateShevronBack() {
        val start = if (shevronAnimator.isRunning)
            180F * (
                    shevronAnimator.currentPlayTime.toFloat() /
                            ANIMATION_DURATION.toFloat()
                    )
        else
            180F

        shevronAnimator.cancel()
        shevronAnimator.setFloatValues(start, 0F)
        shevronAnimator.start()
    }
}