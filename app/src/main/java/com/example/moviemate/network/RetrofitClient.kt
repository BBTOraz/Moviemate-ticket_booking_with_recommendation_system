package com.example.moviemate.network

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private const val BASE_URL_MOVIES = "https://afisha.api.kinopark.kz/"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(600, TimeUnit.SECONDS) // Таймаут подключения
        .readTimeout(600, TimeUnit.SECONDS)    // Таймаут чтения данных
        .writeTimeout(600, TimeUnit.SECONDS)   // Таймаут записи данных
        .build()

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL_MOVIES)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    private val recommendationFetchRetrofit = Retrofit.Builder()
        .baseUrl("https://web-production-d5078.up.railway.app/")
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val api: ApiService = recommendationFetchRetrofit.create(ApiService::class.java)

    val cityApi: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }

    val moviesApi: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL_MOVIES)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}