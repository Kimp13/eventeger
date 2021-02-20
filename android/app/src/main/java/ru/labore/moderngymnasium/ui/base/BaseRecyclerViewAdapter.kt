package ru.labore.moderngymnasium.ui.base

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

abstract class BaseRecyclerViewAdapter
    : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        companion object Base {
            const val DEFAULT_VIEW_POSITION = -1
        }

    val additionalItems = arrayListOf<(ViewGroup) -> RecyclerView.ViewHolder>()

    protected abstract fun updateAdditionalItems()
    protected abstract fun createDefaultViewHolder(
        parent: ViewGroup
    ): BaseViewHolder

    override fun getItemViewType(position: Int): Int =
        if (position < additionalItems.size)
            position
        else
            DEFAULT_VIEW_POSITION

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder =
        if (viewType < additionalItems.size && viewType >= 0)
            additionalItems[viewType](parent)
        else
            createDefaultViewHolder(parent)

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as BaseViewHolder).onBind(position, this)
    }
}