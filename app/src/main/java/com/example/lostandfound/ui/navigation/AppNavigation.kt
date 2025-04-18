package com.example.lostandfound.ui.navigation

import android.util.Log
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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

/**
 * Sealed class defining all navigation routes in the app.
 * Each object represents a screen with its route path.
 */
sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Signup : Screen("signup")
    object Browse : Screen("browse")
    object Post : Screen("post")
    object History : Screen("history")
    object Settings : Screen("settings")
    object Detail : Screen("detail/{itemId}") // Route with path parameter
    object Profile : Screen("profile")
    object Chat : Screen("chat/{chatId}") // Route with path parameter
    object ChatList : Screen("chats")

    /**
     * Helper method to create a parametrized route for Detail screen
     * itemId The ID of the item to view details for
     * Formatted route string with the itemId parameter
     */
    fun createRoute(itemId: String): String {
        return "detail/$itemId"
    }
}

/**
 * Main navigation component that sets up the navigation graph and handles routing.
 * Determines the start destination based on authentication state.
 *
 * viewModel The ViewModel that provides data and states to screens
 * modifier Optional modifier for the NavHost
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation(
    viewModel: LostAndFoundViewModel,
    modifier: Modifier = Modifier
) {
    // Create a navigation controller to manage app navigation
    val navController = rememberNavController()
    // Collect the current authentication state
    val authState by viewModel.authState.collectAsState()

    // Set up the navigation host with routes to different screens
    // Start destination depends on whether user is logged in
    NavHost(
        navController = navController,
        startDestination = if (authState is AuthState.Authenticated) Screen.Browse.route else Screen.Login.route,
        modifier = modifier
    ) {
        // Login screen route
        composable(Screen.Login.route) {
            LoginScreen(
                viewModel = viewModel,
                authState = authState,
                onNavigateToSignUp = { navController.navigate(Screen.Signup.route) },
                onLoginSuccess = {
                    // Navigate to Browse screen and remove Login from back stack
                    navController.navigate(Screen.Browse.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        // Browse (Home) screen route - uses the bottom navigation
        composable(Screen.Browse.route) {
            MainScaffold(
                navController = navController,
                viewModel = viewModel,
                currentRoute = Screen.Browse.route
            ) { paddingModifier ->
                BrowseScreen(
                    modifier = paddingModifier,
                    viewModel = viewModel,
                    onNavigateToDetail = { item ->
                        navController.navigate(Screen.Detail.createRoute(item.id))
                    },
                    onNavigateToPost = { navController.navigate(Screen.Post.route) },
                    onNavigateToHistory = { navController.navigate(Screen.History.route) },
                    onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                    onLogout = {
                        // Handle logout by signing out and navigating to login
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

        // History screen route - shows user's posts
        composable(Screen.History.route) {
            MainScaffold(
                navController = navController,
                viewModel = viewModel,
                currentRoute = Screen.History.route
            ) { paddingModifier ->
                HistoryScreen(
                    modifier = paddingModifier,
                    viewModel = viewModel,
                    onNavigateToDetail = { item ->
                        navController.navigate(Screen.Detail.createRoute(item.id))
                    },
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
        
        // Signup screen route
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
        
        // Post new item screen route
        composable(Screen.Post.route) {
            PostScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        // Settings screen route
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
        
        // Detail screen route with itemId parameter
        composable(
            route = Screen.Detail.route,
            arguments = listOf(navArgument("itemId") { type = NavType.StringType })
        ) { backStackEntry ->
            // Extract the itemId parameter from navigation arguments
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

        // Profile screen route - uses the bottom navigation
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

        // Chat screen route with chatId parameter
        composable(
            route = Screen.Chat.route,
            arguments = listOf(navArgument("chatId") { type = NavType.StringType })
        ) { backStackEntry ->
            // Extract the chatId parameter from navigation arguments
            val chatId = backStackEntry.arguments?.getString("chatId") ?: ""
            ChatScreen(
                chatId = chatId,
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Chat list screen route - uses the bottom navigation
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

/**
 * Scaffold layout that includes the bottom navigation bar.
 * Used by screens that are part of the main navigation flow (Browse, History, ChatList, Profile).
 *
 * navController The navigation controller
 * viewModel The ViewModel that provides data to screens
 * currentRoute The current route to highlight in the bottom navigation
 * content The content to display within the scaffold
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScaffold(
    navController: NavHostController,
    viewModel: LostAndFoundViewModel,
    currentRoute: String,
    content: @Composable (Modifier) -> Unit
) {
    // Define the bottom navigation items
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
    
    // Create scaffold with bottom navigation bar
    Scaffold(
        bottomBar = {
            // Add elevation to bottom bar with surface
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp,
                shape = RoundedCornerShape(0.dp)
            ) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 0.dp
                ) {
                    // Get current navigation state
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentDestination = navBackStackEntry?.destination
                    
                    // Create navigation items for bottom bar
                    items.forEach { item ->
                        NavigationBarItem(
                            icon = { 
                                Icon(
                                    // Show selected or unselected icon based on current route
                                    imageVector = if (currentDestination?.hierarchy?.any { it.route == item.route } == true) {
                                        item.selectedIcon
                                    } else {
                                        item.unselectedIcon
                                    },
                                    contentDescription = item.label
                                )
                            },
                            selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
                            onClick = {
                                // Navigate with single top pattern to avoid duplicate screens on the stack
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                indicatorColor = MaterialTheme.colorScheme.surface
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        // Apply padding from scaffold to content
        content(Modifier.padding(innerPadding))
    }
}

/**
 * Data class representing an item in the bottom navigation.
 *
 * route The route this navigation item links to
 * selectedIcon The icon to display when this item is selected
 * unselectedIcon The icon to display when this item is not selected
 * label The text label for this navigation item
 */
data class NavigationItem(
    val route: String,
    val selectedIcon: androidx.compose.ui.graphics.vector.ImageVector,
    val unselectedIcon: androidx.compose.ui.graphics.vector.ImageVector,
    val label: String
) 