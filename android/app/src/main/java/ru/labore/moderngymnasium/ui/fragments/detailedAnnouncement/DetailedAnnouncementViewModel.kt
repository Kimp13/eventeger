package ru.labore.moderngymnasium.ui.fragments.detailedAnnouncement

import android.app.Application
import kotlinx.coroutines.Job
import ru.labore.moderngymnasium.data.AppRepository
import ru.labore.moderngymnasium.data.db.entities.AnnouncementEntity
import ru.labore.moderngymnasium.data.db.entities.CommentEntity
import ru.labore.moderngymnasium.ui.adapters.DetailedAnnouncementRecyclerViewAdapter
import ru.labore.moderngymnasium.ui.adapters.InboxRecyclerViewAdapter
import ru.labore.moderngymnasium.ui.base.BaseViewModel
import ru.labore.moderngymnasium.ui.base.ListElementFragment

class DetailedAnnouncementViewModel(
    private val app: Application
) : BaseViewModel(app) {
    private lateinit var viewAdapter: DetailedAnnouncementRecyclerViewAdapter
    private lateinit var announcement: AnnouncementEntity
    private var currentOffset = 0
    val itemCount
        get() = comments.size

    private var current: Job? = null
    private var reachedEnd = false
    private val comments = mutableListOf<CommentEntity>()

    fun getAdapter(
        controls: ListElementFragment.Companion.ListElementFragmentControls,
        currentAnnouncement: AnnouncementEntity
    ): DetailedAnnouncementRecyclerViewAdapter {
        announcement = currentAnnouncement

        viewAdapter = DetailedAnnouncementRecyclerViewAdapter(
            app.resources,
            appRepository,
            announcement,
            comments
        )

        return viewAdapter
    }

    private suspend fun getComments(
        offset: Int,
        forceFetch: AppRepository.Companion.UpdateParameters
    ) = appRepository.getComments(announcement.id, offset, forceFetch)
}