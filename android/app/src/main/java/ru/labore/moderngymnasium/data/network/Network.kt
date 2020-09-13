package ru.labore.moderngymnasium.data.network

import android.content.Context
import android.net.ConnectivityManager
import androidx.lifecycle.MutableLiveData
import okhttp3.Interceptor
import okhttp3.Response
import ru.labore.moderngymnasium.data.db.entities.AnnouncementEntity
import ru.labore.moderngymnasium.data.sharedpreferences.entities.User

class AppNetwork(context: Context): Interceptor {
    private val appContext = context.applicationContext
    val fetchedAnnouncementEntity = MutableLiveData<Array<AnnouncementEntity>>()

    suspend fun fetchAnnouncements(
        jwt: String,
        offset: Int,
        limit: Int
    ) = fetchedAnnouncementEntity.postValue(FetchAnnouncements(
            appContext,
            this,
            jwt,
            offset,
            limit
        ))


    override fun intercept(chain: Interceptor.Chain): Response {
        if (isOnline()) {
            val response = chain.proceed(chain.request())

            if (response.code() in 400..499) {
                throw ClientErrorException(response.code())
            }

            return response
        } else {
            throw ClientConnectionException()
        }
    }

    private fun isOnline(): Boolean {
        val connectivityManager = appContext.getSystemService(Context.CONNECTIVITY_SERVICE)
            as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo

        return (networkInfo != null && networkInfo.isConnected)
    }
}