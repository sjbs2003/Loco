package com.example.loco.viewModel

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val _authState = MutableStateFlow<AuthState>(AuthState.Initial)
    private lateinit var googleSignInClient: GoogleSignInClient
    val authState: StateFlow<AuthState> = _authState

    fun getCurrentUser() = auth.currentUser
    fun getCurrentUserId(): String? = auth.currentUser?.uid

    init {
        checkCurrentUser()
    }

    private fun checkCurrentUser(){
        auth.currentUser?.let {
            _authState.value = AuthState.Authenticated(it.uid)
        } ?: run {
            _authState.value = AuthState.Unauthenticated
        }
    }

    fun initializeGoogleSignIn(context: Context, webClientId: String){
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestIdToken(webClientId).requestEmail().build()
        googleSignInClient = GoogleSignIn.getClient(context,gso)
    }

    fun signUp(email: String, password: String) {
        viewModelScope.launch {
            try {
                _authState.value = AuthState.Loading
                // Create auth user
                val result = auth.createUserWithEmailAndPassword(email, password).await()

                result.user?.let { firebaseUser ->
                    // Create user document in Firestore
                    val user = hashMapOf(
                        "userId" to firebaseUser.uid,
                        "email" to email,
                        "createdAt" to FieldValue.serverTimestamp()
                    )

                    try {
                        // Save user data to Firestore
                        firestore.collection("users")
                            .document(firebaseUser.uid)
                            .set(user)
                            .await()

                        _authState.value = AuthState.Authenticated(firebaseUser.uid)
                    } catch (e: Exception) {
                        // If Firestore save fails, delete the auth user
                        firebaseUser.delete().await()
                        _authState.value = AuthState.Error("Failed to create user profile")
                    }
                } ?: run {
                    _authState.value = AuthState.Error("Registration failed")
                }
            } catch (e: Exception) {
                when {
                    e.message?.contains("email already in use", ignoreCase = true) == true -> {
                        _authState.value = AuthState.Error("Email already in use")
                    }
                    e.message?.contains("weak password", ignoreCase = true) == true -> {
                        _authState.value = AuthState.Error("Password is too weak")
                    }
                    else -> {
                        _authState.value = AuthState.Error(e.message ?: "Registration failed")
                    }
                }
            }
        }
    }

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            try {
                _authState.value = AuthState.Loading
                val result = auth.signInWithEmailAndPassword(email, password).await()
                result.user?.let {
                    _authState.value = AuthState.Authenticated(it.uid)
                } ?: run {
                    _authState.value = AuthState.Error("Authentication failed")
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Authentication failed")
            }
        }
    }

    fun getGoogleSignInIntent(): Intent {
        return googleSignInClient.signInIntent
    }

    fun handleGoogleSignInResult(data: Intent?){
        viewModelScope.launch {
            try{
                _authState.value = AuthState.Loading
                val account = GoogleSignIn.getSignedInAccountFromIntent(data).await()
                val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                val result = auth.signInWithCredential(credential).await()
                handleAuthResult(result)
            }catch (e:ApiException){
                _authState.value = AuthState.Error("Google sign in failed: ${e.statusCode}")
            }catch (e: Exception){
                _authState.value = AuthState.Error(e.message ?: "Google sign in failed")
            }
        }
    }

    private fun handleAuthResult(authResult: com.google.firebase.auth.AuthResult){
        authResult.user?.let {
            _authState.value= AuthState.Authenticated(it.uid)
        }?:run {
            _authState.value = AuthState.Error("Authentication Failded")
        }
    }

    fun signOut() {
        auth.signOut()
        _authState.value = AuthState.Unauthenticated
    }

    companion object {
        const val RC_SIGN_IN = 9001
    }
}

sealed class AuthState {
    data object Initial : AuthState()
    data object Loading : AuthState()
    data object Unauthenticated : AuthState()
    data class Authenticated(val userId: String) : AuthState()
    data class Error(val message: String) : AuthState()
}