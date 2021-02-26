package ru.labore.moderngymnasium.ui.base

import android.app.Application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import ru.labore.moderngymnasium.data.db.entities.AuthoredEntity

abstract class BaseRecyclerViewModel(
    val app: Application
) : BaseViewModel(app) {
    protected lateinit var adapter: BaseRecyclerViewAdapter
    protected var currentOffset = 0
    protected var current: Job? = null
    protected var reachedEnd = false

    protected var loading: Boolean
        get() =
            adapter.loading
        set(value) {
            MainScope().launch {
                if (adapter.loading != value)
                    adapter.loading = value
            }
        }

    protected fun refreshItems(
        previousSize: Int,
        itemCount: Int
    ) {
        MainScope().launch {
            adapter.refreshItems(previousSize, itemCount)
            loading = false
        }
    }

    protected fun pushItems(
        previousSize: Int,
        itemCount: Int
    ) {
        MainScope().launch {
            adapter.pushItems(previousSize, itemCount)
            loading = false
        }
    }

    val items = mutableListOf<AuthoredEntity>()

    val itemCount
        get() = items.size
}