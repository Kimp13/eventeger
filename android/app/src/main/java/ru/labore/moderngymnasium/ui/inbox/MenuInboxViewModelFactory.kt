package ru.labore.moderngymnasium.ui.inbox

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import ru.labore.moderngymnasium.data.repository.AppRepository

class MenuInboxViewModelFactory (
    private val appRepository: AppRepository
): ViewModelProvider.NewInstanceFactory() {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return MenuInboxViewModel(appRepository) as T
    }
}