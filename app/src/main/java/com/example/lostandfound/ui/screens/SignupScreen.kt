package com.example.lostandfound.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.lostandfound.ui.theme.Primary
import com.example.lostandfound.ui.theme.Shapes
import com.example.lostandfound.viewmodel.AuthState
import com.example.lostandfound.viewmodel.LostAndFoundViewModel
import kotlinx.coroutines.launch

/**
 * SignupScreen composable that handles user registration.
 * Provides a form for users to create a new account with email, username, phone, and password.
 *
 * param viewModel The ViewModel that manages authentication state and business logic
 * param authState The current authentication state from the ViewModel
 * param onNavigateToLogin Callback function to navigate to the login screen
 * param onSignupSuccess Callback function to navigate to the main screen after successful signup
 */
@Composable
fun SignupScreen(
    viewModel: LostAndFoundViewModel,
    authState: AuthState,
    onNavigateToLogin: () -> Unit,
    onSignupSuccess: () -> Unit
) {
    // State variables for form fields
    var email by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    
    // Snackbar state for showing error messages
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    // Handle authentication state changes
    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Authenticated -> onSignupSuccess() // Navigate on successful signup
            is AuthState.Error -> {
                // Show error message in snackbar
                scope.launch {
                    snackbarHostState.showSnackbar(authState.message)
                }
            }
            else -> {} // Do nothing for other states
        }
    }
    
    // Main screen layout with snackbar support
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        // Content container
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Signup form column with scrolling enabled for smaller screens
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState()) // Enable scrolling for the form
                    .align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Screen title
                Text(
                    text = "Sign Up",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                // Username input field
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Username") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    )
                )
                
                Spacer(modifier = Modifier.height(16.dp))

                // Email input field
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    )
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Phone number input field with phone keyboard type
                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = { phoneNumber = it },
                    label = { Text("Phone Number") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Phone,
                        imeAction = ImeAction.Next
                    )
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Password input field with hidden text
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Next
                    )
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Confirm password input field with hidden text
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Confirm Password") },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    )
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Sign up button with validation
                Button(
                    onClick = {
                        // Check if passwords match before attempting signup
                        if (password == confirmPassword) {
                            viewModel.signUp(email, username, phoneNumber, password)
                        } else {
                            scope.launch {
                                snackbarHostState.showSnackbar("Passwords do not match")
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = Shapes.extraSmall,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    // Button is enabled only when all fields are filled and not in loading state
                    enabled = email.isNotEmpty() && username.isNotEmpty() && 
                             phoneNumber.isNotEmpty() && password.isNotEmpty() && 
                             confirmPassword.isNotEmpty() && authState !is AuthState.Loading
                ) {
                    Text("Sign Up")
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Link to login screen for existing users
                TextButton(onClick = onNavigateToLogin) {
                    Text("Already have an account? Login")
                }
            }
            
            // Loading indicator shown during authentication
            if (authState is AuthState.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
} 