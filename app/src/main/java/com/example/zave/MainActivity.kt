package com.example.zave

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.zave.data.repository.AuthRepository
import com.example.zave.ui.auth.AuthScreen
import com.example.zave.ui.common.navigation.Screen
import com.example.zave.ui.home.HomeScreen
import com.example.zave.ui.results.ResultsScreen
import com.example.zave.ui.settings.SettingsScreen
import com.example.zave.ui.theme.ZaveTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint // Hilt Entry Point
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var authRepository: AuthRepository // Inject AuthRepository to determine start screen

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ZaveTheme {
                AppNavigation(authRepository = authRepository)
            }
        }
    }
}

/**
 * Defines the entire navigation structure of the application.
 */
@Composable
fun AppNavigation(authRepository: AuthRepository) {
    val navController = rememberNavController()

    // Determine the starting destination based on authentication state
    val startDestination = if (authRepository.isAuthenticated) {
        Screen.Home.route
    } else {
        Screen.Auth.route
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = Modifier.fillMaxSize()
    ) {
        // 1. Authentication Screen
        composable(Screen.Auth.route) {
            AuthScreen(navController = navController)
        }

        // 2. Home Screen
        composable(Screen.Home.route) {
            HomeScreen(navController = navController)
        }

        // 3. Search Results Screen (requires argument)
        composable(
            route = Screen.Results.route
        ) { backStackEntry ->
            val query = backStackEntry.arguments?.getString(Screen.Results.ARG_QUERY) ?: ""
            ResultsScreen(navController = navController, query = query)
        }

        // 4. Settings Screen
        composable(Screen.Settings.route) {
            SettingsScreen(navController = navController)
        }
    }
}