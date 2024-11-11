package com.example.loco.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.loco.AppViewModelProvider
import com.example.loco.R
import com.example.loco.viewModel.AuthState
import com.example.loco.viewModel.AuthViewModel

@Preview(showBackground = true)
@Composable
fun AuthScreen(
    onLoginSuccess: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: AuthViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val colorScheme = MaterialTheme.colorScheme
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        viewModel.handleGoogleSignInResult(result.data)
    }

    val authState by viewModel.authState.collectAsState()
    var showSignInDialog by remember { mutableStateOf(false) }
    var showSignUpDialog by remember { mutableStateOf(false) }

    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Authenticated -> onLoginSuccess()
            is AuthState.Error -> {
                // Handle error state
            }
            else -> {}
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.surface)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(colorScheme.primaryContainer, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(R.drawable.app_logo),
                contentDescription = "App Logo",
                modifier = Modifier.fillMaxSize()
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Keep It Forever",
            style = MaterialTheme.typography.headlineMedium,
            color = colorScheme.onSurface,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "It's free to sync and see your content\non any devices",
            style = MaterialTheme.typography.bodyLarge,
            color = colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(48.dp))

        // Google Sign In Button
        Button(
            onClick = { launcher.launch(viewModel.getGoogleSignInIntent()) },
            modifier = Modifier.height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = colorScheme.surface,
                contentColor = colorScheme.onSurface
            ),
            shape = RoundedCornerShape(28.dp),
            border = BorderStroke(1.dp, colorScheme.outline.copy(alpha = 0.5f))
        ) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Image(
                    painter = painterResource(id = R.drawable.google),
                    contentDescription = "Google Icon",
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Sign in with Google",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Email Sign In Button
        Button(
            onClick = { showSignInDialog = true },
            modifier = Modifier.height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = colorScheme.surface,
                contentColor = colorScheme.onSurface
            ),
            shape = RoundedCornerShape(28.dp),
            border = BorderStroke(1.dp, colorScheme.outline.copy(alpha = 0.5f))
        ) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Default.Email,
                    contentDescription = "Email Icon",
                    modifier = Modifier.size(24.dp),
                    tint = colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Sign in with Mail",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        TextButton(
            onClick = { /* Handle skip sign up */ },
            colors = ButtonDefaults.textButtonColors(contentColor = colorScheme.primary)
        ) {
            Text(
                text = "Skip Sign Up",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }

    // Sign In Dialog
    if (showSignInDialog) {
        Dialog(
            onDismissRequest = { showSignInDialog = false }
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.DarkGray)
            ) {
                var email by remember { mutableStateOf("") }
                var password by remember { mutableStateOf("") }
                var isPassVisible by remember { mutableStateOf(false) }
                var errorMessage by remember { mutableStateOf<String?>(null) }

                // Handle auth state changes specifically for the dialog
                LaunchedEffect(authState) {
                    when (authState) {
                        is AuthState.Error -> {
                            errorMessage = (authState as AuthState.Error).message
                        }
                        is AuthState.Authenticated -> {
                            showSignInDialog = false
                            errorMessage = null
                        }
                        else -> {
                            errorMessage = null
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Sign In",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    OutlinedTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            errorMessage = null
                        },
                        label = { Text("Email", color = Color.White) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Cyan,
                            unfocusedBorderColor = Color.Gray,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        isError = errorMessage != null
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            errorMessage = null
                        },
                        label = { Text("Password", color = Color.White) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        visualTransformation = if (isPassVisible)
                            VisualTransformation.None
                        else
                            PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { isPassVisible = !isPassVisible }) {
                                Icon(
                                    imageVector = if (isPassVisible)
                                        Icons.Default.VisibilityOff
                                    else
                                        Icons.Default.Visibility,
                                    contentDescription = if (isPassVisible)
                                        "Hide Password"
                                    else
                                        "Show Password",
                                    tint = Color.White
                                )
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Cyan,
                            unfocusedBorderColor = Color.Gray,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        isError = errorMessage != null
                    )

                    // Error message
                    if (errorMessage != null) {
                        Text(
                            text = errorMessage ?: "",
                            color = Color.Red,
                            modifier = Modifier.padding(top = 8.dp),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            if (email.isBlank() || password.isBlank()) {
                                errorMessage = "Please fill in all fields"
                                return@Button
                            }
                            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                                errorMessage = "Please enter a valid email"
                                return@Button
                            }
                            viewModel.signIn(email, password)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Cyan,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(8.dp),
                        enabled = authState !is AuthState.Loading
                    ) {
                        if (authState is AuthState.Loading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White
                            )
                        } else {
                            Text("Sign In", color = Color.Black)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    TextButton(onClick = {
                        showSignInDialog = false
                        showSignUpDialog = true
                    }) {
                        Text(
                            "Don't have an account?",
                            color = Color.Cyan
                        )
                    }
                }
            }
        }
    }

    // Sign In Dialog
    if (showSignInDialog) {
        Dialog(onDismissRequest = { showSignInDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = colorScheme.surface)
            ) {
                var email by remember { mutableStateOf("") }
                var password by remember { mutableStateOf("") }
                var isPassVisible by remember { mutableStateOf(false) }
                var errorMessage by remember { mutableStateOf<String?>(null) }

                LaunchedEffect(authState) {
                    when (authState) {
                        is AuthState.Error -> {
                            errorMessage = (authState as AuthState.Error).message
                        }
                        is AuthState.Authenticated -> {
                            showSignInDialog = false
                            errorMessage = null
                        }
                        else -> {
                            errorMessage = null
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Sign In",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    OutlinedTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            errorMessage = null
                        },
                        label = { Text("Email", color = colorScheme.onSurfaceVariant) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colorScheme.primary,
                            unfocusedBorderColor = colorScheme.outline,
                            focusedTextColor = colorScheme.onSurface,
                            unfocusedTextColor = colorScheme.onSurface,
                            focusedLabelColor = colorScheme.primary,
                            unfocusedLabelColor = colorScheme.onSurfaceVariant
                        ),
                        isError = errorMessage != null
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            errorMessage = null
                        },
                        label = { Text("Password", color = colorScheme.onSurfaceVariant) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        visualTransformation = if (isPassVisible)
                            VisualTransformation.None
                        else
                            PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { isPassVisible = !isPassVisible }) {
                                Icon(
                                    imageVector = if (isPassVisible)
                                        Icons.Default.VisibilityOff
                                    else
                                        Icons.Default.Visibility,
                                    contentDescription = if (isPassVisible)
                                        "Hide Password"
                                    else
                                        "Show Password",
                                    tint = colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colorScheme.primary,
                            unfocusedBorderColor = colorScheme.outline,
                            focusedTextColor = colorScheme.onSurface,
                            unfocusedTextColor = colorScheme.onSurface,
                            focusedLabelColor = colorScheme.primary,
                            unfocusedLabelColor = colorScheme.onSurfaceVariant
                        ),
                        isError = errorMessage != null
                    )

                    if (errorMessage != null) {
                        Text(
                            text = errorMessage ?: "",
                            color = colorScheme.error,
                            modifier = Modifier.padding(top = 8.dp),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            if (email.isBlank() || password.isBlank()) {
                                errorMessage = "Please fill in all fields"
                                return@Button
                            }
                            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                                errorMessage = "Please enter a valid email"
                                return@Button
                            }
                            viewModel.signIn(email, password)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colorScheme.primary,
                            contentColor = colorScheme.onPrimary
                        ),
                        shape = RoundedCornerShape(8.dp),
                        enabled = authState !is AuthState.Loading
                    ) {
                        if (authState is AuthState.Loading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = colorScheme.onPrimary
                            )
                        } else {
                            Text("Sign In")
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    TextButton(onClick = {
                        showSignInDialog = false
                        showSignUpDialog = true
                    }) {
                        Text(
                            "Don't have an account?",
                            color = colorScheme.primary
                        )
                    }
                }
            }
        }
    }

    // Sign Up Dialog
    if (showSignUpDialog) {
        Dialog(onDismissRequest = { showSignUpDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = colorScheme.surface)
            ) {
                var email by remember { mutableStateOf("") }
                var password by remember { mutableStateOf("") }
                var confirmPassword by remember { mutableStateOf("") }
                var isPassVisible by remember { mutableStateOf(false) }
                var isConfirmPassVisible by remember { mutableStateOf(false) }
                var errorMessage by remember { mutableStateOf<String?>(null) }

                LaunchedEffect(authState) {
                    when (authState) {
                        is AuthState.Error -> {
                            errorMessage = (authState as AuthState.Error).message
                        }
                        is AuthState.Authenticated -> {
                            showSignUpDialog = false
                            errorMessage = null
                        }
                        else -> {
                            errorMessage = null
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Sign Up",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    OutlinedTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            errorMessage = null
                        },
                        label = { Text("Email", color = colorScheme.onSurfaceVariant) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colorScheme.primary,
                            unfocusedBorderColor = colorScheme.outline,
                            focusedTextColor = colorScheme.onSurface,
                            unfocusedTextColor = colorScheme.onSurface,
                            focusedLabelColor = colorScheme.primary,
                            unfocusedLabelColor = colorScheme.onSurfaceVariant
                        ),
                        isError = errorMessage != null
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            errorMessage = null
                        },
                        label = { Text("Password", color = colorScheme.onSurfaceVariant) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        visualTransformation = if (isPassVisible)
                            VisualTransformation.None
                        else
                            PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { isPassVisible = !isPassVisible }) {
                                Icon(
                                    imageVector = if (isPassVisible)
                                        Icons.Default.VisibilityOff
                                    else
                                        Icons.Default.Visibility,
                                    contentDescription = if (isPassVisible)
                                        "Hide Password"
                                    else
                                        "Show Password",
                                    tint = colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colorScheme.primary,
                            unfocusedBorderColor = colorScheme.outline,
                            focusedTextColor = colorScheme.onSurface,
                            unfocusedTextColor = colorScheme.onSurface,
                            focusedLabelColor = colorScheme.primary,
                            unfocusedLabelColor = colorScheme.onSurfaceVariant
                        ),
                        isError = errorMessage != null
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = {
                            confirmPassword = it
                            errorMessage = null
                        },
                        label = { Text("Confirm Password", color = colorScheme.onSurfaceVariant) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        visualTransformation = if (isConfirmPassVisible)
                            VisualTransformation.None
                        else
                            PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { isConfirmPassVisible = !isConfirmPassVisible }) {
                                Icon(
                                    imageVector = if (isConfirmPassVisible)
                                        Icons.Default.VisibilityOff
                                    else
                                        Icons.Default.Visibility,
                                    contentDescription = if (isConfirmPassVisible)
                                        "Hide Password"
                                    else
                                        "Show Password",
                                    tint = colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colorScheme.primary,
                            unfocusedBorderColor = colorScheme.outline,
                            focusedTextColor = colorScheme.onSurface,
                            unfocusedTextColor = colorScheme.onSurface,
                            focusedLabelColor = colorScheme.primary,
                            unfocusedLabelColor = colorScheme.onSurfaceVariant
                        ),
                        isError = errorMessage != null
                    )

                    if (errorMessage != null) {
                        Text(
                            text = errorMessage ?: "",
                            color = colorScheme.error,
                            modifier = Modifier.padding(top = 8.dp),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            when {
                                email.isBlank() || password.isBlank() || confirmPassword.isBlank() -> {
                                    errorMessage = "Please fill in all fields"
                                }
                                !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                                    errorMessage = "Please enter a valid email"
                                }
                                password.length < 6 -> {
                                    errorMessage = "Password must be at least 6 characters"
                                }
                                password != confirmPassword -> {
                                    errorMessage = "Passwords don't match"
                                }
                                else -> {
                                    viewModel.signUp(email, password)
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colorScheme.primary,
                            contentColor = colorScheme.onPrimary
                        ),
                        shape = RoundedCornerShape(8.dp),
                        enabled = authState !is AuthState.Loading
                    ) {
                        if (authState is AuthState.Loading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = colorScheme.onPrimary
                            )
                        } else {
                            Text("Sign Up")
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    TextButton(onClick = {
                        showSignUpDialog = false
                        showSignInDialog = true
                    }) {
                        Text(
                            "Already have an account?",
                            color = colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}