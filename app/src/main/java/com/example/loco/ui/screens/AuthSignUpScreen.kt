package com.example.loco.ui.screens


import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.loco.ui.AppViewModelProvider
import com.example.loco.viewModel.AuthState
import com.example.loco.viewModel.AuthViewModel


@Composable
fun SignUp(
    onSignUpSuccess: () -> Unit,
    onBackToLogin: () -> Unit,
    viewModel: AuthViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val email by remember { mutableStateOf("") }
    val password by remember { mutableStateOf("") }
    val confirmPassword by remember { mutableStateOf("") }

    val authState by viewModel.authState.collectAsState()

    // Form validation states
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var confirmPasswordError by remember { mutableStateOf<String?>(null) }

    // Handle authentication state changes
    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Authenticated -> onSignUpSuccess()
            is AuthState.Error -> {
                // Show error message
                // You could add a SnackBar or other error UI here
            }
            else -> {}
        }
    }

    // Validation function
    fun validateForm(): Boolean {
        var isValid = true

        // Email validation
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailError = "Invalid email format"
            isValid = false
        } else {
            emailError = null
        }

        // Password validation
        if (password.length < 6) {
            passwordError = "Password must be at least 6 characters"
            isValid = false
        } else {
            passwordError = null
        }

        // Confirm password validation
        if (password != confirmPassword) {
            confirmPasswordError = "Passwords don't match"
            isValid = false
        } else {
            confirmPasswordError = null
        }

        return isValid
    }

    // Your existing UI code here, but update the Button onClick:
    Button(
        onClick = {
            if (validateForm()) {
                viewModel.signUp(email, password)
            }
        },
        modifier = Modifier
            .height(58.dp)
            .width(335.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF007AFF)),
        enabled = email.isNotEmpty() &&
                password.isNotEmpty() &&
                confirmPassword.isNotEmpty() &&
                authState !is AuthState.Loading
    ) {
        if (authState is AuthState.Loading) {
            CircularProgressIndicator(color = Color.White)
        } else {
            Text(
                text = "Sign Up",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }

    // Update your "Don't have an account" row:
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Already have an account?",
            fontWeight = FontWeight.SemiBold,
            fontSize = 15.sp,
            color = Color.Black
        )
        TextButton(onClick = onBackToLogin) {
            Text(text = "Login", fontSize = 15.sp, color = Color.DarkGray)
        }
    }

    // Show error messages if they exist
    emailError?.let { error ->
        Text(
            text = error,
            color = Color.Red,
            modifier = Modifier.padding(start = 16.dp)
        )
    }

    passwordError?.let { error ->
        Text(
            text = error,
            color = Color.Red,
            modifier = Modifier.padding(start = 16.dp)
        )
    }

    confirmPasswordError?.let { error ->
        Text(
            text = error,
            color = Color.Red,
            modifier = Modifier.padding(start = 16.dp)
        )
    }
}