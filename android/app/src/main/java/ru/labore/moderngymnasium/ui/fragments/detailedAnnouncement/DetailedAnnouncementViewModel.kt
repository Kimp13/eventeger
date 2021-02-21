package ru.labore.moderngymnasium.ui.fragments.detailedAnnouncement

import android.app.Activity
import android.app.Application
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import ru.labore.moderngymnasium.data.AppRepository
import ru.labore.moderngymnasium.data.db.entities.AnnouncementEntity
import ru.labore.moderngymnasium.data.db.entities.CommentEntity
import ru.labore.moderngymnasium.ui.adapters.DetailedAnnouncementRecyclerViewAdapter
import ru.labore.moderngymnasium.ui.base.BaseViewModel

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

    suspend fun updateComments(
        activity: Activity,
        forceFetch: AppRepository.Companion.UpdateParameters =
            AppRepository.Companion.UpdateParameters.DETERMINE,
        refresh: Boolean = false
    ) {
        if (current == null || !current!!.isActive) {
            if (refresh || !reachedEnd) {
                val offset = if (refresh) {
                    0
                } else {
                    currentOffset
                }

                val newComments = hashMapOf<Int, CommentEntity>()

                current = GlobalScope.async {
                    makeRequest(
                        activity,
                        {
                            getComments(offset, forceFetch).forEach {
                                newComments[it.id] = it
                            }
                        },
                        {
                            getComments(
                                offset,
                                AppRepository.Companion.UpdateParameters.DONT_UPDATE
                            ).forEach {
                                newComments[it.id] = it
                            }
                        }
                    )
                }

                current?.join()

                if (refresh) {
                    val previousSize = itemCount

                    currentOffset = 0
                    reachedEnd = false
                    comments.clear()
                    comments.addAll(newComments.values)

                    viewAdapter.refreshComments(
                        previousSize,
                        itemCount
                    )
                } else {
                    if (newComments.isEmpty()) {
                        reachedEnd = true
                    } else {
                        val iterator = comments.listIterator()

                        while (iterator.hasNext()) {
                            val it = iterator.next()
                            val newValue = newComments[it.id]

                            if (newValue != null) {
                                iterator.set(newValue)
                                newComments.remove(it.id)
                            }
                        }

                        val previousSize = itemCount

                        currentOffset += newComments.size
                        comments.addAll(newComments.values)

                        viewAdapter.pushComments(
                            previousSize,
                            itemCount
                        )
                    }
                }
            }
        } else {
            current?.join()
        }

        if (currentOffset > announcement.commentCount) {
            announcement.commentCount = currentOffset
            appRepository.persistFetchedAnnouncement(announcement)
        }
    }

    private suspend fun getComments(
        offset: Int,
        forceFetch: AppRepository.Companion.UpdateParameters
    ) = appRepository.getComments(announcement.id, offset, forceFetch)
}