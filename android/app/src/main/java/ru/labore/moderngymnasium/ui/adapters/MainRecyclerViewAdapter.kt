package ru.labore.moderngymnasium.ui.adapters

import android.content.res.Resources
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ru.labore.moderngymnasium.R
import ru.labore.moderngymnasium.data.db.entities.AnnouncementEntity
import ru.labore.moderngymnasium.utils.announcementEntityToCaption

class MainRecyclerViewAdapter(
    private val resources: Resources,
    private var announcements: MutableList<AnnouncementEntity>
) : RecyclerView.Adapter<MainRecyclerViewAdapter.MainViewHolder>() {
    class MainViewHolder(val layout: LinearLayout) : RecyclerView.ViewHolder(layout)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainViewHolder {
        val linearLayout = LayoutInflater.from(parent.context)
            .inflate(R.layout.inbox_recycler_view, parent, false) as LinearLayout

        return MainViewHolder(linearLayout)
    }

    override fun onBindViewHolder(holder: MainViewHolder, position: Int) {
        val authorView = (holder.layout.getChildAt(0) as TextView)
        val textView = (holder.layout.getChildAt(1) as TextView)

        authorView.text = if (announcements[position].author == null) {
            resources.getString(R.string.no_author)
        } else {
            announcementEntityToCaption(
                announcements[position],
                resources.getString(R.string.author_no_name)
            )
        }

        textView.text = announcements[position].text
    }

    override fun getItemCount() = announcements.size

    fun prependAnnouncement(
        announcement: AnnouncementEntity
    ) {
        announcements.add(0, announcement)

        notifyItemInserted(0)
    }

    fun refreshAnnouncements(
        newAnnouncements: Array<AnnouncementEntity>
    ) {
        val count = itemCount

        announcements = mutableListOf()

        notifyItemRangeRemoved(0, count)

        newAnnouncements.forEach {
            announcements.add(it)
        }

        notifyItemRangeInserted(0, announcements.size)
    }

    fun pushAnnouncements(
        newAnnouncements: Array<AnnouncementEntity>
    ) {
        val positionStart = itemCount

        announcements.addAll(newAnnouncements)

        notifyItemRangeInserted(
            positionStart,
            newAnnouncements.size
        )
    }
}