package com.example.novelreaderapp.backendconnection

import com.example.novelreaderapp.backendconnection.backmodels.AuthResponse
import com.example.novelreaderapp.backendconnection.backmodels.NovelBackend
import com.example.novelreaderapp.backendconnection.backmodels.UserCredentials
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.DELETE
import retrofit2.http.PUT
import retrofit2.http.Path

// Data class for ping response
data class PingResponse(
    val message: String,
    val timestamp: String
)

interface ByronApi {
    @POST("users/register")
    suspend fun register(@Body user: UserCredentials): Response<AuthResponse>

    @POST("users/login")
    suspend fun login(@Body user: UserCredentials): Response<AuthResponse>

    @GET("system/ping")
    suspend fun ping(): Response<PingResponse>

    @GET("novels")
    suspend fun getUserNovels(@Header("Authorization") token: String): Response<List<NovelBackend>>

    @POST("novels")
    suspend fun createNovel(
        @Header("Authorization") token: String,
        @Body novel: NovelBackend
    ): Response<Unit>

    @PUT("novels/{id}")
    suspend fun updateNovel(
        @Header("Authorization") token: String,
        @Path("id") id: String,
        @Body novel: NovelBackend
    ): Response<Unit>

    @DELETE("novels/{id}")
    suspend fun deleteNovel(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): Response<Unit>
}
