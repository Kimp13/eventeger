package ru.labore.moderngymnasium.ui.fragments.detailedAnnouncement

import android.app.Activity
import android.app.Application
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import ru.labore.moderngymnasium.data.AppRepository
import ru.labore.moderngymnasium.data.db.entities.AuthoredEntity
import ru.labore.moderngymnasium.data.db.entities.CommentEntity
import ru.labore.moderngymnasium.ui.adapters.DetailedAnnouncementRecyclerViewAdapter
import ru.labore.moderngymnasium.ui.base.DetailedAuthoredEntityViewModel
import ru.labore.moderngymnasium.ui.fragments.detailedComment.DetailedCommentFragment

class DetailedAnnouncementViewModel(
    app: Application
) : DetailedAuthoredEntityViewModel(app) {
    override lateinit var onItemClicked: (AuthoredEntity) -> Unit

    fun getAdapter(
        currentFragment: DetailedAnnouncementFragment
    ): DetailedAnnouncementRecyclerViewAdapter {
        fragment = currentFragment
        announcementId = currentFragment.item.id

        val newAdapter = DetailedAnnouncementRecyclerViewAdapter(this)
        newAdapter.updateAdditionalItems()

        onItemClicked = {
            currentFragment.controls.push(
                DetailedCommentFragment(
                    currentFragment.controls,
                    it as CommentEntity
                )
            )
        }

        adapter = newAdapter

        return newAdapter
    }

    suspend fun updateComments(
        activity: Activity,
        forceFetch: AppRepository.Companion.UpdateParameters =
            AppRepository.Companion.UpdateParameters.DETERMINE,
        refresh: Boolean = false
    ) {
        if (current == null || !current!!.isActive) {
            if (refresh || !reachedEnd) {
                loading = true

                val offset = if (refresh)
                    0
                else
                    currentOffset

                val newComments = hashMapOf<Int, CommentEntity>()

                current = GlobalScope.launch {
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

                    reachedEnd = false
                    items.clear()
                    items.addAll(newComments.values.sortedByDescending {
                        it.createdAt
                    })

                    currentOffset = items.size

                    refreshItems(
                        previousSize,
                        itemCount
                    )
                } else {
                    if (newComments.isEmpty()) {
                        reachedEnd = true
                    } else {
                        val iterator = items.listIterator()

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
                        items.addAll(newComments.values)

                        if (currentOffset > fragment.item.commentCount) {
                            fragment.item.commentCount = currentOffset
                            appRepository.persistFetchedAuthoredEntity(fragment.item)
                        }

                        pushItems(
                            previousSize,
                            itemCount
                        )
                    }
                }
            }
        } else {
            current?.join()
        }
    }

    suspend fun setup(activity: Activity) {
        if (itemCount == 0)
            updateComments(
                activity,
                AppRepository.Companion.UpdateParameters.DETERMINE,
                true
            )
        else
            loading = false
    }

    private suspend fun getComments(
        offset: Int,
        forceFetch: AppRepository.Companion.UpdateParameters
    ) = appRepository.getComments(announcementId, offset, forceFetch)
}