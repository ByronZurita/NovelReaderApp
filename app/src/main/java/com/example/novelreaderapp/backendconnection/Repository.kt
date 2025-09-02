package com.example.novelreaderapp.backendconnection

import com.example.novelreaderapp.backendconnection.backmodels.UserCredentials
import android.content.Context
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.example.novelreaderapp.backendconnection.backmodels.NovelBackend

class UserRepository(
    private val context: Context,
    private val api: ByronApi
) {

    private val prefs by lazy {
        val masterKey = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        EncryptedSharedPreferences.create(
            "auth_prefs",
            masterKey,
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    private val TOKEN_KEY = "jwt_token"

    // Save token after login/register
    fun saveToken(token: String) {
        prefs.edit().putString(TOKEN_KEY, token).apply()
    }

    fun getToken(): String? {
        return prefs.getString(TOKEN_KEY, null)
    }

    fun clearToken() {
        prefs.edit().remove(TOKEN_KEY).apply()
    }

    suspend fun register(username: String, password: String): Result<String> {
        return try {
            val response = api.register(UserCredentials(username, password))
            if (response.isSuccessful) {
                Result.success(response.body()?.token.orEmpty())
            } else {
                Result.failure(Exception("Registration failed: ${response.errorBody()?.string()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun login(username: String, password: String): Result<String> {
        return try {
            val response = api.login(UserCredentials(username, password))
            if (response.isSuccessful) {
                Result.success(response.body()?.token.orEmpty())
            } else {
                Result.failure(Exception("Login failed: ${response.errorBody()?.string()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun healthPing(): Result<String> {
        return try {
            val response = api.ping()
            if (response.isSuccessful) {
                val message = response.body()?.message ?: "No message"
                Result.success(message)
            } else {
                Result.failure(Exception("Health ping failed: ${response.errorBody()?.string()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserNovels(token: String): Result<List<NovelBackend>> {
        return try {
            val response = api.getUserNovels("Bearer $token")
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                Result.failure(Exception("Failed to fetch novels: ${response.code()} ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    suspend fun createNovel(token: String, novel: NovelBackend): Result<Unit> {
        return try {
            // Remove any "Bearer " prefix if user mistakenly added
            val cleanToken = token.removePrefix("Bearer ").trim()
            val authHeader = "Bearer $cleanToken"
            Log.d("Repository", "Creating novel with auth header: $authHeader")

            val response = api.createNovel(authHeader, novel)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("Repository", "Failed to create novel: ${response.code()} $errorBody")
                Result.failure(Exception("Error ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            Log.e("Repository", "Exception in createNovel", e)
            Result.failure(e)
        }
    }


    suspend fun updateNovel(token: String, novelId: String, novel: NovelBackend): Result<Unit> {
        return try {
            val response = api.updateNovel(token, novelId, novel)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Error ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteNovel(token: String, novelId: String): Result<Unit> {
        return try {
            val response = api.deleteNovel(token, novelId)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Error ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
