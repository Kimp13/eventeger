package ru.labore.moderngymnasium.ui.adapters

import android.content.res.Resources
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import ru.labore.moderngymnasium.R
import ru.labore.moderngymnasium.data.AppRepository
import ru.labore.moderngymnasium.data.db.entities.AnnouncementEntity
import ru.labore.moderngymnasium.data.db.entities.CommentEntity
import ru.labore.moderngymnasium.ui.base.BaseRecyclerViewAdapter
import ru.labore.moderngymnasium.ui.base.BaseViewHolder
import ru.labore.moderngymnasium.utils.announcementEntityToCaption
import kotlin.math.min

class DetailedAnnouncementRecyclerViewAdapter(
    private val resources: Resources,
    private val appRepository: AppRepository,
    private val announcement: AnnouncementEntity,
    private val comments: MutableList<CommentEntity>
) : BaseRecyclerViewAdapter() {
    companion object DetailedAnnouncement {
        class CommentViewHolder(private val layout: ConstraintLayout) : BaseViewHolder(layout) {
            override fun onBind(position: Int, parent: BaseRecyclerViewAdapter) {
                if (parent is DetailedAnnouncementRecyclerViewAdapter) {
                    val pos = position - parent.beginAdditionalItems.size
                    val headline = layout.getChildAt(1) as TextView
                    val text = layout.getChildAt(2) as TextView
                    val comment = parent.comments[pos]
                    val author = parent.appRepository.users[comment.authorId]

                    headline.text = if (author == null)
                        parent.resources.getString(R.string.no_author)
                    else if (
                        author.firstName != null &&
                        author.lastName != null
                    )
                        "${author.firstName} ${author.lastName}"
                    else author.firstName ?: parent.resources.getString(R.string.noname)

                    text.text = comment.text
                }
            }
        }

        class AnnouncementViewHolder(private val layout: ConstraintLayout) :
            BaseViewHolder(layout) {
            override fun onBind(position: Int, parent: BaseRecyclerViewAdapter) {
                if (parent is DetailedAnnouncementRecyclerViewAdapter) {
                    val authorName = layout.getChildAt(1) as TextView
                    val authorRank = layout.getChildAt(2) as TextView
                    val text = layout.getChildAt(3) as TextView
                    val author = parent.appRepository.users[parent.announcement.authorId]
                    val role = parent.appRepository.roles[author?.roleId]
                    val `class` = parent.appRepository.classes[author?.classId]

                    authorName.text = if (author == null) {
                        authorRank.visibility = View.GONE
                        parent.resources.getString(R.string.no_author)
                    } else {
                        val caption = announcementEntityToCaption(
                            author,
                            parent.resources.getString(R.string.noname),
                            role,
                            `class`
                        )
                        val comma = caption.indexOf(',')

                        if (comma == -1) {
                            authorRank.visibility = View.GONE
                            caption
                        } else {
                            authorRank.text = caption.substring(comma + 2)
                            caption.substring(0, comma)
                        }
                    }

                    text.text = parent.announcement.text
                }
            }
        }

        const val ANNOUNCEMENT_VIEW_HOLDER_ID = "announcement"
    }

    override fun createDefaultViewHolder(
        parent: ViewGroup
    ) = CommentViewHolder(
        LayoutInflater.from(parent.context)
            .inflate(
                R.layout.detailed_announcement_view_handler,
                parent,
                false
            ) as ConstraintLayout
    )

    override fun updateAdditionalItems() {
        if (beginAdditionalItems.isEmpty()) {
            beginAdditionalItems.add(
                Base.AdditionalItem(
                    ANNOUNCEMENT_VIEW_HOLDER_ID
                ) { parent ->
                    AnnouncementViewHolder(
                        LayoutInflater.from(parent.context)
                            .inflate(
                                R.layout.detailed_announcement_view,
                                parent,
                                false
                            ) as ConstraintLayout
                    )
                }
            )
        }
    }

    override val defaultItemCount: Int
        get() = comments.size

    fun prependComment() {
        notifyItemInserted(beginAdditionalItems.size)
    }

    fun prependComment(comment: CommentEntity) {
        comments.add(0, comment)

        prependComment()
    }

    fun refreshComments(
        newSize: Int,
        previousSize: Int
    ) {
        if (previousSize > newSize) {
            notifyItemRangeRemoved(
                beginAdditionalItems.size + newSize,
                previousSize - newSize
            )
        }

        notifyItemRangeChanged(
            beginAdditionalItems.size,
            min(newSize, previousSize)
        )

        if (previousSize < newSize) {
            notifyItemRangeInserted(
                itemCount,
                newSize - previousSize
            )
        }
    }

    fun pushComments(
        previousSize: Int,
        addedSize: Int
    ) {
        notifyItemRangeInserted(
            previousSize + beginAdditionalItems.size,
            addedSize
        )
    }
}