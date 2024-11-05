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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val _authState = MutableStateFlow<AuthState>(AuthState.Initial)
    private lateinit var googleSignInClient: GoogleSignInClient
    val authState: StateFlow<AuthState> = _authState

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

    fun signUp(email: String, password: String) {
        viewModelScope.launch {
            try {
                _authState.value = AuthState.Loading
                val result = auth.createUserWithEmailAndPassword(email, password).await()
                result.user?.let {
                    _authState.value = AuthState.Authenticated(it.uid)
                } ?: run {
                    _authState.value = AuthState.Error("Registration failed")
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Registration failed")
            }
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
    object Initial : AuthState()
    object Loading : AuthState()
    object Unauthenticated : AuthState()
    data class Authenticated(val userId: String) : AuthState()
    data class Error(val message: String) : AuthState()
}