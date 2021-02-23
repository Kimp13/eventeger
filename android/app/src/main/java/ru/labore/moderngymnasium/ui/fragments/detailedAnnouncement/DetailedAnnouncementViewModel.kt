package ru.labore.moderngymnasium.ui.fragments.detailedAnnouncement

import android.app.Activity
import android.app.Application
import android.graphics.Color
import android.text.InputType
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import androidx.appcompat.widget.PopupMenu
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.children
import kotlinx.android.synthetic.main.fragment_announcement_detailed.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import ru.labore.moderngymnasium.R
import ru.labore.moderngymnasium.data.AppRepository
import ru.labore.moderngymnasium.data.db.entities.CommentEntity
import ru.labore.moderngymnasium.ui.adapters.DetailedAnnouncementRecyclerViewAdapter
import ru.labore.moderngymnasium.ui.base.BaseViewModel
import ru.labore.moderngymnasium.utils.hideKeyboard
import ru.labore.moderngymnasium.utils.showKeyboard

class DetailedAnnouncementViewModel(
    val app: Application
) : BaseViewModel(app) {
    private lateinit var viewAdapter: DetailedAnnouncementRecyclerViewAdapter
    lateinit var fragment: DetailedAnnouncementFragment
    private var isHidden = false
    private var currentText = ""
        set(value) {
            field = value

            viewAdapter.setCommentText(value)
        }
    private var currentOffset = 0
    val itemCount
        get() = comments.size

    private var loading: Boolean
        get() =
            viewAdapter.loading
        set(value) {
            viewAdapter.loading = value
        }

    private var current: Job? = null
    private var reachedEnd = false
    val comments = mutableListOf<CommentEntity>()

    fun promptCommentVisibility(anchor: View) {
        val popup = PopupMenu(fragment.requireContext(), anchor)

        popup.menuInflater.inflate(R.menu.create_comment_menu, popup.menu)
        popup.menu.getItem(0).isChecked = isHidden

        popup.setOnMenuItemClickListener {
            isHidden = !it.isChecked
            it.isChecked = isHidden

            it.setShowAsAction(MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW)
            it.actionView = View(fragment.requireContext())
            it.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
                override fun onMenuItemActionCollapse(item: MenuItem?) = false
                override fun onMenuItemActionExpand(item: MenuItem?) = false
            })

            false
        }

        popup.show()
    }

    fun enterCommentText() {
        val layout = fragment.announcementDetailedRootLayout
        val context = fragment.requireContext()
        val grayScreenLayoutParams = ConstraintLayout.LayoutParams(
            ConstraintLayout.LayoutParams.MATCH_PARENT,
            0
        )

        grayScreenLayoutParams.topToTop = layout.id
        grayScreenLayoutParams.startToStart = layout.id
        grayScreenLayoutParams.bottomToBottom = layout.id
        grayScreenLayoutParams.endToEnd = layout.id
        grayScreenLayoutParams.verticalWeight = 1F

        val grayScreen = View(context)
        grayScreen.setBackgroundColor(Color.argb(128, 0, 0, 0))
        grayScreen.layoutParams = grayScreenLayoutParams

        val childLayout = LayoutInflater.from(context)
            .inflate(
                R.layout.create_comment_text_layout,
                layout,
                false
            ) as LinearLayout
        val iterator = childLayout.children.iterator()

        val editText = iterator.next() as EditText
        val button = iterator.next()

        editText.setText(currentText)
        editText.inputType = InputType.TYPE_TEXT_FLAG_MULTI_LINE or InputType.TYPE_CLASS_TEXT

        layout.addView(grayScreen)
        layout.addView(childLayout)

        editText.requestFocus()
        fragment.showKeyboard()

        grayScreen.setOnClickListener {
            val size = layout.childCount

            layout.removeViewAt(size - 1)
            layout.removeViewAt(size - 2)

            fragment.hideKeyboard()
        }

        button.setOnClickListener {
            val size = layout.childCount

            layout.removeViewAt(size - 1)
            layout.removeViewAt(size - 2)

            fragment.hideKeyboard()

            currentText = editText.text.toString()
        }
    }

    fun sendComment() {
        GlobalScope.launch {
            makeRequest(fragment.requireActivity(), {
                val comment = appRepository.createComment(
                    fragment.announcement.id,
                    currentText,
                    isHidden
                )

                comment.createdAt = appRepository.now().minusSeconds(1)

                currentText = ""

                fragment.announcement.commentCount += 1
                println(fragment.announcement.commentCount)
                appRepository.persistFetchedAnnouncement(fragment.announcement)
                fragment.requireActivity().runOnUiThread {
                    viewAdapter.onSuccessfulCreation(comment)
                }
            }, {
                fragment.requireActivity().runOnUiThread {
                    viewAdapter.onUnsuccessfulCreation(currentText)
                }
            })
        }
    }

    fun getAdapter(
        currentFragment: DetailedAnnouncementFragment
    ): DetailedAnnouncementRecyclerViewAdapter {
        fragment = currentFragment

        viewAdapter = DetailedAnnouncementRecyclerViewAdapter(this)
        viewAdapter.updateAdditionalItems()

        return viewAdapter
    }

    suspend fun updateComments(
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

                    loading = false
                }

                current?.join()

                if (refresh) {
                    val previousSize = itemCount

                    currentOffset = 0
                    reachedEnd = false
                    comments.clear()
                    comments.addAll(newComments.values.sortedByDescending {
                        it.createdAt
                    })

                    viewAdapter.refreshComments(
                        itemCount,
                        previousSize
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

        if (currentOffset > fragment.announcement.commentCount) {
            fragment.announcement.commentCount = currentOffset
            appRepository.persistFetchedAnnouncement(fragment.announcement)
        }

        loading = false
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
    ) = appRepository.getComments(fragment.announcement.id, offset, forceFetch)
}