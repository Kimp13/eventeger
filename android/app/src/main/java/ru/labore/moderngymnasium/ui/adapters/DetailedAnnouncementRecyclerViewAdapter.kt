package ru.labore.moderngymnasium.ui.adapters

import android.graphics.Color
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.children
import ru.labore.moderngymnasium.R
import ru.labore.moderngymnasium.data.db.entities.CommentEntity
import ru.labore.moderngymnasium.ui.base.BaseRecyclerViewAdapter
import ru.labore.moderngymnasium.ui.base.BaseViewHolder
import ru.labore.moderngymnasium.ui.fragments.detailedAnnouncement.DetailedAnnouncementViewModel
import ru.labore.moderngymnasium.utils.announcementEntityToCaption
import kotlin.math.min

class DetailedAnnouncementRecyclerViewAdapter(
    private val viewModel: DetailedAnnouncementViewModel
) : BaseRecyclerViewAdapter() {
    companion object DetailedAnnouncement {
        class CommentViewHolder(private val layout: ConstraintLayout) :
            BaseViewHolder(layout) {
            override fun onBind(position: Int, parent: BaseRecyclerViewAdapter) {
                if (parent is DetailedAnnouncementRecyclerViewAdapter) {
                    val pos = position - parent.beginAdditionalItems.size
                    val headline = layout.getChildAt(1) as TextView
                    val time = layout.getChildAt(2) as TextView
                    val text = layout.getChildAt(3) as TextView
                    val comment = parent.viewModel.comments[pos]
                    val author = parent.viewModel.appRepository.users[comment.authorId]
                    val iconButton = layout.children.last() as TextView

                    headline.text = if (author == null)
                        parent.viewModel.app.resources.getString(R.string.no_author)
                    else if (
                        author.firstName != null &&
                        author.lastName != null
                    )
                        "${author.firstName} ${author.lastName}"
                    else author.firstName
                        ?: parent.viewModel.app.resources.getString(R.string.noname)

                    time.text = DateUtils.getRelativeTimeSpanString(
                        comment.createdAt.toEpochSecond() * 1000,
                        parent.viewModel.appRepository.now().toEpochSecond() * 1000,
                        0
                    )

                    text.text = comment.text

                    if (comment.childrenCount > 0) {
                        iconButton.visibility = View.VISIBLE
                        iconButton.text = comment.childrenCount.toString()
                    }
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
                    val author = parent
                        .viewModel
                        .appRepository
                        .users[parent.viewModel.fragment.announcement.authorId]
                    val role = parent.viewModel.appRepository.roles[author?.roleId]
                    val `class` = parent.viewModel.appRepository.classes[author?.classId]

                    authorName.text = if (author == null) {
                        authorRank.visibility = View.GONE
                        parent.viewModel.app.resources.getString(R.string.no_author)
                    } else {
                        val caption = announcementEntityToCaption(
                            author,
                            parent.viewModel.app.resources.getString(R.string.noname),
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

                    text.text = parent.viewModel.fragment.announcement.text
                }
            }
        }

        class CreateCommentViewHolder(private val layout: LinearLayout) :
            BaseViewHolder(layout) {
            override fun onBind(position: Int, parent: BaseRecyclerViewAdapter) {
                if (parent is DetailedAnnouncementRecyclerViewAdapter) {
                    val headerLayout = layout.getChildAt(0) as LinearLayout
                    val headerCount = headerLayout.children.last() as TextView
                    val editLayout = layout.children.last() as LinearLayout
                    val children = editLayout.children.iterator()

                    parent.commentSettingsButton = children.next()
                    parent.commentTextView = children.next() as TextView
                    parent.commentSendButton = children.next()
                    parent.commentSendProgressBar = children.next()

                    println(parent.viewModel.fragment.announcement.commentCount)

                    headerCount.text = parent
                        .viewModel
                        .fragment
                        .announcement
                        .commentCount
                        .toString()

                    parent.initializeCommentSending()
                }
            }
        }

        const val ANNOUNCEMENT_VIEW_HOLDER_ID = "announcement"
        const val CREATE_COMMENT_VIEW_HOLDER_ID = "create_comment"
    }

    private lateinit var commentSettingsButton: View
    private lateinit var commentTextView: TextView
    private lateinit var commentSendButton: View
    private lateinit var commentSendProgressBar: View

    override fun createDefaultViewHolder(
        parent: ViewGroup
    ) = CommentViewHolder(
        LayoutInflater.from(parent.context)
            .inflate(
                R.layout.comment_view_holder,
                parent,
                false
            ) as ConstraintLayout
    )

    public override fun updateAdditionalItems() {
        var announcementNeeded = true
        var createCommentNeeded = true

        beginAdditionalItems.forEach {
            if (it.id == ANNOUNCEMENT_VIEW_HOLDER_ID)
                announcementNeeded = false
            else if (it.id == CREATE_COMMENT_VIEW_HOLDER_ID)
                createCommentNeeded = false
        }

        if (announcementNeeded)
            beginAdditionalItems.add(
                0,
                Base.AdditionalItem(
                    ANNOUNCEMENT_VIEW_HOLDER_ID
                ) { parent ->
                    AnnouncementViewHolder(
                        LayoutInflater.from(parent.context)
                            .inflate(
                                R.layout.detailed_announcement_view_holder,
                                parent,
                                false
                            ) as ConstraintLayout
                    )
                }
            )

        if (createCommentNeeded)
            beginAdditionalItems.add(
                1,
                Base.AdditionalItem(
                    CREATE_COMMENT_VIEW_HOLDER_ID
                ) { parent ->
                    CreateCommentViewHolder(
                        LayoutInflater.from(parent.context)
                            .inflate(
                                R.layout.create_comment_view_holder,
                                parent,
                                false
                            ) as LinearLayout
                    )
                }
            )
    }

    override val defaultItemCount: Int
        get() = viewModel.itemCount

    private fun initializeCommentSending(dropText: Boolean = true) {
        if (dropText)
            setCommentText("")

        commentSettingsButton.setOnClickListener {
            viewModel.promptCommentVisibility(it)
        }

        commentTextView.setOnClickListener {
            viewModel.enterCommentText()
        }
    }

    private fun onCreation() {
        commentSendProgressBar.visibility = View.GONE
        commentSendButton.visibility = View.VISIBLE

        initializeCommentSending(false)
    }

    fun onUnsuccessfulCreation(text: String) {
        onCreation()
        setCommentText(text)
    }

    fun onSuccessfulCreation(comment: CommentEntity) {
        onCreation()
        setCommentText("")
        notifyItemChanged(1)
        prependComment(comment)
    }

    fun setCommentText(text: String) {
        commentSendButton.apply {
            if (text.isEmpty()) {
                commentTextView.text = viewModel.app.resources.getString(R.string.write_comment)
                commentTextView.alpha = .7F

                alpha = .4F
                isFocusable = false
                isClickable = false
                setBackgroundColor(Color.TRANSPARENT)
                setOnClickListener(null)
            } else {
                commentTextView.text = text
                commentTextView.alpha = 1F

                alpha = 1F
                isFocusable = true
                isClickable = true
                setBackgroundResource(R.drawable.clickable_background_borderless_inverse)
                setOnClickListener {
                    commentSendProgressBar.visibility = View.VISIBLE
                    commentSendButton.visibility = View.GONE

                    setCommentText("")

                    commentTextView.setText(R.string.wait)
                    commentTextView.setOnClickListener(null)
                    commentSettingsButton.setOnClickListener(null)

                    viewModel.sendComment()
                }
            }
        }
    }

    private fun prependComment() {
        notifyItemInserted(beginAdditionalItems.size)
    }

    private fun prependComment(comment: CommentEntity) {
        viewModel.comments.add(0, comment)

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

        val minimal = min(newSize, previousSize)

        if (minimal > 0)
            notifyItemRangeChanged(
                beginAdditionalItems.size,
                minimal
            )

        if (previousSize < newSize) {
            notifyItemRangeInserted(
                beginAdditionalItems.size + previousSize,
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