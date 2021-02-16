package ru.labore.moderngymnasium.ui.base

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import ru.labore.moderngymnasium.ui.interfaces.InterfaceViewHolder

abstract class BaseViewHolder(
    view: View
) : RecyclerView.ViewHolder(view) {
    abstract fun onBind(
        position: Int,
        parent: BaseRecyclerViewAdapter
    ): Unit
}