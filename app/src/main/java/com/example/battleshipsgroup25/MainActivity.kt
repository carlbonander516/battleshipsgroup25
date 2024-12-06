package com.example.battleshipsgroup25

import GameModes
import Gameboard
import IntroScreen
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.battleshipsgroup25.ui.theme.Battleshipsgroup25Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Battleshipsgroup25Theme {
                val navController = rememberNavController()

                // Set up NavHost with the updated routes
                NavHost(
                    navController = navController,
                    startDestination = "intro"
                ) {
                    // Routes for all screens
                    composable("intro") { IntroScreen(navController) }
                    composable("game_modes") { GameModes(navController) }
                    composable("lobby") { Lobby(navController) }
                    composable("game_board") { Gameboard(navController) }

                    // Route for GameLobby with parameters
                    composable("game_lobby/{gameId}/{username}") { backStackEntry ->
                        val gameId = backStackEntry.arguments?.getString("gameId") ?: ""
                        val username = backStackEntry.arguments?.getString("username") ?: ""
                        GameLobby(navController, gameId, username)
                    }
                }
            }
        }
    }
}
