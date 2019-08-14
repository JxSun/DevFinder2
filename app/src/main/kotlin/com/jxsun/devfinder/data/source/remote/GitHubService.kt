package com.jxsun.devfinder.data.source.remote

import com.jxsun.devfinder.BuildConfig
import io.reactivex.Single
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Represents the remote GitHub API service.
 */
interface GitHubService {

    /**
     * Fetches the all users list since the given index.
     */
    @GET("users")
    fun getAllUsers(
        @Query("since") since: Int,
        @Query("per_page") perPage: Int,
        @Query("client_id") clientId: String = BuildConfig.CLIENT_ID,
        @Query("client_secret") clientSecret: String = BuildConfig.CLIENT_SECRET
    ): Single<Response<List<UserResponse>>>

    /**
     * Fetches the user's detailed info.
     */
    @GET("users/{username}")
    fun getUser(
        @Path("username") loginName: String
    ): Single<Response<UserDetailResponse>>

    /**
     * The factory to create the [GitHubService].
     */
    class Factory {

        fun create(customOkHttpClient: OkHttpClient? = null): GitHubService {
            return Retrofit.Builder()
                .baseUrl("https://api.github.com")
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(customOkHttpClient ?: createDefaultOkHttpClient())
                .build()
                .create(GitHubService::class.java)
        }

        private fun createDefaultOkHttpClient(): OkHttpClient {
            return OkHttpClient.Builder()
                .apply {
                    if (BuildConfig.DEBUG) {
                        val loggingInterceptor = HttpLoggingInterceptor().apply {
                            level = HttpLoggingInterceptor.Level.BODY
                        }
                        addInterceptor(loggingInterceptor)
                    }
                }
                .build()
        }
    }
}
