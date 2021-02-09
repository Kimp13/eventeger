package ru.labore.moderngymnasium.ui.views

import android.animation.Animator
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
import androidx.core.view.doOnLayout
import ru.labore.moderngymnasium.R

open class CollapsingLayout(
    context: Context,
    attrs: AttributeSet
) : LinearLayout(context) {
    protected val toolbar: ConstraintLayout
    protected var collapsed = false
    private val animator = ValueAnimator()
    private val params = layoutParams ?: LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.WRAP_CONTENT
    )

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

    init {
        orientation = LinearLayout.VERTICAL

        toolbar = inflateToolbar()

        doOnAttach {
            toolbar.layoutParams = LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )

            initializeToolbar()

            addView(toolbar, 0)

            animator.duration = 300
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

    /**
     * Expand all children except the toolbar
     */
    protected fun expandContent() {
        if (!animator.isRunning)
            for (i in 1 until childCount)
                getChildAt(i).visibility = View.VISIBLE

        this.measure(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        val height = this.measuredHeight

        animator.removeAllListeners()
        animator.setIntValues(this.height, height)
        animator.addUpdateListener {
            params.height = it.animatedValue as Int

            layoutParams = params
        }

        // Non-Kotlin way, but ??? it doesn't work Kotlin way
        animator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator?) {
            }

            override fun onAnimationEnd(animation: Animator?) {
                params.height = ViewGroup.LayoutParams.WRAP_CONTENT

                layoutParams = params
            }

            override fun onAnimationCancel(animation: Animator?) {
            }

            override fun onAnimationRepeat(animation: Animator?) {
            }
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

        // Non-Kotlin way, but ??? it doesn't work Kotlin way
        animator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator?) {
            }

            override fun onAnimationEnd(animation: Animator?) {
                for (i in 1 until childCount)
                    getChildAt(i).visibility = View.GONE
            }

            override fun onAnimationCancel(animation: Animator?) {
            }

            override fun onAnimationRepeat(animation: Animator?) {
            }
        })

        animator.start()
    }

    /**
     * Rotate shevron indicator by 180°
     *
     * TODO animation
     */
    protected fun rotateShevron() {
        toolbar.children.last().rotation = 180F
    }

    /**
     * Rotate shevron back to 0°
     *
     * TODO animation
     */
    protected fun rotateShevronBack() {
        toolbar.children.last().rotation = 0F
    }
}