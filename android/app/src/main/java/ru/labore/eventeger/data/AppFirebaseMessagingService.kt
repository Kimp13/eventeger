package ru.labore.eventeger.data

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance

class AppFirebaseMessagingService : FirebaseMessagingService(), DIAware {
    override val di: DI by lazy { (applicationContext as DIAware).di }

    private val repository: AppRepository by instance()
    private val gson = Gson()

    override fun onNewToken(token: String) {
        repository.saveToken(token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        if (remoteMessage.data.isNotEmpty()) {
            repository.pushNewAnnouncement(remoteMessage.data)
        }
    }
}