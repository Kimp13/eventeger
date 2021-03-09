package ru.labore.eventeger.ui.adapters

import android.text.format.DateUtils
import android.view.LayoutInflater
import android.widget.RelativeLayout
import android.widget.TextView
import ru.labore.eventeger.R
import ru.labore.eventeger.data.db.entities.CommentEntity
import ru.labore.eventeger.ui.base.BaseRecyclerViewAdapter
import ru.labore.eventeger.ui.base.BaseViewHolder
import ru.labore.eventeger.ui.base.DetailedAuthoredEntityRecyclerViewAdapter
import ru.labore.eventeger.ui.fragments.detailedComment.DetailedCommentViewModel

class DetailedCommentRecyclerViewAdapter(
    override val viewModel: DetailedCommentViewModel
) : DetailedAuthoredEntityRecyclerViewAdapter(viewModel) {
    companion object DetailedComment {

        class ParentCommentViewHolder(private val layout: RelativeLayout) :
            BaseViewHolder(layout) {
            override fun onBind(position: Int, parent: BaseRecyclerViewAdapter) {
                if (parent is DetailedCommentRecyclerViewAdapter) {
                    val headline = layout.getChildAt(1) as TextView
                    val time = layout.getChildAt(2) as TextView
                    val text = layout.getChildAt(3) as TextView
                    val comment = parent.viewModel.fragment.item as CommentEntity
                    val author = parent.viewModel.appRepository.users[comment.authorId]

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
                }
            }
        }

        const val DETAILED_COMMENT_VIEW_HOLDER_ID = "detailed_comment"
    }

    public override fun updateAdditionalItems() {
        var absent = true

        for (i in 0 until beginAdditionalItems.size) {
            if (beginAdditionalItems[i].id == DETAILED_COMMENT_VIEW_HOLDER_ID) {
                absent = false
                break
            }
        }

        if (absent)
            beginAdditionalItems.add(
                0,
                Base.AdditionalItem(
                    DETAILED_COMMENT_VIEW_HOLDER_ID
                ) { parent ->
                    ParentCommentViewHolder(
                        LayoutInflater.from(parent.context)
                            .inflate(
                                R.layout.detailed_comment_view_holder,
                                parent,
                                false
                            ) as RelativeLayout
                    )
                }
            )

        super.updateAdditionalItems()
    }
}