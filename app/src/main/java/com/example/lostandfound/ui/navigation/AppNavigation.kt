package com.example.lostandfound.ui.navigation

import android.util.Log
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.lostandfound.ui.screens.BrowseScreen
import com.example.lostandfound.ui.screens.ChatListScreen
import com.example.lostandfound.ui.screens.ChatScreen
import com.example.lostandfound.ui.screens.DetailScreen
import com.example.lostandfound.ui.screens.HistoryScreen
import com.example.lostandfound.ui.screens.LoginScreen
import com.example.lostandfound.ui.screens.PostScreen
import com.example.lostandfound.ui.screens.ProfileScreen
import com.example.lostandfound.ui.screens.SettingsScreen
import com.example.lostandfound.ui.screens.SignupScreen
import com.example.lostandfound.viewmodel.AuthState
import com.example.lostandfound.viewmodel.LostAndFoundViewModel

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Signup : Screen("signup")
    object Browse : Screen("browse")
    object Post : Screen("post")
    object History : Screen("history")
    object Settings : Screen("settings")
    object Detail : Screen("detail/{itemId}")
    object Profile : Screen("profile")
    object Chat : Screen("chat/{chatId}")
    object ChatList : Screen("chats")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation(viewModel: LostAndFoundViewModel) {
    val navController = rememberNavController()
    val authState by viewModel.authState.collectAsState()
    
    NavHost(
        navController = navController,
        startDestination = if (authState is AuthState.Authenticated) Screen.Browse.route else Screen.Login.route
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                viewModel = viewModel,
                authState = authState,
                onNavigateToSignUp = { navController.navigate(Screen.Signup.route) },
                onLoginSuccess = { navController.navigate(Screen.Browse.route) {
                    popUpTo(Screen.Login.route) { inclusive = true }
                }}
            )
        }
        
        composable(Screen.Signup.route) {
            SignupScreen(
                viewModel = viewModel,
                authState = authState,
                onNavigateToLogin = { navController.navigate(Screen.Login.route) },
                onSignupSuccess = { navController.navigate(Screen.Browse.route) {
                    popUpTo(Screen.Signup.route) { inclusive = true }
                }}
            )
        }
        
        composable(Screen.Browse.route) {
            MainScaffold(
                navController = navController,
                viewModel = viewModel,
                currentRoute = Screen.Browse.route
            ) { modifier ->
                BrowseScreen(
                    modifier = modifier,
                    viewModel = viewModel,
                    onNavigateToDetail = { lostItem ->
                        try {
                            navController.navigate(Screen.Detail.route.replace("{itemId}", lostItem.id))
                        } catch (e: Exception) {
                            // Handle error
                            Log.e("AppNavigation", "Error navigating to detail", e)
                        }
                    },
                    onNavigateToPost = { 
                        navController.navigate(Screen.Post.route)
                    },
                    onNavigateToHistory = {
                        navController.navigate(Screen.History.route)
                    },
                    onNavigateToSettings = {
                        navController.navigate(Screen.Settings.route)
                    },
                    onLogout = {
                        viewModel.signOut()
                        navController.navigate(Screen.Login.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                inclusive = true
                            }
                        }
                    }
                )
            }
        }
        
        composable(Screen.Post.route) {
            PostScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(Screen.History.route) {
            // Check if user is authenticated
            val isAuthenticated = (viewModel.authState.collectAsState().value is AuthState.Authenticated)
            
            HistoryScreen(
                viewModel = viewModel,
                onNavigateToDetail = { lostItem ->
                    try {
                        navController.navigate(Screen.Detail.route.replace("{itemId}", lostItem.id))
                    } catch (e: Exception) {
                        // Handle error
                        Log.e("AppNavigation", "Error navigating to detail", e)
                    }
                },
                onNavigateToPost = { 
                    navController.navigate(Screen.Post.route) 
                },
                onNavigateBack = { 
                    try {
                        navController.popBackStack()
                    } catch (e: Exception) {
                        Log.e("AppNavigation", "Error navigating back", e)
                    }
                }
            )
        }
        
        composable(Screen.Settings.route) {
            SettingsScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToHistory = { 
                    // Check auth status before navigating to History
                    val isAuthenticated = viewModel.authState.value is AuthState.Authenticated
                    if (isAuthenticated) {
                        navController.navigate(Screen.History.route) {
                            popUpTo(Screen.Settings.route)
                        }
                    } else {
                        // Log the issue but don't crash
                        Log.w("AppNavigation", "Cannot navigate to History from Settings - user not authenticated")
                    }
                },
                onLogout = {
                    viewModel.signOut()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            inclusive = true
                        }
                    }
                }
            )
        }
        
        composable(
            route = Screen.Detail.route,
            arguments = listOf(navArgument("itemId") { type = NavType.StringType })
        ) { backStackEntry ->
            val itemId = backStackEntry.arguments?.getString("itemId") ?: ""
            
            DetailScreen(
                itemId = itemId,
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onDeleteSuccess = { navController.popBackStack() },
                onNavigateToChat = { chatId ->
                    navController.navigate(Screen.Chat.route.replace("{chatId}", chatId))
                }
            )
        }

        composable(Screen.Profile.route) {
            MainScaffold(
                navController = navController,
                viewModel = viewModel,
                currentRoute = Screen.Profile.route
            ) { modifier ->
                ProfileScreen(
                    modifier = modifier,
                    viewModel = viewModel,
                    onNavigateToHistory = { navController.navigate(Screen.History.route) },
                    onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                    onLogout = {
                        viewModel.signOut()
                        navController.navigate(Screen.Login.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                inclusive = true
                            }
                        }
                    }
                )
            }
        }

        composable(
            route = Screen.Chat.route,
            arguments = listOf(navArgument("chatId") { type = NavType.StringType })
        ) { backStackEntry ->
            val chatId = backStackEntry.arguments?.getString("chatId") ?: ""
            ChatScreen(
                chatId = chatId,
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.ChatList.route) {
            MainScaffold(
                navController = navController,
                viewModel = viewModel,
                currentRoute = Screen.ChatList.route
            ) { modifier ->
                ChatListScreen(
                    modifier = modifier,
                    viewModel = viewModel,
                    onNavigateToChat = { chatId ->
                        navController.navigate(Screen.Chat.route.replace("{chatId}", chatId))
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScaffold(
    navController: NavHostController,
    viewModel: LostAndFoundViewModel,
    currentRoute: String,
    content: @Composable (Modifier) -> Unit
) {
    val items = listOf(
        NavigationItem(
            route = Screen.Browse.route,
            selectedIcon = Icons.Filled.Home,
            unselectedIcon = Icons.Outlined.Home,
            label = "Browse"
        ),
        NavigationItem(
            route = Screen.History.route,
            selectedIcon = Icons.AutoMirrored.Filled.List,
            unselectedIcon = Icons.AutoMirrored.Outlined.List,
            label = "My Posts"
        ),
        NavigationItem(
            route = Screen.ChatList.route,
            selectedIcon = Icons.AutoMirrored.Filled.Chat,
            unselectedIcon = Icons.AutoMirrored.Outlined.Chat,
            label = "Chats"
        ),
        NavigationItem(
            route = Screen.Profile.route,
            selectedIcon = Icons.Filled.Person,
            unselectedIcon = Icons.Outlined.Person,
            label = "Profile"
        )
    )
    
    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                
                items.forEach { item ->
                    NavigationBarItem(
                        icon = { 
                            Icon(
                                imageVector = if (currentDestination?.hierarchy?.any { it.route == item.route } == true) {
                                    item.selectedIcon
                                } else {
                                    item.unselectedIcon
                                },
                                contentDescription = item.label
                            )
                        },
                        label = { Text(item.label) },
                        selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        content(Modifier.padding(innerPadding))
    }
}

data class NavigationItem(
    val route: String,
    val selectedIcon: androidx.compose.ui.graphics.vector.ImageVector,
    val unselectedIcon: androidx.compose.ui.graphics.vector.ImageVector,
    val label: String
) 