package ru.labore.moderngymnasium.data.network

import android.content.Context
import com.google.gson.Gson
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import ru.labore.moderngymnasium.R
import ru.labore.moderngymnasium.data.db.entities.AnnouncementEntity
import ru.labore.moderngymnasium.data.db.entities.ClassEntity
import ru.labore.moderngymnasium.data.db.entities.RoleEntity
import ru.labore.moderngymnasium.data.db.entities.UserEntity
import ru.labore.moderngymnasium.data.sharedpreferences.entities.User

class Utility(
    private val context: Context,
    requestInterceptor: Interceptor,
    private val gson: Gson
) {
    companion object {
        data class UserCredentials(val username: String, val password: String)

        data class AnnouncementTextAndRecipients(
            val text: String,
            val recipients: HashMap<Int, MutableList<Int>>
        )

        data class TokenPayload(val token: String)

        data class CountResponse(val count: Int)

        private interface SignIn {
            @POST("users/signin")
            suspend fun signIn(@Body body: UserCredentials): User?
        }

        private interface CreateAnnouncement {
            @POST("announcements/create")
            suspend fun createAnnouncement(
                @Header("Authentication") jwt: String,
                @Body body: AnnouncementTextAndRecipients
            )
        }

        private interface PushToken {
            @POST("tokens/add")
            suspend fun pushToken(
                @Header("Authentication") jwt: String,
                @Body body: TokenPayload
            )
        }

        private interface FetchAnnouncement {
            @GET("announcements/get")
            suspend fun fetch(
                @Header("Authentication") jwt: String,
                @Query("id") id: Int
            ): AnnouncementEntity?
        }

        private interface FetchAnnouncements {
            @GET("announcements/getMine")
            suspend fun fetch(
                @Header("Authentication") jwt: String,
                @Query("offset") offset: Int,
                @Query("limit") limit: Int
            ): Array<AnnouncementEntity>
        }

        private interface CountAnnouncements {
            @GET("announcements/countMine")
            suspend fun count(
                @Header("Authentication") jwt: String
            ): CountResponse
        }

        private interface FetchUser {
            @GET("users")
            suspend fun fetch(@Query("id") id: Int): UserEntity?
        }

        private interface FetchRole {
            @GET("roles")
            suspend fun fetch(@Query("id") id: Int): RoleEntity?
        }

        private interface FetchAllRoles {
            @GET("roles/all")
            suspend fun fetch(
                @Header("Authentication") jwt: String
            ): Array<RoleEntity?>
        }

        private interface FetchClass {
            @GET("class")
            suspend fun fetch(@Query("id") id: Int): ClassEntity?
        }
    }

    private val okHttpClient = OkHttpClient
        .Builder()
        .addInterceptor(requestInterceptor)
        .build()

    suspend fun signIn(
        username: String,
        password: String
    ): User? = Retrofit
        .Builder()
        .client(okHttpClient)
        .baseUrl(
            context
                .resources
                .getString(R.string.api_url)
        )
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()
        .create(SignIn::class.java)
        .signIn(UserCredentials(username, password))

    suspend fun createAnnouncement(
        jwt: String,
        text: String,
        recipients: HashMap<Int, MutableList<Int>>
    ) = Retrofit
        .Builder()
        .client(okHttpClient)
        .baseUrl(
            context
                .resources
                .getString(R.string.api_url)
        )
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()
        .create(CreateAnnouncement::class.java)
        .createAnnouncement(
            jwt,
            AnnouncementTextAndRecipients(text, recipients)
        )

    suspend fun pushToken(
        jwt: String,
        token: String
    ) = Retrofit
        .Builder()
        .client(okHttpClient)
        .baseUrl(
            context
                .resources
                .getString(R.string.api_url)
        )
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()
        .create(PushToken::class.java)
        .pushToken(
            jwt,
            TokenPayload(token)
        )

    suspend fun fetchAnnouncement(
        jwt: String,
        id: Int
    ) = Retrofit
        .Builder()
        .client(okHttpClient)
        .baseUrl(
            context
                .resources
                .getString(R.string.api_url)
        )
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()
        .create(FetchAnnouncement::class.java)
        .fetch(
            jwt,
            id
        )

    suspend fun fetchAnnouncements(
        jwt: String,
        offset: Int,
        limit: Int
    ) = Retrofit
        .Builder()
        .client(okHttpClient)
        .baseUrl(
            context
                .resources
                .getString(R.string.api_url)
        )
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()
        .create(FetchAnnouncements::class.java)
        .fetch(
            jwt,
            offset,
            limit
        )

    suspend fun countAnnouncements(
        jwt: String
    ) = Retrofit
        .Builder()
        .client(okHttpClient)
        .baseUrl(
            context
                .resources
                .getString(R.string.api_url)
        )
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()
        .create(CountAnnouncements::class.java)
        .count(
            jwt
        ).count

    suspend fun fetchUser(
        id: Int
    ) = Retrofit
        .Builder()
        .client(okHttpClient)
        .baseUrl(
            context
                .resources
                .getString(R.string.api_url)
        )
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()
        .create(FetchUser::class.java)
        .fetch(id)

    suspend fun fetchRole(
        id: Int
    ) = Retrofit
        .Builder()
        .client(okHttpClient)
        .baseUrl(
            context
                .resources
                .getString(R.string.api_url)
        )
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()
        .create(FetchRole::class.java)
        .fetch(id)

    suspend fun fetchAllRoles(jwt: String) = Retrofit
        .Builder()
        .client(okHttpClient)
        .baseUrl(
            context
                .resources
                .getString(R.string.api_url)
        )
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()
        .create(FetchAllRoles::class.java)
        .fetch(jwt)

    suspend fun fetchClass(
        id: Int
    ) = Retrofit
        .Builder()
        .client(okHttpClient)
        .baseUrl(
            context
                .resources
                .getString(R.string.api_url)
        )
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(FetchClass::class.java)
        .fetch(id)
}

