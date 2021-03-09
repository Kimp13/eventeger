package ru.labore.eventeger.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import ru.labore.eventeger.R
import ru.labore.eventeger.ui.base.DetailedAuthoredEntityRecyclerViewAdapter
import ru.labore.eventeger.ui.base.BaseRecyclerViewAdapter
import ru.labore.eventeger.ui.base.BaseViewHolder
import ru.labore.eventeger.ui.fragments.detailedAnnouncement.DetailedAnnouncementViewModel
import ru.labore.eventeger.utils.announcementEntityToCaption

class DetailedAnnouncementRecyclerViewAdapter(
    override val viewModel: DetailedAnnouncementViewModel
) : DetailedAuthoredEntityRecyclerViewAdapter(viewModel) {
    companion object DetailedAnnouncement {

        class AnnouncementViewHolder(private val layout: RelativeLayout) :
            BaseViewHolder(layout) {

            override fun onBind(position: Int, parent: BaseRecyclerViewAdapter) {
                if (parent is DetailedAuthoredEntityRecyclerViewAdapter) {
                    val authorName = layout.getChildAt(1) as TextView
                    val authorRank = layout.getChildAt(2) as TextView
                    val text = layout.getChildAt(3) as TextView
                    val author = parent
                        .viewModel
                        .appRepository
                        .users[parent.viewModel.fragment.item.authorId]
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

                    text.text = parent.viewModel.fragment.item.text
                }
            }
        }

        const val ANNOUNCEMENT_VIEW_HOLDER_ID = "announcement"
    }

    public override fun updateAdditionalItems() {
        var absent = true

        for (i in 0 until beginAdditionalItems.size) {
            if (beginAdditionalItems[i].id == ANNOUNCEMENT_VIEW_HOLDER_ID) {
                absent = false
                break
            }
        }

        if (absent)
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
                            ) as RelativeLayout
                    )
                }
            )

        super.updateAdditionalItems()
    }
}