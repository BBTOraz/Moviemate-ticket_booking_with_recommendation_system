package com.example.moviemate.ui

//noinspection UsingMaterialAndMaterial3Libraries
import SelectedSceneScreen
import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.moviemate.data.TicketPurchase
import com.example.moviemate.ui.theme.DarkColorPalette

@SuppressLint("NewApi")
@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = currentRoute in listOf("cinema", "theatre", "ticket", "profile")

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                BottomNavigationBar(navController = navController)
            }
        },
        backgroundColor = DarkColorPalette.background.copy(alpha = 1f)
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "splash",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("splash") {
                SplashScreen(navController = navController)
            }
            composable("login") { LoginScreen(navController) }
            composable("register") { RegisterScreen(navController) }
            composable("cinema") { HomeScreen(navController) }
            composable("selectSeatsScreen") { SelectedSceneScreen(navController) }
            composable("theatre") { TheatreScreen() }
            composable("ticket") { TicketScreen() }
            composable("profile") { ProfileScreen() }
            composable("movie_details/{movieId}") { navBackStackEntry ->
                val movieId = navBackStackEntry.arguments?.getString("movieId")?: ""
                MovieDetailsScreen(movieId = movieId, navController)
            }
        }
    }
}