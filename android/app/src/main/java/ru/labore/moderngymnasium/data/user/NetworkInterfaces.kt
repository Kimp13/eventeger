package ru.labore.moderngymnasium.data.user

import android.content.Context
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import ru.labore.moderngymnasium.R

data class UserCredentials(val username: String, val password: String)

interface UserSignIn {
    @POST("users/signin")
    suspend fun signIn(@Body body: UserCredentials): User?

    companion object {
        operator fun invoke(context: Context): UserSignIn {
            val okHttpClient = OkHttpClient.Builder().build()

            return Retrofit
                .Builder()
                .client(okHttpClient)
                .baseUrl(
                    context
                        .getResources()
                        .getString(R.string.api_url)
                )
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(UserSignIn::class.java)
        }
    }
}