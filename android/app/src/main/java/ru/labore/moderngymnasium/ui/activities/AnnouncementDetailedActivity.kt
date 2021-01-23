package ru.labore.moderngymnasium.ui.activities

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import kotlinx.android.synthetic.main.activity_announcement_detailed.*
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import ru.labore.moderngymnasium.R
import ru.labore.moderngymnasium.data.db.entities.AnnouncementEntity
import ru.labore.moderngymnasium.data.repository.AppRepository
import ru.labore.moderngymnasium.ui.base.BaseActivity
import ru.labore.moderngymnasium.utils.announcementEntityToCaption

class AnnouncementDetailedActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_announcement_detailed)

        intent.setExtrasClassLoader(AnnouncementEntity::class.java.classLoader)
        if (intent.extras == null) {
            finish()
        } else {
            val announcement: AnnouncementEntity? = intent.getParcelableExtra("announcement")

            announcementDetailedAuthorName.text = if (announcement?.author == null) {
                announcementDetailedAuthorRank.visibility = View.GONE
                resources.getString(R.string.no_author)
            } else {
                val caption = announcementEntityToCaption(
                    announcement,
                    resources.getString(R.string.author_no_name)
                )
                val comma = caption.indexOf(',')

                if (comma == -1) {
                    announcementDetailedAuthorRank.visibility = View.GONE
                    caption
                } else {
                    announcementDetailedAuthorRank.text = caption.substring(comma + 2)
                    caption.substring(0, comma)
                }
            }

            announcementDetailedText.text = announcement?.text ?: "No text"
        }

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = resources.getString(
            R.string.announcement_detailed_activity_action_bar_title
        )
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == android.R.id.home) {
            finish()
            true
        } else {
            super.onOptionsItemSelected(item)
        }
    }
}