package ru.labore.moderngymnasium.data.network

import android.content.Context
import com.google.gson.Gson
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import org.threeten.bp.ZonedDateTime
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import ru.labore.moderngymnasium.R
import ru.labore.moderngymnasium.data.db.entities.*
import ru.labore.moderngymnasium.data.sharedpreferences.entities.AnnounceMap
import ru.labore.moderngymnasium.data.sharedpreferences.entities.User

class Utility(
    context: Context,
    requestInterceptor: Interceptor,
    gson: Gson
) {
    companion object {
        data class UserCredentials(val username: String, val password: String)

        data class AnnouncementTextAndRecipients(
            val text: String,
            val recipients: HashMap<Int, HashSet<Int>>
        )

        data class FullPackageAnnouncement(
            val text: String,
            val recipients: HashMap<Int, HashSet<Int>>,
            val beginsAt: ZonedDateTime?,
            val endsAt: ZonedDateTime?,
            val event: Boolean = true
        )

        data class CreateCommentBody(
            val announcementId: Int,
            val text: String,
            val hidden: Boolean,
            val replyTo: Int?
        )

        data class TokenPayload(val token: String)

        data class CountResponse(val count: Int)

        private interface FetchUtility {
            @GET("users/me")
            suspend fun fetchMe(
                @Header("Authentication") jwt: String
            ): User?

            @GET("roles/mine")
            suspend fun fetchAnnounceMap(
                @Header("Authentication") jwt: String
            ): AnnounceMap

            @POST("users/signin")
            suspend fun signIn(
                @Body body: UserCredentials
            ): User?

            @POST("announcements/create")
            suspend fun createAnnouncement(
                @Header("Authentication") jwt: String,
                @Body body: AnnouncementTextAndRecipients
            )

            @POST("comments/create")
            suspend fun createComment(
                @Header("Authorization") jwt: String,
                @Body body: CreateCommentBody
            ): CommentEntity

            @POST("announcements/create")
            suspend fun createAnnouncement(
                @Header("Authorization") jwt: String,
                @Body body: FullPackageAnnouncement
            )

            @POST("tokens/add")
            suspend fun pushToken(
                @Header("Authentication") jwt: String,
                @Body body: TokenPayload
            )

            @GET("comments/")
            suspend fun fetchComment(
                @Header("Authorization") jwt: String,
                @Query("id") id: Int
            ): CommentEntity?

            @GET("comments/get")
            suspend fun fetchComments(
                @Header("Authorization") jwt: String,
                @Query("announcementId") announcementId: Int,
                @Query("offset") offset: Int,
                @Query("replyTo") replyTo: Int?
            ): Array<CommentEntity>

            @GET("announcements/getMine")
            suspend fun fetchAnnouncement(
                @Header("Authentication") jwt: String,
                @Query("id") id: Int
            ): AnnouncementEntity?

            @GET("announcements/getMine")
            suspend fun fetchAnnouncements(
                @Header("Authentication") jwt: String,
                @Query("offset") offset: Int
            ): Array<AnnouncementEntity>

            @GET("announcements/countMine")
            suspend fun countAnnouncements(
                @Header("Authentication") jwt: String
            ): CountResponse

            @GET("users")
            suspend fun fetchUser(@Query("id") id: Int): UserEntity?

            @GET("users")
            suspend fun fetchUsers(@Query("id[]") id: Array<Int>): Array<UserEntity>

            @GET("roles")
            suspend fun fetchRole(@Query("id") id: Int): RoleEntity?

            @GET("roles")
            suspend fun fetchRoles(@Query("id[]") ids: Array<Int>): Array<RoleEntity>

            @GET("class")
            suspend fun fetchClass(@Query("id") id: Int): ClassEntity?

            @GET("class")
            suspend fun fetchClasses(@Query("id[]") ids: Array<Int>):
                    Array<ClassEntity>
        }
    }

    private val okHttpClient = OkHttpClient
        .Builder()
        .addInterceptor(requestInterceptor)
        .build()

    private val builder = Retrofit
        .Builder()
        .client(okHttpClient)
        .baseUrl(
            context
                .resources
                .getString(R.string.api_url)
        )
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()
        .create(FetchUtility::class.java)

    suspend fun fetchMe(
        jwt: String
    ): User? = builder.fetchMe(jwt)

    suspend fun fetchAnnouncementMap(
        jwt: String
    ): AnnounceMap = builder.fetchAnnounceMap(jwt)

    suspend fun signIn(
        username: String,
        password: String
    ): User? = builder.signIn(UserCredentials(username, password))

    suspend fun createComment(
        jwt: String,
        announcementId: Int,
        text: String,
        hidden: Boolean,
        replyTo: Int? = null
    ) = builder.createComment(jwt, CreateCommentBody(
        announcementId, text, hidden, replyTo
    ))

    suspend fun createAnnouncement(
        jwt: String,
        text: String,
        recipients: HashMap<Int, HashSet<Int>>
    ) = builder.createAnnouncement(
        jwt,
        AnnouncementTextAndRecipients(text, recipients)
    )

    suspend fun createAnnouncement(
        jwt: String,
        text: String,
        recipients: HashMap<Int, HashSet<Int>>,
        beginsAt: ZonedDateTime?,
        endsAt: ZonedDateTime?
    ) = builder.createAnnouncement(
        jwt,
        FullPackageAnnouncement(text, recipients, beginsAt, endsAt)
    )

    suspend fun pushToken(
        jwt: String,
        token: String
    ) = builder.pushToken(jwt, TokenPayload(token))

    suspend fun fetchComment(
        jwt: String,
        id: Int
    ) = builder.fetchComment(jwt, id)

    suspend fun fetchComments(
        jwt: String,
        announcementId: Int,
        offset: Int,
        replyTo: Int?
    ) = builder.fetchComments(jwt, announcementId, offset, replyTo)

    suspend fun fetchAnnouncement(
        jwt: String,
        id: Int
    ) = builder.fetchAnnouncement(jwt, id)

    suspend fun fetchAnnouncements(
        jwt: String,
        offset: Int
    ) = builder
        .fetchAnnouncements(jwt, offset)

    suspend fun countAnnouncements(
        jwt: String
    ) = builder.countAnnouncements(jwt).count

    suspend fun fetchUser(
        id: Int
    ) = builder.fetchUser(id)

    suspend fun fetchUsers(
        ids: Array<Int>
    ) = builder.fetchUsers(ids)

    suspend fun fetchRole(
        id: Int
    ) = builder.fetchRole(id)

    suspend fun fetchRoles(
        ids: Array<Int>
    ) = builder.fetchRoles(ids)

    suspend fun fetchClass(
        id: Int
    ) = builder.fetchClass(id)

    suspend fun fetchClasses(
        ids: Array<Int>
    ) = builder.fetchClasses(ids)
}

