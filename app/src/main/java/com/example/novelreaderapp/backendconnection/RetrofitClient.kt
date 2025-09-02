package com.example.novelreaderapp.backendconnection

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "https://byron-backend.onrender.com/Byron-Backend/"

    val api: ByronApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ByronApi::class.java)
    }
}