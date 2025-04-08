package com.example.lostandfound

// Import necessary Android and Compose components
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.lostandfound.ui.navigation.AppNavigation
import com.example.lostandfound.ui.theme.LostAndFoundTheme
import com.example.lostandfound.utils.FirebaseStorageUtils
import com.example.lostandfound.viewmodel.LostAndFoundViewModel
import com.example.lostandfound.viewmodel.ThemeState

/**
 * Main activity class that serves as the entry point for the application.
 * Handles initialization, theme management, and sets up the main UI.
 */
class MainActivity : ComponentActivity() {
    /**
     * Called when the activity is first created.
     * Sets up the application's initial state and UI.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize Firebase Storage Utils with the application context
        FirebaseStorageUtils.init(this)
        
        enableEdgeToEdge()
        
        // Set up the Compose UI
        setContent {
            // Get the ViewModel instance for managing app state
            val viewModel = viewModel<LostAndFoundViewModel>()
            
            // Collect theme state from ViewModel
            val themeState by viewModel.themeState.collectAsState()
            val isDarkTheme = themeState == ThemeState.DARK
            
            // Apply theme and set up the main UI surface
            LostAndFoundTheme(darkTheme = isDarkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Set up the main navigation component
                    AppNavigation(viewModel = viewModel)
                }
            }
        }
    }
}