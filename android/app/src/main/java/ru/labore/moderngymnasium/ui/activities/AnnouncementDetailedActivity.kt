package ru.labore.moderngymnasium.ui.activities

import android.os.Bundle
import kotlinx.android.synthetic.main.activity_announcement_detailed.*
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import ru.labore.moderngymnasium.R
import ru.labore.moderngymnasium.data.db.entities.AnnouncementEntity
import ru.labore.moderngymnasium.data.repository.AppRepository
import ru.labore.moderngymnasium.ui.base.ScopedActivity

class AnnouncementDetailedActivity : ScopedActivity(), DIAware {
    override val di: DI by lazy { (applicationContext as DIAware).di }
    val repository: AppRepository by instance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_announcement_detailed)

        intent.setExtrasClassLoader(AnnouncementEntity::class.java.classLoader)
        if (intent.extras == null) {
            finish()
        } else {
            val announcement: AnnouncementEntity? = intent.getParcelableExtra("announcement")

            detailedTextView.text = announcement.toString()
        }
    }
}