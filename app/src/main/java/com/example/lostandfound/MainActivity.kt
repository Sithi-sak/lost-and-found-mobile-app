package com.example.lostandfound

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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Initialize Firebase Storage Utils
        FirebaseStorageUtils.init(this)
        
        enableEdgeToEdge()
        setContent {
            val viewModel = viewModel<LostAndFoundViewModel>()
            val themeState by viewModel.themeState.collectAsState()
            val isDarkTheme = themeState == ThemeState.DARK
            
            LostAndFoundTheme(darkTheme = isDarkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation(viewModel = viewModel)
                }
            }
        }
    }
}