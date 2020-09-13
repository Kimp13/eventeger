package ru.labore.moderngymnasium.data.repository

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.threeten.bp.ZonedDateTime
import ru.labore.moderngymnasium.R
import ru.labore.moderngymnasium.data.db.daos.AnnouncementEntityDao
import ru.labore.moderngymnasium.data.db.entities.AnnouncementEntity
import ru.labore.moderngymnasium.data.db.entities.AnnouncementWithAuthor
import ru.labore.moderngymnasium.data.network.AppNetwork
import ru.labore.moderngymnasium.data.network.SignIn
import ru.labore.moderngymnasium.data.sharedpreferences.entities.User

class AppRepository(
    private val context: Context,
    private val announcementEntityDao: AnnouncementEntityDao,
    private val appNetwork: AppNetwork
) {
    private val gson = Gson()
    private val sharedPreferences = context.getSharedPreferences(
        context.getString(R.string.utility_shared_preference_file_key),
        Context.MODE_PRIVATE
    )

    var user: User? = null

    init {
        val userString = sharedPreferences.getString("user", null)

        if (userString != null) {
            user = gson.fromJson(userString, User::class.java)
        }

        appNetwork.fetchedAnnouncementEntity.observeForever {
            persistFetchedAnnouncements(it)
        }
    }

    suspend fun signIn(username: String, password: String) {
        user = SignIn(context, appNetwork, username, password)
        val editor = sharedPreferences.edit()

        editor.putString("user", gson.toJson(user))
        editor.apply()
    }

    suspend fun getAnnouncements(offset: Int = 0, limit: Int = 10): LiveData<Array<AnnouncementWithAuthor>> {
        if (user == null) {
            val result: LiveData<Array<AnnouncementWithAuthor>> by lazy {
                MutableLiveData(emptyArray())
            }

            return result
        }

        return withContext(Dispatchers.IO) {
            if (isAnnouncementsFetchNeeded(ZonedDateTime.now().minusHours(1))) {
                appNetwork.fetchAnnouncements(user!!.jwt, offset, limit)
            }

            return@withContext announcementEntityDao.getAnnouncements(offset, limit)
        }
    }

    private fun persistFetchedAnnouncements(fetchedAnnouncements: Array<AnnouncementEntity>) {
        GlobalScope.launch(Dispatchers.IO) {
            announcementEntityDao.upsert(fetchedAnnouncements)
        }
    }

    private fun isAnnouncementsFetchNeeded(lastFetchTime: ZonedDateTime): Boolean {
        val tenMinutesAgo = ZonedDateTime.now().minusMinutes(10)

        return lastFetchTime.isBefore(tenMinutesAgo)
    }
}