package ru.labore.moderngymnasium.ui.fragments.inbox

import android.app.Application
import android.content.Intent
import android.os.Bundle
import ru.labore.moderngymnasium.data.repository.AppRepository
import ru.labore.moderngymnasium.ui.activities.AnnouncementDetailedActivity
import ru.labore.moderngymnasium.ui.activities.CreateActivity
import ru.labore.moderngymnasium.ui.adapters.InboxRecyclerViewAdapter
import ru.labore.moderngymnasium.ui.base.BaseViewModel

class InboxViewModel(
    application: Application
) : BaseViewModel(application) {
    private val viewAdapter = InboxRecyclerViewAdapter(
        application.resources,
        {
            val intent = Intent(
                application.applicationContext,
                CreateActivity::class.java
            )

            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK

            application.startActivity(intent)
        },
        {
            val intent = Intent(
                application.applicationContext,
                AnnouncementDetailedActivity::class.java
            )
            val bundle = Bundle()
            bundle.putParcelable("announcement", it)
            intent.putExtras(bundle)
            application.startActivity(intent)
        }
    )
    val itemCount
        get() = viewAdapter.announcements.size


    private var reachedEnd = false

    init {
        appRepository.unreadAnnouncementsPushListener = {
            viewAdapter.prependAnnouncement(it)
        }
    }

    fun bindAdapter(): InboxRecyclerViewAdapter = viewAdapter

    suspend fun updateAnnouncements(
        forceFetch: AppRepository.Companion.UpdateParameters =
            AppRepository.Companion.UpdateParameters.DETERMINE,
        refresh: Boolean = false
    ) {
        if (refresh || !reachedEnd) {
            val offset = if (refresh) {
                0
            } else {
                itemCount
            }

            val announcements = getAnnouncements(offset, forceFetch)

            if (refresh) {
                reachedEnd = false
                viewAdapter.refreshAnnouncements(announcements)
            } else {
                if (announcements.isEmpty()) {
                    reachedEnd = true
                } else {
                    viewAdapter.pushAnnouncements(announcements)
                }
            }
        }
    }

    private suspend fun getAnnouncements(
        offset: Int,
        forceFetch: AppRepository.Companion.UpdateParameters
    ) =
        appRepository.getAnnouncements(offset, forceFetch)
}