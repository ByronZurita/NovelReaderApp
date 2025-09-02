package com.example.novelreaderapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.lifecycle.ViewModelProvider
import com.example.novelreaderapp.backendconnection.RetrofitClient
import com.example.novelreaderapp.backendconnection.UserRepository
import com.example.novelreaderapp.ui.screens.common.AppNavigation
import com.example.novelreaderapp.viewmodel.*
import com.example.novelreaderapp.viewmodel.factories.AuthViewModelFactory

/**
 * MainActivity — Application Entry Point.
 *
 * Responsibilities:
 * - Initialize ViewModels
 * - Define Navigation Graph
 * - Apply App Theme
 */
@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {

    // Persistent ViewModels
    private val settingsViewModel: SettingsViewModel by viewModels()
    private val chapterViewModel: ChapterViewModel by viewModels()
    private lateinit var authViewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize AuthViewModel with custom factory
        val api = RetrofitClient.api
        val repository = UserRepository(applicationContext, api)
        val factory = AuthViewModelFactory(application, repository)
        authViewModel = ViewModelProvider(this, factory)[AuthViewModel::class.java]

        setContent {
            AppNavigation(
                authViewModel = authViewModel,
                chapterViewModel = chapterViewModel,
                settingsViewModel = settingsViewModel
            )
        }
    }
}
