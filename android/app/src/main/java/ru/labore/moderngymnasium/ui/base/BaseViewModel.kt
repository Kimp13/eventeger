package ru.labore.moderngymnasium.ui.base

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import ru.labore.moderngymnasium.data.repository.AppRepository

abstract class BaseViewModel(
    application: Application
) : AndroidViewModel(application), DIAware {
    override val di: DI by lazy { (application as DIAware).di }
    val appRepository: AppRepository by instance()

    fun cleanseUser() {
        appRepository.user = null
    }
}