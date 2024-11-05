package com.example.loco

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import com.example.loco.ui.AppViewModelProvider
import com.example.loco.ui.theme.LocoTheme
import com.example.loco.viewModel.AuthViewModel
import com.google.firebase.FirebaseApp

class MainActivity : ComponentActivity() {

    private lateinit var authViewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Firebase first
        if (FirebaseApp.getApps(this).isEmpty()) {
            FirebaseApp.initializeApp(this)
        }

        // Initialize AuthViewModel using ViewModelProvider
        authViewModel = ViewModelProvider(
            this,
            AppViewModelProvider.Factory
        )[AuthViewModel::class.java]

        // Initialize Google Sign In
        authViewModel.initializeGoogleSignIn(
            context = this,
            webClientId = getString(R.string.web_client_id)
        )

        enableEdgeToEdge()
        setContent {
            LocoTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NoteApp()
                }
            }
        }
    }
}
