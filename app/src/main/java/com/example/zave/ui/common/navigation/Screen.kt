package com.example.zave.ui.common.navigation


//sealed class for defining all the navigation routes in the app
sealed class Screen(val route: String) {

    //use login initial screen
    data object Auth : Screen("auth_screen")

    //main app where search and featured content are displayed
    data object Home : Screen("home_screen")

    //screen for search result
    data object Results : Screen("results_screen/{query}") {
        fun createRoute(query: String) = "results_screen/$query"

        const val ARG_QUERY = "query"
    }
    //app settings
    data object Settings : Screen("settings_screen")
}