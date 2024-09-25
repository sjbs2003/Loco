package com.example.loco.ui.screens.login

import android.content.Intent
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.loco.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.Firebase
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


@Composable
fun AuthScreen(modifier: Modifier = Modifier){

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val email = remember { mutableStateOf("") }
    val password = remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    // Firebase Authentication instance
    val auth = FirebaseAuth.getInstance()


    // Define what happens when authentication completes or fails
    val authLauncher = authLauncher(
        onAuthComplete = { authResult ->
            // Handle successful Google login
            // For example, navigate to a new screen or display a success message
            val user = authResult.user
            println("User signed in successfully: ${user?.displayName}")
        },
        onAuthError = {exception ->
            // Handle authentication error
            println("Google Sign-in Failed: ${exception.message}")
        }
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(painter = painterResource(R.drawable.logo), contentDescription = "App Logo")

        Spacer(modifier = modifier.height(20.dp))

        Text(
            text = stringResource(R.string.welcome_back),
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(modifier = modifier.height(20.dp))

        OutlinedTextField(
            value = email.value,
            onValueChange = { email.value = it },
            label = { Text("Email") },
            modifier = modifier.fillMaxWidth()
        )
        Spacer(modifier = modifier.height(10.dp))

        OutlinedTextField(
            value = password.value,
            onValueChange = { password.value = it },
            label = { Text("Password") },
            modifier = modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation()
        )
        Spacer(modifier = modifier.height(20.dp))

        // handle email/password login
        Button(
            onClick = {
                if (email.value.isNotEmpty() && password.value.isNotEmpty()){
                    isLoading = true
                    coroutineScope.launch {
                        try {
                            val authResult = auth.signInWithEmailAndPassword(
                                email.value.trim(),
                                password.value.trim()
                            ).await()

                            // Login successful, navigate to home screen or display a success message
                            val user = authResult.user
                            isLoading = false
                            println("Login successful: ${user?.email}")
                            TODO("Navigate to the next screen or display a message")

                        } catch (e: Exception){
                            isLoading = false
                            println("Login failed: ${e.message}")
                        }
                    }
                }else{
                    // Display an error for empty fields
                    println("Email or password cannot be empty")
                }
            },
            modifier = modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.log_in))
        }
        Spacer(modifier = modifier.height(20.dp))
        Text(text = "or")
        Spacer(modifier = modifier.height(20.dp))
    }
    GoogleSignInButton(
        onClick = { // set up Google Sign-in options
            val googleSignInClient = GoogleSignIn.getClient(
                context,
                GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(context.getString(R.string.web_id))
                    .requestEmail()
                    .build()
            )
            val signInIntent = googleSignInClient.signInIntent
            authLauncher.launch(signInIntent)
                  },
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(modifier = modifier.height(10.dp))

    TextButton(onClick = { TODO("navigate to sign-up Screen") }) {
        Text("Don't have an account? Sign Up")
    }
}

@Composable
fun GoogleSignInButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(Color.White)
    ) {
        Icon(
            painter = painterResource(R.drawable.google),
            contentDescription = "Google Icon",
            tint = Color.Unspecified,
            modifier = modifier.size(24.dp)
        )
        Spacer(modifier = modifier.width(8.dp))
        Text("Continue with Google", color = Color.Black)
    }
}


@Composable
fun authLauncher(
    onAuthComplete: (AuthResult) -> Unit,
    onAuthError: (ApiException) -> Unit
): ManagedActivityResultLauncher<Intent, ActivityResult> {

    val coroutineScope = rememberCoroutineScope()
    return rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)!!
            val credential = GoogleAuthProvider.getCredential(account.idToken!!, null)

            coroutineScope.launch {
                val authResult = Firebase.auth.signInWithCredential(credential).await()
                onAuthComplete(authResult)
            }
        }catch (e: ApiException){
            onAuthError(e)
        }
    }
}