package ru.labore.moderngymnasium.ui.fragments.inbox

import android.app.Activity
import android.app.Application
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import ru.labore.moderngymnasium.data.AppRepository
import ru.labore.moderngymnasium.data.db.entities.AnnouncementEntity
import ru.labore.moderngymnasium.ui.adapters.InboxRecyclerViewAdapter
import ru.labore.moderngymnasium.ui.base.BaseViewModel
import ru.labore.moderngymnasium.ui.base.ListElementFragment
import ru.labore.moderngymnasium.ui.fragments.create.CreateFragment
import ru.labore.moderngymnasium.ui.fragments.detailedAnnouncement.DetailedAnnouncementFragment

class InboxViewModel(
    private val app: Application
) : BaseViewModel(app) {
    private lateinit var viewAdapter: InboxRecyclerViewAdapter
    private var currentOffset = 0
    val itemCount
        get() = announcements.size

    private var loading: Boolean
        get() =
            viewAdapter.loading
        set(value) {
            viewAdapter.loading = value
        }

    private var current: Job? = null
    private var reachedEnd = false
    private val announcements = mutableListOf<AnnouncementEntity>()

    init {
        appRepository.unreadAnnouncementsPushListener = {
            announcements.add(viewAdapter.beginAdditionalItems.size, it)
            viewAdapter.prependAnnouncement()
        }
    }

    fun getAdapter(
        controls: ListElementFragment.Companion.ListElementFragmentControls
    ): InboxRecyclerViewAdapter {
        viewAdapter = InboxRecyclerViewAdapter(
            app.resources,
            appRepository,
            announcements,
            {
                controls.push(CreateFragment(controls))
            },
            {
                controls.push(DetailedAnnouncementFragment(controls, it))
            }
        )

        return viewAdapter
    }

    suspend fun updateAnnouncements(
        activity: Activity,
        forceFetch: AppRepository.Companion.UpdateParameters =
            AppRepository.Companion.UpdateParameters.DETERMINE,
        refresh: Boolean = false
    ) {
        loading = true

        if (current == null || !current!!.isActive) {
            if (refresh || !reachedEnd) {
                val offset = if (refresh) {
                    0
                } else {
                    currentOffset
                }

                val newAnnouncements = hashMapOf<Int, AnnouncementEntity>()

                current = GlobalScope.async {
                    makeRequest(
                        activity,
                        {
                            getAnnouncements(offset, forceFetch).forEach {
                                newAnnouncements[it.id] = it
                            }
                        },
                        {
                            getAnnouncements(
                                offset,
                                AppRepository.Companion.UpdateParameters.DONT_UPDATE
                            ).forEach {
                                newAnnouncements[it.id] = it
                            }
                        }
                    )
                }

                current?.join()

                if (refresh) {
                    val previousSize = itemCount

                    currentOffset = 0
                    reachedEnd = false
                    announcements.clear()
                    announcements.addAll(newAnnouncements.values)

                    viewAdapter.refreshAnnouncements(
                        previousSize,
                        itemCount
                    )
                } else {
                    if (newAnnouncements.isEmpty()) {
                        reachedEnd = true
                    } else {
                        val iterator = announcements.listIterator()

                        while (iterator.hasNext()) {
                            val it = iterator.next()
                            val newValue = newAnnouncements[it.id]

                            if (newValue != null) {
                                iterator.set(newValue)
                                newAnnouncements.remove(it.id)
                            }
                        }

                        val previousSize = itemCount

                        currentOffset += newAnnouncements.size
                        announcements.addAll(newAnnouncements.values)

                        viewAdapter.pushAnnouncements(
                            previousSize,
                            itemCount
                        )
                    }
                }
            }
        } else {
            current?.join()
        }

        loading = false
    }

    suspend fun setup(activity: Activity) {
        if (itemCount == 0)
            updateAnnouncements(
                activity,
                AppRepository.Companion.UpdateParameters.DETERMINE,
                true
            )
        else
            loading = false
    }

    private suspend fun getAnnouncements(
        offset: Int,
        forceFetch: AppRepository.Companion.UpdateParameters
    ) =
        appRepository.getAnnouncements(offset, forceFetch)
}