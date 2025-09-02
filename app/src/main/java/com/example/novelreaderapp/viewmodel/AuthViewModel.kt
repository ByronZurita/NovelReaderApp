package com.example.novelreaderapp.viewmodel

import android.app.Application
import android.util.Base64
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.novelreaderapp.backendconnection.UserRepository
import com.example.novelreaderapp.backendconnection.backmodels.NovelBackend
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject

class AuthViewModel(
    application: Application,
    private val repository: UserRepository
) : AndroidViewModel(application) {

    // ✅ First: auth token
    private val _authToken = MutableStateFlow<String?>(repository.getToken())
    val authToken: StateFlow<String?> = _authToken

    // ✅ Then: fields that depend on token
    private val _userId = MutableStateFlow<String?>(getUserIdFromToken(_authToken.value))
    val userId: StateFlow<String?> = _userId

    private val _username = MutableStateFlow<String?>(getUsernameFromToken(_authToken.value))
    val username: StateFlow<String?> = _username

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _userNovels = MutableStateFlow<List<NovelBackend>>(emptyList())
    val userNovels: StateFlow<List<NovelBackend>> = _userNovels

    val isLoggedIn: Boolean
        get() = _authToken.value != null

    init {
        // Auto-load novels if already logged in
        if (isLoggedIn) fetchUserNovels()
    }

    fun sendHealthPing() {
        viewModelScope.launch {
            try {
                repository.healthPing()
            } catch (_: Exception) {}
        }
    }

    fun login(username: String, password: String) {
        viewModelScope.launch {
            val result = repository.login(username, password)
            result.onSuccess { token ->
                _authToken.value = token
                repository.saveToken(token)
                _userId.value = getUserIdFromToken(token)
                _username.value = getUsernameFromToken(token)
                _error.value = null
                fetchUserNovels()
            }.onFailure {
                _error.value = it.message
            }
        }
    }

    fun register(username: String, password: String) {
        viewModelScope.launch {
            val result = repository.register(username, password)
            result.onSuccess { token ->
                _authToken.value = token
                repository.saveToken(token)
                _userId.value = getUserIdFromToken(token)
                _username.value = getUsernameFromToken(token)
                _error.value = null
                fetchUserNovels()
            }.onFailure {
                _error.value = it.message
            }
        }
    }

    fun logout() {
        repository.clearToken()
        _authToken.value = null
        _username.value = null
        _userNovels.value = emptyList()
    }

    private fun getUsernameFromToken(token: String?): String? {
        if (token == null) return null
        return try {
            val parts = token.split(".")
            if (parts.size != 3) return null
            val payload = String(Base64.decode(parts[1], Base64.DEFAULT))
            val json = JSONObject(payload)
            json.optString("username")
        } catch (e: Exception) {
            null
        }
    }

    private fun getUserIdFromToken(token: String?): String? {
        if (token == null) return null
        return try {
            val parts = token.split(".")
            if (parts.size != 3) return null
            val payload = String(Base64.decode(parts[1], Base64.DEFAULT))
            Log.d("JWT", "Payload: $payload")  // Debug output to check claims
            val json = JSONObject(payload)
            val userId = json.optString("userId").takeIf { it.isNotEmpty() }
                ?: json.optString("_id").takeIf { it.isNotEmpty() }
                ?: json.optString("id").takeIf { it.isNotEmpty() }
                ?: json.optString("sub") // common fallback
            Log.d("JWT", "Parsed userId: $userId")  // Debug output to check extracted userId
            userId
        } catch (e: Exception) {
            Log.e("JWT", "Error parsing userId from token", e)
            null
        }
    }

    fun fetchUserNovels() {
        viewModelScope.launch {
            val token = _authToken.value ?: return@launch
            val result = repository.getUserNovels(token)
            result.onSuccess {
                _userNovels.value = it
            }.onFailure {
                _error.value = it.message
            }
        }
    }

    fun createNovel(novel: NovelBackend, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            val token = _authToken.value ?: return@launch
            val result = repository.createNovel("Bearer $token", novel)
            result.onSuccess {
                fetchUserNovels()  // Refresh the list after a successful create
                onResult(true, null)
            }.onFailure {
                onResult(false, it.message)
            }
        }
    }

    fun updateNovel(novelId: String, novel: NovelBackend, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            val token = _authToken.value ?: return@launch
            val result = repository.updateNovel("Bearer $token", novelId, novel)
            result.onSuccess {
                fetchUserNovels()  // Refresh after update
                onResult(true, null)
            }.onFailure {
                onResult(false, it.message)
            }
        }
    }

    fun deleteNovel(novelId: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            val token = _authToken.value ?: return@launch
            val result = repository.deleteNovel("Bearer $token", novelId)
            result.onSuccess {
                fetchUserNovels()  // Refresh after delete
                onResult(true, null)
            }.onFailure {
                onResult(false, it.message)
            }
        }
    }
}
