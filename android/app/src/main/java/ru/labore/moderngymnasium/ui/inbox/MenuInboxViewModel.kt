package ru.labore.moderngymnasium.ui.inbox

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import ru.labore.moderngymnasium.data.db.entities.AnnouncementEntity
import ru.labore.moderngymnasium.data.repository.AppRepository
import ru.labore.moderngymnasium.utils.lazyDeferred

class MenuInboxViewModel(
    val appRepository: AppRepository
) : ViewModel() {
    val announcements by lazyDeferred {
        appRepository.getAnnouncements()
    }

    suspend fun getAnnouncements(offset: Int) = appRepository.getAnnouncements(offset)
}