package ru.labore.moderngymnasium.data

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import ru.labore.moderngymnasium.data.repository.AppRepository

class MyFirebaseMessagingService : FirebaseMessagingService(), DIAware {
    override val di: DI by lazy { (applicationContext as DIAware).di }

    private val repository: AppRepository by instance()

    override fun onNewToken(token: String) {
        repository.pushToken(token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        println("From: ${remoteMessage.from}")

        if (remoteMessage.data.isNotEmpty()) {
            println("Message data payload: ${remoteMessage.data}")
        }

        remoteMessage.notification?.let {
            println("Message Notification Body: ${it.body}")
        }
    }
}