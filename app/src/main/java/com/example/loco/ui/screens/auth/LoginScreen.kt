@file:Suppress("PreviewAnnotationInFunctionWithParameters")

package com.example.loco.ui.screens.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.loco.R
//import com.google.firebase.Firebase
//import com.google.firebase.auth.FirebaseAuth
//import com.google.firebase.auth.GoogleAuthProvider
//import com.google.firebase.auth.auth
//import kotlinx.coroutines.tasks.await

//enum class LocoScreen(val route: String) {
//    NoteList("noteList"),
//    NoteDetail("noteDetail/{noteId}"),
//    NoteCreate("noteCreate/{noteId}"),
//    NoteCreateNew("noteCreateNew")
//}
//
//@Composable
//fun AppNavigation() {
//    val navController = rememberNavController()
//
//    NavHost(navController = navController, startDestination = "authScreen") {
//        composable("authScreen") { }
//    }
//}
//
//
@Preview(showBackground = true)
@Composable
fun AuthScreen(
////    onNavigateToHome: () -> Unit,
////    onNavigateToSignUp: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isPassVisible by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
//        verticalArrangement = Arrangement.Center
    )
    {
        Box(
            modifier = Modifier
                .size(180.dp)  // Avatar size
                .clip(CircleShape)  // Clip to a circular shape
                .border(
                    BorderStroke(2.dp, Color.LightGray),  // Optional: Border around the avatar
                    CircleShape
                )
        ) {
            Image(
                painter = painterResource(R.drawable.logo),
                contentDescription = "App Logo",
                modifier = Modifier
                    .size(300.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = stringResource(R.string.welcome_back),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(20.dp))
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
//            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(size = 15.dp),
                    colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.Transparent,
            focusedIndicatorColor = Color.Black,  // Remove the outline when focused
            unfocusedIndicatorColor = Color.Black,
        ),
        textStyle = TextStyle(fontSize = 20.sp),
        placeholder = {
            Text(
                text = "Example@youremail.com",
                color = Color.Gray
            )
        },
            trailingIcon = {
                if (email.isNotEmpty()) {
                    IconButton(onClick = { email = "" }) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Clear text",
                            tint = Color.Black
                        )
                    }
                }
            }
        )
        Spacer(modifier = Modifier.height(10.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
//            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(size = 15.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Black,  // Remove the outline when focused
                unfocusedIndicatorColor = Color.Black,
            ),
            textStyle = TextStyle(fontSize = 20.sp),
            placeholder = {
                Text(
                    text = "*********",
                    color = Color.Gray
                )
            },
            trailingIcon = {
                IconButton(onClick = { isPassVisible = !isPassVisible }) {
                    Icon(

                        imageVector = if (isPassVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                        contentDescription = if (isPassVisible) "Hide Password" else "Show Password"
                    )
                }
            }
        )

        Spacer(modifier = Modifier.height(20.dp))
        Button(
            onClick = {}, modifier = Modifier
                .height(58.dp)
                .width(335.dp)
                .align(Alignment.CenterHorizontally),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF007AFF))
        ) {
            Text(
                text = "Log In",
                fontSize = 16.sp,
//                fontFamily = poppins,
                fontWeight = FontWeight.Medium
            )
        }
        Spacer(modifier = Modifier.height(20.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Don't have an Account?", fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = Color.Black)
            TextButton(onClick = { /*TODO*/ }) {
                Text(text = "Sign Up", fontSize = 15.sp, color = Color.DarkGray)
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
        Text(text = "Or login with", fontSize = 15.sp, color = Color.Gray, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(20.dp))
        Row {
            IconButton(onClick = { /*TODO*/ }) {

            }
            Spacer(modifier = Modifier.width(20.dp))
            IconButton(onClick = { /*TODO*/ }) {

            }
        }

    }
}
//

//    val context = LocalContext.current
//    val coroutineScope = rememberCoroutineScope()
//    var isLoading by remember { mutableStateOf(false) }
//
//    // Firebase Authentication instance
////    val auth = FirebaseAuth.getInstance()
//
//
//    // Define what happens when authentication completes or fails
////    val authLauncher = authLauncher(
////        onAuthComplete = { authResult ->
////            // Handle successful Google login
////            // For example, navigate to a new screen or display a success message
////            val user = authResult.user
////            println("User signed in successfully: ${user?.displayName}")
////        },
////        onAuthError = {exception ->
////            // Handle authentication error
////            println("Google Sign-in Failed: ${exception.message}")
////        }
////    )
//
//    Column(
//        modifier = modifier
//            .fillMaxSize()
//            .padding(16.dp),
//        horizontalAlignment = Alignment.CenterHorizontally,
//        verticalArrangement = Arrangement.Center
//    ) {
//        Image(painter = painterResource(R.drawable.logo), contentDescription = "App Logo")
//
//        Spacer(modifier = modifier.height(20.dp))
//
//        Text(
//            text = stringResource(R.string.welcome_back),
//            style = MaterialTheme.typography.headlineSmall
//        )
//        Spacer(modifier = modifier.height(20.dp))
//
//        OutlinedTextField(
//            value = email.value,
//            onValueChange = { email.value = it },
//            label = { Text("Email") },
//            modifier = modifier.fillMaxWidth()
//        )
//        Spacer(modifier = modifier.height(10.dp))
//
//        OutlinedTextField(
//            value = password.value,
//            onValueChange = { password.value = it },
//            label = { Text("Password") },
//            modifier = modifier.fillMaxWidth(),
//            visualTransformation = PasswordVisualTransformation()
//        )
//        Spacer(modifier = modifier.height(20.dp))
//
//        // handle email/password login
//        Button(
//            onClick = {
//                if (email.value.isNotEmpty() && password.value.isNotEmpty()) {
//                    isLoading = true
//                    coroutineScope.launch {
//                        try {
////                            val authResult = auth.signInWithEmailAndPassword(
////                                email.value.trim(),
////                                password.value.trim()
////                            ).await()
//
//                            // Login successful, navigate to home screen or display a success message
////                            val user = authResult.user
//                            isLoading = false
////                            println("Login successful: ${user?.email}")
////                            onNavigateToHome() // navigate to home screen
//
//                        } catch (e: Exception) {
//                            isLoading = false
//                            println("Login failed: ${e.message}")
//                        }
//                    }
//                } else {
//                    // Display an error for empty fields
//                    println("Email or password cannot be empty")
//                }
//            },
//            modifier = modifier.fillMaxWidth(),
//            enabled = !isLoading // Disable button while loading
//        ) {
//            Text(stringResource(R.string.log_in))
//        }
//        Spacer(modifier = modifier.height(20.dp))
//        Text(text = "or")
//        Spacer(modifier = modifier.height(20.dp))
//        TextButton(onClick = { /*onNavigateToSignUp()*/ }) {
//            Text("Don't have an account? Sign Up")
//        }
//    }
//    GoogleSignInButton(
//        onClick = { // set up Google Sign-in options
////            val googleSignInClient = GoogleSignIn.getClient(
////                context,
////                GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
////                    .requestIdToken(context.getString(R.string.web_id))
////                    .requestEmail()
////                    .build()
////            )
////            val signInIntent = googleSignInClient.signInIntent
////            authLauncher.launch(signInIntent)
//        },
//        modifier = Modifier.fillMaxWidth()
//    )
//    Spacer(modifier = modifier.height(10.dp))

//@Composable
//fun GoogleSignInButton(
//    onClick: () -> Unit,
//    modifier: Modifier = Modifier,
//) {
//    Button(
//        onClick = onClick,
//        modifier = modifier.fillMaxWidth(),
//        colors = ButtonDefaults.buttonColors(Color.White)
//    ) {
//        Icon(
//            painter = painterResource(R.drawable.google),
//            contentDescription = "Google Icon",
//            tint = Color.Unspecified,
//            modifier = modifier.size(24.dp)
//        )
//        Spacer(modifier = modifier.width(8.dp))
//        Text("Continue with Google", color = Color.Black)
//    }
//}
//
//
//@Composable
//fun authLauncher(
//    onAuthComplete: (AuthResult) -> Unit,
//    onAuthError: (ApiException) -> Unit,
//): ManagedActivityResultLauncher<Intent, ActivityResult> {
//
//    val coroutineScope = rememberCoroutineScope()
//    return rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
////        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
////        try {
////            val account = task.getResult(ApiException::class.java)!!
////            val credential = GoogleAuthProvider.getCredential(account.idToken!!, null)
////
////            coroutineScope.launch {
////                val authResult = Firebase.auth.signInWithCredential(credential).await()
////                onAuthComplete(authResult)
////            }
////        }catch (e: ApiException){
////            onAuthError(e)
////        }
//    }
//}