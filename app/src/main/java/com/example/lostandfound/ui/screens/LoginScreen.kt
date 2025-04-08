package com.example.lostandfound.ui.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
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
import com.example.lostandfound.ui.theme.BorderGrey
import com.example.lostandfound.ui.theme.Primary
import com.example.lostandfound.ui.theme.Shapes
import com.example.lostandfound.ui.theme.White
import com.example.lostandfound.viewmodel.AuthState
import com.example.lostandfound.viewmodel.LostAndFoundViewModel
import kotlinx.coroutines.launch

/**
 * LoginScreen composable that handles user authentication.
 * Provides a form for users to enter their email and password to sign in.
 *
 * param viewModel The ViewModel that manages authentication state and business logic
 * param authState The current authentication state from the ViewModel
 * param onNavigateToSignUp Callback function to navigate to the sign-up screen
 * param onLoginSuccess Callback function to navigate to the main screen after successful login
 */
@Composable
fun LoginScreen(
    viewModel: LostAndFoundViewModel,
    authState: AuthState,
    onNavigateToSignUp: () -> Unit,
    onLoginSuccess: () -> Unit
) {
    // State variables for form fields
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    
    // Snackbar state for showing error messages
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    // Handle authentication state changes
    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Authenticated -> onLoginSuccess() // Navigate on successful login
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
            // Login form column
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // App title
                Text(
                    text = "Lost and Found",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(bottom = 24.dp)
                )
                
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
                
                // Password input field with hidden text
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    )
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Login button - enabled only when fields are filled and not loading
                Button(
                    onClick = { viewModel.signIn(email, password) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = Shapes.extraSmall,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    enabled = email.isNotEmpty() && password.isNotEmpty() && authState !is AuthState.Loading
                ) {
                    Text("Login")
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Link to sign-up screen for new users
                TextButton(onClick = onNavigateToSignUp) {
                    Text("Don't have an account? Sign up")
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