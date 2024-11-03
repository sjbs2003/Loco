package com.example.loco.ui.screens

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.loco.R
import com.example.loco.ui.AppViewModelProvider
import com.example.loco.viewModel.AuthState
import com.example.loco.viewModel.AuthViewModel


@Composable
fun SignUp(
    onSignUpSuccess: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AuthViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var phonenum by remember { mutableStateOf("") }
    var isPassVisible by remember { mutableStateOf(false) }

    // Collect auth state
    val authState by viewModel.authState.collectAsState()

    // Handle auth state changes
    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Authenticated -> onSignUpSuccess()
            is AuthState.Error -> {
                // You might want to show an error message
            }
            else -> {}
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
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

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Welcome User!",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Enter Your Email",
            fontSize = 15.sp,
            fontWeight = FontWeight.W500,
            modifier = Modifier.align(Alignment.Start)
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
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

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Enter Your Phone Number",
            fontSize = 15.sp,
            fontWeight = FontWeight.W500,
            modifier = Modifier.align(Alignment.Start)
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = phonenum,
            onValueChange = { phonenum = it },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(size = 15.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Black,  // Remove the outline when focused
                unfocusedIndicatorColor = Color.Black,
            ),
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
            textStyle = TextStyle(fontSize = 20.sp),
            placeholder = {
                Text(
                    text = "+91-1234567890",
                    color = Color.Gray
                )
            },
            trailingIcon = {
                if (phonenum.isNotEmpty()) {
                    IconButton(onClick = { phonenum = "" }) {
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

        Text(
            text = "Create Password",
            fontSize = 15.sp,
            fontWeight = FontWeight.W500,
            modifier = Modifier.align(Alignment.Start)
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
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

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Confirm Password",
            fontSize = 15.sp,
            fontWeight = FontWeight.W500,
            modifier = Modifier.align(Alignment.Start)
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
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
            onClick = {
                if (password == confirmPassword){
                    viewModel.signIn(email, password)
                }
            },
            modifier = Modifier
                .height(58.dp)
                .width(335.dp)
                .align(Alignment.CenterHorizontally),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF007AFF)),
            enabled = authState !is AuthState.Loading &&
                    email.isNotEmpty() &&
                    password.isNotEmpty() &&
                    confirmPassword.isNotEmpty() &&
                    password == confirmPassword
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

        Text(
            text = "Or login with",
            fontSize = 15.sp,
            color = Color.Gray,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(20.dp))

        Row {
            IconButton(onClick = { /*TODO*/ }) {}
            Spacer(modifier = Modifier.width(20.dp))
            IconButton(onClick = { /*TODO*/ }) {}
        }
    }
}